package com.chainedclimber.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.chainedclimber.ChainedClimberGame;
import com.chainedclimber.debug.DebugMode;
import com.chainedclimber.entities.BouncyBlock;
import com.chainedclimber.entities.BreakableBlock;
import com.chainedclimber.entities.Checkpoint;
import com.chainedclimber.entities.Goal;
import com.chainedclimber.entities.IceBlock;
import com.chainedclimber.entities.MovingPlatform;
import com.chainedclimber.entities.Platform;
import com.chainedclimber.entities.Player;
import com.chainedclimber.entities.Ramp;
import com.chainedclimber.entities.Spike;
import com.chainedclimber.systems.InputController;
import com.chainedclimber.systems.RenderCuller;
import com.chainedclimber.systems.SpatialHash;
import com.chainedclimber.utils.BlockType;
import com.chainedclimber.utils.Constants;
import com.chainedclimber.utils.LevelData;
import com.chainedclimber.utils.LevelGenerator;
import com.chainedclimber.utils.LevelMatrix;
import com.chainedclimber.utils.TextureManager;

public class GameScreen implements Screen {
    private ChainedClimberGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    
    private Player player;
    private LevelData levelData;
    private InputController inputController;
    
    // Checkpoint system
    private Vector2 lastCheckpoint;
    private boolean levelComplete = false;
    private float levelCompleteTimer = 0f;
    private static final float VICTORY_DISPLAY_TIME = 3.0f;
    
    // Performance optimizations
    private RenderCuller renderCuller;
    private SpatialHash<Platform> platformSpatialHash;
    private SpatialHash<Spike> spikeSpatialHash;
    
    // Frame timing for delta smoothing
    private float accumulatedDelta = 0f;
    private static final float FIXED_TIME_STEP = 1/60f; // 60 FPS physics
    private static final int MAX_PHYSICS_STEPS = 8; // Allow more catch-up on mobile devices

    // Render interpolation alpha (0..1)
    private float renderInterpolationAlpha = 0f;

    // Lightweight profiling
    private float physicsTimeAvgMs = 0f;
    private int profileFrameCounter = 0;
    private int lastPhysicsSteps = 0;
    
    // Render counters for debugging
    private int renderedEntities = 0;
    private int totalEntities = 0;
    
    // Collision counters
    private int platformCollisionCount = 0;
    private int spikeCollisionCount = 0;
    private int movingPlatformCollisionCount = 0;
    private int totalCollisionChecks = 0;
    private int spatialHashQueryCount = 0;
    
    // Moving platform tracking - stores the platform player is currently standing on
    private MovingPlatform currentMovingPlatform = null;
    
    private LevelMatrix currentLevelMatrix;
    
    // World dimensions (actual map size calculated from level matrix)
    private float worldWidth;
    private float worldHeight;
    
    // Camera viewport dimensions - DYNAMIC (adapts to window size)
    // Minimum viewport ensures readable game on small screens
    // ExtendViewport will show MORE content on larger screens
    private static final float MIN_VIEWPORT_WIDTH = 800f;   // Minimum ~10 blocks horizontally
    private static final float MIN_VIEWPORT_HEIGHT = 600f;  // Minimum ~7.5 blocks vertically
    private static final float MAX_VIEWPORT_WIDTH = 2400f;  // Maximum for ultra-wide screens
    private static final float MAX_VIEWPORT_HEIGHT = 1800f; // Maximum for tall screens
    
    public GameScreen(ChainedClimberGame game) {
        this.game = game;
        
        // Setup camera with DYNAMIC viewport that adapts to window size
        // ExtendViewport: maintains aspect ratio, shows MORE content on larger screens
        // NO BLACK BARS - always fills entire window
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(MIN_VIEWPORT_WIDTH, MIN_VIEWPORT_HEIGHT, 
                                      MAX_VIEWPORT_WIDTH, MAX_VIEWPORT_HEIGHT, camera);
        camera.position.set(MIN_VIEWPORT_WIDTH / 2, MIN_VIEWPORT_HEIGHT / 2, 0);
        camera.update();
        
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true); // Optimization: auto-switch shape types
        
        // OPTIMIZATION: Larger SpriteBatch for better performance with many textures
        // Default size is 1000, we use 8000 for large maps to reduce flush() calls
        spriteBatch = new SpriteBatch(8000);
        
        inputController = new InputController();
        
        // Initialize texture manager and preload textures
        TextureManager.getInstance().preloadGameTextures();
        
        // Initialize performance systems
        renderCuller = new RenderCuller();
        
        // Generate level from matrix - USE LEVEL 3 with simple moving platform test!
        currentLevelMatrix = LevelGenerator.createLevel3(); // Level 3: Simple test level with one moving platform
        currentLevelMatrix.printMatrix(); // Debug output
        levelData = LevelGenerator.generateAllEntities(currentLevelMatrix);
        
        // Calculate actual world dimensions from level matrix
        // World size = number of cells × cell dimensions
        worldWidth = currentLevelMatrix.getColumns() * currentLevelMatrix.getCellWidth();
        worldHeight = currentLevelMatrix.getRows() * currentLevelMatrix.getCellHeight();
        
        System.out.println("World dimensions: " + worldWidth + "x" + worldHeight);
        System.out.println("Viewport range: " + MIN_VIEWPORT_WIDTH + "-" + MAX_VIEWPORT_WIDTH + 
                          " x " + MIN_VIEWPORT_HEIGHT + "-" + MAX_VIEWPORT_HEIGHT);
        System.out.println("Initial window: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());
        
        // Initialize spatial hashes for ultra-fast collision detection (use world dimensions)
        platformSpatialHash = new SpatialHash<>(worldWidth / 8f); // 8x8 grid based on actual world
        spikeSpatialHash = new SpatialHash<>(worldWidth / 8f);
        
        // Build spatial hashes with actual entities
        buildSpatialHashes();
        
        // Count total entities for performance metrics
        totalEntities = levelData.platforms.size() + levelData.spikes.size() + 
                       levelData.bouncyBlocks.size() + levelData.iceBlocks.size() +
                       levelData.movingPlatforms.size() + levelData.breakableBlocks.size() +
                       levelData.checkpoints.size() + levelData.goals.size() + levelData.ramps.size();
        
        // Display optimization stats
        System.out.println("=== OPTIMIZATION METRICS ===");
        System.out.println("Total entities after merging: " + totalEntities);
        System.out.println("Platforms (merged): " + levelData.platforms.size());
        System.out.println("Map cells: " + (currentLevelMatrix.getRows() * currentLevelMatrix.getColumns()));
        System.out.println("Optimization ratio: " + 
            String.format("%.1f%%", (1.0 - (double)totalEntities / (currentLevelMatrix.getRows() * currentLevelMatrix.getColumns())) * 100));
        
        // Find spawn point in matrix (marked with @/SPAWN_POINT)
        Vector2 spawnPoint = findSpawnPointInMatrix(currentLevelMatrix);
        player = new Player(spawnPoint.x, spawnPoint.y);
        lastCheckpoint = new Vector2(spawnPoint.x, spawnPoint.y);
    }
    
    /**
     * Find spawn point marked in the level matrix
     * If not found, default to safe position on ground
     */
    private Vector2 findSpawnPointInMatrix(LevelMatrix matrix) {
        // Search matrix for SPAWN_POINT block
        for (int row = 0; row < matrix.getRows(); row++) {
            for (int col = 0; col < matrix.getColumns(); col++) {
                if (matrix.getCell(row, col) == BlockType.SPAWN_POINT) {
                    // Found spawn point, convert to world coordinates
                    float[] worldPos = matrix.gridToWorld(row, col);
                    float worldX = worldPos[0];
                    float worldY = worldPos[1];
                    
                    // Spawn on top of the spawn block
                    worldY += Constants.PLATFORM_HEIGHT;
                    
                    return new Vector2(worldX, worldY);
                }
            }
        }
        
        // No spawn point found - default to ground level
        float[] defaultPos = matrix.gridToWorld(matrix.getRows() - 1, 1);
        float defaultX = defaultPos[0];
        float defaultY = defaultPos[1] + Constants.PLATFORM_HEIGHT;
        return new Vector2(defaultX, defaultY);
    }
    
    /**
     * Build spatial hash structures for O(1) collision queries
     */
    private void buildSpatialHashes() {
        platformSpatialHash.clear();
        spikeSpatialHash.clear();
        
        for (Platform platform : levelData.platforms) {
            platformSpatialHash.insert(platform, platform.getBounds());
        }
        
        for (Spike spike : levelData.spikes) {
            spikeSpatialHash.insert(spike, spike.getBounds());
        }
    }
    
    @Override
    public void show() {
    }
    
    @Override
    public void render(float delta) {
        // Handle debug mode controls
        handleDebugControls();
        
        // Check if we should pause for step mode
        if (DebugMode.shouldPause()) {
            renderFrame(); // Still render, just don't update
            DebugMode.renderOverlay(spriteBatch);
            return;
        }
        
        if (levelComplete) {
            levelCompleteTimer += delta;
            renderVictoryScreen();
            
            // After displaying victory screen, could return to main menu
            // For now, just keep showing the victory screen
            return;
        }
        
        // Fixed timestep physics for consistent simulation across devices
        // Cap delta to 0.1s (6 frames at 60fps) to prevent huge spikes on slow devices
        accumulatedDelta += Math.min(delta, 0.1f);

        // Reset collision counters for this frame
        platformCollisionCount = 0;
        spikeCollisionCount = 0;
        movingPlatformCollisionCount = 0;
        totalCollisionChecks = 0;
        spatialHashQueryCount = 0;

        int physicsSteps = 0;
        long physicsStart = System.nanoTime();
        
        // Run physics loop with CORRECT interpolation state management
        while (accumulatedDelta >= FIXED_TIME_STEP && physicsSteps < MAX_PHYSICS_STEPS) {
            updatePhysics(FIXED_TIME_STEP);
            accumulatedDelta -= FIXED_TIME_STEP;
            physicsSteps++;
            
            // CRITICAL: Save state AFTER physics step completes
            // This is the state we'll interpolate FROM (not TO)
            if (physicsSteps == 1 || accumulatedDelta < FIXED_TIME_STEP) {
                player.savePreviousPosition();
                for (MovingPlatform mp : levelData.movingPlatforms) {
                    mp.savePreviousState();
                }
            }
        }
        long physicsElapsedNanos = System.nanoTime() - physicsStart;

        // Update profiling averages
        float physicsMs = physicsElapsedNanos / 1_000_000f;
        physicsTimeAvgMs = physicsTimeAvgMs * 0.9f + physicsMs * 0.1f;
        lastPhysicsSteps = physicsSteps;
        
        profileFrameCounter++;
        if (profileFrameCounter % 60 == 0) {
            Gdx.app.log("Perf", String.format("Physics steps/frame: %d, avg ms: %.3f", physicsSteps, physicsTimeAvgMs));
        }

        // Compute interpolation alpha for rendering
        // Alpha represents how far between the last physics step and the next one we are
        renderInterpolationAlpha = (physicsSteps > 0) ? (accumulatedDelta / FIXED_TIME_STEP) : 0f;

        // Update rendering
        renderFrame();
    }
    
    /**
     * Fixed timestep physics update for consistent simulation
     */
    private void updatePhysics(float delta) {
        // STEP 1: Update moving platforms FIRST and store their movement
        for (MovingPlatform mp : levelData.movingPlatforms) {
            if (mp != null) {
                mp.setLevelData(levelData);
                // Store position before update
                float oldX = mp.getBounds().x;
                float oldY = mp.getBounds().y;
                
                // Update platform
                mp.update(delta);
                
                // Calculate actual delta movement
                float deltaX = mp.getBounds().x - oldX;
                float deltaY = mp.getBounds().y - oldY;
                mp.setLastDelta(deltaX, deltaY); // Store the actual movement
            }
        }
        
        // STEP 2: Check if player is standing on a moving platform and apply platform movement
        MovingPlatform ridingPlatform = null;
        Rectangle pBounds = player.getBounds();
        
        for (MovingPlatform mp : levelData.movingPlatforms) {
            Rectangle mpBounds = mp.getBounds();
            
            // Calculate vertical distance between player's feet and platform's top surface
            float playerBottom = pBounds.y;
            float platformTop = mpBounds.y + mpBounds.height;
            float verticalDistance = Math.abs(playerBottom - platformTop);
            
            // Check if player horizontally overlaps with platform
            boolean horizontalOverlap = (pBounds.x < mpBounds.x + mpBounds.width) && 
                                       (pBounds.x + pBounds.width > mpBounds.x);
            
            // Determine if player is standing on the platform:
            // 1. Player must horizontally overlap with platform
            // 2. Player's feet must be within 3 pixels of platform top (increased tolerance)
            // 3. Player must not be jumping upward (velocity.y <= 0) OR just barely leaving (velocity.y < 100)
            //    This prevents the player from "sticking" when trying to jump off
            boolean standingOnPlatform = horizontalOverlap && 
                                        (verticalDistance <= 3) && 
                                        (player.getVelocity().y <= 100);
            
            if (standingOnPlatform) {
                ridingPlatform = mp;
                
                // Only set grounded and apply movement if player is actually on top (not jumping off)
                if (player.getVelocity().y <= 0) {
                    player.setGrounded(true);
                    
                    // Apply platform's movement delta to player position
                    player.getPosition().x += mp.getLastDeltaX();
                    player.getPosition().y += mp.getLastDeltaY();
                    player.getBounds().setPosition(player.getPosition().x, player.getPosition().y);
                }
                
                break;
            }
        }
        
        // STEP 3: Handle player input
        inputController.update();
        
        if (inputController.isLeftPressed()) {
            player.moveLeft();
        } else if (inputController.isRightPressed()) {
            player.moveRight();
        } else {
            player.stopHorizontalMovement();
        }
        
        if (inputController.isJumpJustPressed()) {
            player.jump();
        }
        // Jump is auto-consumed by input controller on next update()
        
        // STEP 4: Update player physics (normal movement)
        player.update(delta, worldWidth);
        
        // Update breakable blocks
        for (BreakableBlock bb : levelData.breakableBlocks) {
            bb.update(delta);
        }
        
        // Update checkpoints and goal
        for (Checkpoint cp : levelData.checkpoints) {
            cp.update(delta);
        }
        for (Goal goal : levelData.goals) {
            goal.update(delta);
        }
        
        // Reset friction (will be set by ice blocks if standing on them)
        player.resetFriction();
        
        // OPTIMIZED: Use spatial hash for collision detection (O(1) instead of O(n))
        Rectangle playerBounds = player.getBounds();
        Array<Platform> nearbyPlatforms = platformSpatialHash.query(playerBounds);
        Array<Spike> nearbySpikes = spikeSpatialHash.query(playerBounds);
        
        // Track spatial hash queries
        spatialHashQueryCount += 2; // One for platforms, one for spikes
        
        // Check collisions only with nearby platforms
        for (Platform platform : nearbyPlatforms) {
            player.checkCollision(platform);
            platformCollisionCount++;
            totalCollisionChecks++;
        }
        
        // Check collisions with bouncy blocks
        for (BouncyBlock bouncy : levelData.bouncyBlocks) {
            // Check if player is landing on top
            if (player.getBounds().overlaps(bouncy.getBounds())) {
                float overlapBottom = (player.getBounds().y + player.getBounds().height) - bouncy.getBounds().y;
                float overlapTop = (bouncy.getBounds().y + bouncy.getBounds().height) - player.getBounds().y;
                
                if (overlapTop < overlapBottom && overlapTop > 0) {
                    // Landing from above - bounce!
                    player.bounce(bouncy.getBounceVelocity());
                }
            }
        }
        
        // Check collisions with ice blocks (solid with low friction)
        for (IceBlock ice : levelData.iceBlocks) {
            // Treat ice as solid platform
            Platform icePlatform = new Platform(ice.getBounds().x, ice.getBounds().y,
                                               ice.getBounds().width, ice.getBounds().height, false);
            player.checkCollision(icePlatform);
            
            // Apply friction reduction when player is on top
            if (player.getBounds().overlaps(ice.getBounds())) {
                float overlapTop = (ice.getBounds().y + ice.getBounds().height) - player.getBounds().y;
                if (overlapTop < 10 && overlapTop > 0) { // Player is on top
                    player.setFrictionMultiplier(ice.getFrictionMultiplier());
                }
            }
        }
        
        // Handle moving platform collisions (treat as solid obstacles)
        // Platform movement is already applied via setPlatformVelocity() before player.update()
        currentMovingPlatform = ridingPlatform;
        
        for (MovingPlatform mp : levelData.movingPlatforms) {
            // Only do collision check if NOT riding this platform
            if (mp != ridingPlatform) {
                Rectangle mpBounds = mp.getBounds();
                Platform solidPlatform = new Platform(mpBounds.x, mpBounds.y, 
                                                     mpBounds.width, mpBounds.height, false);
                player.checkCollision(solidPlatform);
                movingPlatformCollisionCount++;
                totalCollisionChecks++;
            }
        }
        
        // Check collisions with ramps (diagonal platforms with vertical walls)
        for (Ramp ramp : levelData.ramps) {
            if (ramp.checkCollision(player.getBounds(), player.getPosition(), player.getVelocity())) {
                player.setGrounded(true);
            }
        }
        
        // Check collisions with breakable blocks (only if not broken)
        for (BreakableBlock bb : levelData.breakableBlocks) {
            if (!bb.isBroken()) {
                Platform tempPlatform = new Platform(bb.getBounds().x, bb.getBounds().y,
                                                    bb.getBounds().width, bb.getBounds().height, false);
                player.checkCollision(tempPlatform);
                
                // Check if player is standing on top
                float overlapTop = (bb.getBounds().y + bb.getBounds().height) - player.getBounds().y;
                if (overlapTop < 10 && overlapTop > 0) {
                    bb.setPlayerOnTop(true);
                } else {
                    bb.setPlayerOnTop(false);
                }
            }
        }
        
        // Check collisions only with nearby spikes
        for (Spike spike : nearbySpikes) {
            spikeCollisionCount++;
            totalCollisionChecks++;
            if (spike.checkCollision(playerBounds)) {
                // Player hit spike - respawn at last checkpoint
                player.resetPosition(lastCheckpoint);
            }
        }
        
        // Check checkpoint activation
        for (Checkpoint cp : levelData.checkpoints) {
            if (!cp.isActivated() && cp.checkCollision(player.getBounds())) {
                cp.activate();
                lastCheckpoint = cp.getSpawnPosition();
            }
        }
        
        // Check goal completion
        for (Goal goal : levelData.goals) {
            if (goal.checkCollision(player.getBounds())) {
                levelComplete = true;
            }
        }
        
    }
    
    /**
     * Optimized rendering with frustum culling
     */
    private void renderFrame() {
        // ========================================
        // CAMERA SYSTEM - Works for ANY map size
        // ========================================
        
        // 1. Calculate player center position in world coordinates
        float playerCenterX = player.getPosition().x + Constants.PLAYER_WIDTH / 2;
        float playerCenterY = player.getPosition().y + Constants.PLAYER_HEIGHT / 2;
        
        // 2. Get ACTUAL viewport dimensions (changes with window resize)
        float viewportWidth = camera.viewportWidth;
        float viewportHeight = camera.viewportHeight;
        float halfViewportWidth = viewportWidth / 2;
        float halfViewportHeight = viewportHeight / 2;
        
        // 3. Calculate desired camera position (centered on player)
        float desiredCameraX = playerCenterX;
        float desiredCameraY = playerCenterY;
        
        // 4. Clamp camera to world boundaries
        // Camera cannot show area outside the world (0, 0) to (worldWidth, worldHeight)
        
        // X-axis clamping
        float minCameraX = halfViewportWidth; // Left edge
        float maxCameraX = worldWidth - halfViewportWidth; // Right edge
        
        if (worldWidth <= viewportWidth) {
            // World is narrower than viewport - center on world
            camera.position.x = worldWidth / 2;
        } else {
            // Normal case - clamp camera to world bounds
            camera.position.x = Math.max(minCameraX, Math.min(maxCameraX, desiredCameraX));
        }
        
        // Y-axis clamping
        float minCameraY = halfViewportHeight; // Bottom edge
        float maxCameraY = worldHeight - halfViewportHeight; // Top edge
        
        if (worldHeight <= viewportHeight) {
            // World is shorter than viewport - center on world
            camera.position.y = worldHeight / 2;
        } else {
            // Normal case - clamp camera to world bounds
            camera.position.y = Math.max(minCameraY, Math.min(maxCameraY, desiredCameraY));
        }
        
        // 5. Update camera matrices
        camera.update();
        
        // CRITICAL: Apply viewport to OpenGL - this sets glViewport() to use full window
        // WITHOUT this, OpenGL uses default viewport causing black bars!
        viewport.apply();
        
        // Update frustum culler
        renderCuller.update(camera);
        
        // Clear screen
        Gdx.gl.glClearColor(Constants.BACKGROUND_COLOR[0], Constants.BACKGROUND_COLOR[1], 
                           Constants.BACKGROUND_COLOR[2], 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        // Reset render counter
        renderedEntities = 0;
        
        // Render filled shapes with frustum culling
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // OPTIMIZED: Only render visible entities
        for (Platform platform : levelData.platforms) {
            if (renderCuller.isVisible(platform.getBounds())) {
                platform.render(shapeRenderer);
                renderedEntities++;
            }
        }
        for (Spike spike : levelData.spikes) {
            if (renderCuller.isVisible(spike.getBounds())) {
                spike.render(shapeRenderer);
                renderedEntities++;
            }
        }
        for (BouncyBlock bouncy : levelData.bouncyBlocks) {
            if (renderCuller.isVisible(bouncy.getBounds())) {
                bouncy.render(shapeRenderer);
                renderedEntities++;
            }
        }
        for (IceBlock ice : levelData.iceBlocks) {
            if (renderCuller.isVisible(ice.getBounds())) {
                ice.render(shapeRenderer);
                renderedEntities++;
            }
        }
        for (MovingPlatform mp : levelData.movingPlatforms) {
            if (renderCuller.isVisible(mp.getBounds())) {
                mp.renderInterpolated(shapeRenderer, renderInterpolationAlpha);
                renderedEntities++;
            }
        }
        for (Ramp ramp : levelData.ramps) {
            if (renderCuller.isVisible(ramp.getBounds())) {
                ramp.render(shapeRenderer);
                renderedEntities++;
            }
        }
        for (BreakableBlock bb : levelData.breakableBlocks) {
            if (!bb.isBroken() && renderCuller.isVisible(bb.getBounds())) {
                bb.render(shapeRenderer);
                renderedEntities++;
            }
        }
        for (Checkpoint cp : levelData.checkpoints) {
            if (renderCuller.isVisible(cp.getBounds())) {
                cp.render(shapeRenderer);
                renderedEntities++;
            }
        }
        for (Goal goal : levelData.goals) {
            if (renderCuller.isVisible(goal.getBounds())) {
                goal.render(shapeRenderer);
                renderedEntities++;
            }
        }
        
        shapeRenderer.end();
        
        // Render all entities with textures (efficient batch rendering)
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        
        // Render platforms with texture
        for (Platform platform : levelData.platforms) {
            if (renderCuller.isVisible(platform.getBounds())) {
                platform.renderTexture(spriteBatch);
            }
        }
        
        // Render ice blocks with texture
        for (IceBlock ice : levelData.iceBlocks) {
            if (renderCuller.isVisible(ice.getBounds())) {
                ice.renderTexture(spriteBatch);
            }
        }
        
        // Render ramps with texture
        for (Ramp ramp : levelData.ramps) {
            if (renderCuller.isVisible(ramp.getBounds())) {
                ramp.renderTexture(spriteBatch);
            }
        }
        
        // Render spikes with texture
        for (Spike spike : levelData.spikes) {
            if (renderCuller.isVisible(spike.getBounds())) {
                spike.renderTexture(spriteBatch);
            }
        }
        
        // Render player sprite (bigger now!) with interpolation
        player.updateDirection(Gdx.graphics.getDeltaTime());
        player.renderSpriteInterpolated(spriteBatch, renderInterpolationAlpha);
        
        spriteBatch.end();
        
        // OPTIMIZED: Render borders only for visible entities
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        
        for (Platform platform : levelData.platforms) {
            if (renderCuller.isVisible(platform.getBounds())) {
                platform.renderBorder(shapeRenderer);
            }
        }
        for (Spike spike : levelData.spikes) {
            if (renderCuller.isVisible(spike.getBounds())) {
                spike.renderBorder(shapeRenderer);
            }
        }
        for (BouncyBlock bouncy : levelData.bouncyBlocks) {
            if (renderCuller.isVisible(bouncy.getBounds())) {
                bouncy.renderBorder(shapeRenderer);
            }
        }
        for (IceBlock ice : levelData.iceBlocks) {
            if (renderCuller.isVisible(ice.getBounds())) {
                ice.renderBorder(shapeRenderer);
            }
        }
        for (MovingPlatform mp : levelData.movingPlatforms) {
            if (renderCuller.isVisible(mp.getBounds())) {
                mp.renderBorderInterpolated(shapeRenderer, renderInterpolationAlpha);
            }
        }
        for (Ramp ramp : levelData.ramps) {
            if (renderCuller.isVisible(ramp.getBounds())) {
                ramp.renderBorder(shapeRenderer);
            }
        }
        for (BreakableBlock bb : levelData.breakableBlocks) {
            if (!bb.isBroken() && renderCuller.isVisible(bb.getBounds())) {
                bb.renderBorder(shapeRenderer);
            }
        }
        for (Checkpoint cp : levelData.checkpoints) {
            if (renderCuller.isVisible(cp.getBounds())) {
                cp.renderBorder(shapeRenderer);
            }
        }
        for (Goal goal : levelData.goals) {
            if (renderCuller.isVisible(goal.getBounds())) {
                goal.renderBorder(shapeRenderer);
            }
        }
        
        shapeRenderer.end();
        
        // Render UI (buttons) - use separate camera for fixed UI
        renderUI();
    }
    
    private void renderUI() {
        // BULLETPROOF UI RENDERING with guaranteed matching touch areas
        // The TouchButtonManager handles all coordinate conversion automatically!
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        shapeRenderer.setProjectionMatrix(spriteBatch.getProjectionMatrix());
        
        // Render buttons using the manager - touch areas and visuals GUARANTEED to match
        inputController.getTouchButtonManager().render(shapeRenderer);
        
        // Render button icons/arrows
        inputController.getTouchButtonManager().renderIcons(shapeRenderer, 
            (renderer, button, x, y) -> {
                renderer.setColor(1f, 1f, 1f, 0.9f); // White arrows
                float size = button.getRadius() * 0.5f;
                
                switch (button.getId()) {
                    case "left":
                        drawLeftArrow(x, y, size);
                        break;
                    case "right":
                        drawRightArrow(x, y, size);
                        break;
                    case "jump":
                        drawUpArrow(x, y, size);
                        break;
                }
            }
        );
    }
    
    /**
     * Draw left arrow icon (◄)
     */
    private void drawLeftArrow(float centerX, float centerY, float size) {
        shapeRenderer.setColor(1f, 1f, 1f, 0.9f); // White arrow
        
        // Arrow pointing left (triangle)
        float tipX = centerX - size;
        float tipY = centerY;
        float baseX = centerX + size * 0.5f;
        float topY = centerY + size;
        float bottomY = centerY - size;
        
        shapeRenderer.triangle(tipX, tipY, baseX, topY, baseX, bottomY);
    }
    
    /**
     * Draw right arrow icon (►)
     */
    private void drawRightArrow(float centerX, float centerY, float size) {
        shapeRenderer.setColor(1f, 1f, 1f, 0.9f); // White arrow
        
        // Arrow pointing right (triangle)
        float tipX = centerX + size;
        float tipY = centerY;
        float baseX = centerX - size * 0.5f;
        float topY = centerY + size;
        float bottomY = centerY - size;
        
        shapeRenderer.triangle(tipX, tipY, baseX, topY, baseX, bottomY);
    }
    
    /**
     * Draw up arrow icon (▲)
     */
    private void drawUpArrow(float centerX, float centerY, float size) {
        shapeRenderer.setColor(1f, 1f, 1f, 0.95f); // Bright white arrow
        
        // Arrow pointing up (triangle)
        float tipX = centerX;
        float tipY = centerY + size;
        float leftX = centerX - size;
        float rightX = centerX + size;
        float baseY = centerY - size * 0.5f;
        
        shapeRenderer.triangle(tipX, tipY, leftX, baseY, rightX, baseY);
    }
    
    /**
     * Render victory screen with animations
     */
    private void renderVictoryScreen() {
        // Clear to dark background
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Use viewport camera for UI rendering
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        
        // Animated pulse effect
        float pulse = 0.8f + 0.2f * (float)Math.sin(levelCompleteTimer * 3);
        float fadeIn = Math.min(1.0f, levelCompleteTimer / 0.5f); // Fade in over 0.5 seconds
        
        // Draw large "LEVEL COMPLETE!" banner
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Banner background - gold with pulse (scaled proportionally)
        float bannerWidth = Constants.WORLD_WIDTH * 0.6f;  // 60% of screen width
        float bannerHeight = 180;
        float bannerX = (Constants.WORLD_WIDTH - bannerWidth) / 2;
        float bannerY = Constants.WORLD_HEIGHT / 2 + 100;
        
        shapeRenderer.setColor(1f * pulse, 0.84f * pulse, 0f, fadeIn * 0.9f);
        shapeRenderer.rect(bannerX, bannerY, bannerWidth, bannerHeight);
        
        // Star decorations around banner (animated)
        float starSize = 30 + 10 * (float)Math.sin(levelCompleteTimer * 4);
        drawStar(bannerX - 80, bannerY + bannerHeight / 2, starSize, fadeIn);
        drawStar(bannerX + bannerWidth + 50, bannerY + bannerHeight / 2, starSize, fadeIn);
        drawStar(Constants.WORLD_WIDTH / 2, bannerY + bannerHeight + 80, starSize * 0.8f, fadeIn);
        
        // Trophy/Victory icon in center
        float trophyY = Constants.WORLD_HEIGHT / 2 - 150;
        drawTrophy(Constants.WORLD_WIDTH / 2, trophyY, 80, fadeIn, pulse);
        
        // Stats box
        float statsBoxWidth = 400;
        float statsBoxHeight = 200;
        float statsX = (Constants.WORLD_WIDTH - statsBoxWidth) / 2;
        float statsY = trophyY - statsBoxHeight - 50;
        
        // Stats background
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, fadeIn * 0.8f);
        shapeRenderer.rect(statsX, statsY, statsBoxWidth, statsBoxHeight);
        
        // Stats border - animated glow
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(1f * pulse, 0.84f * pulse, 0f, fadeIn);
        shapeRenderer.rect(statsX, statsY, statsBoxWidth, statsBoxHeight);
        
        // Draw "Press any key to continue" text (using shapes)
        if (levelCompleteTimer > 1.5f) {
            float textBlink = (float)Math.sin(levelCompleteTimer * 5);
            if (textBlink > 0) {
                shapeRenderer.setColor(0.8f, 0.8f, 0.8f, fadeIn * textBlink);
                float textY = statsY - 80;
                float textWidth = 300;
                float textX = (Constants.WORLD_WIDTH - textWidth) / 2;
                shapeRenderer.rect(textX, textY, textWidth, 3);
            }
        }
        
        shapeRenderer.end();
        
        // Render filled text approximation for "LEVEL COMPLETE!"
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawLevelCompleteText(bannerX + 100, bannerY + bannerHeight / 2 + 20, fadeIn);
        shapeRenderer.end();
        
        // Performance stats (if visible)
        if (levelCompleteTimer > 0.8f) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float statsFade = Math.min(1.0f, (levelCompleteTimer - 0.8f) / 0.5f);
            drawStats(statsX + 50, statsY + 120, statsFade);
            shapeRenderer.end();
        }
    }
    
    /**
     * Draw a star shape
     */
    private void drawStar(float centerX, float centerY, float size, float alpha) {
        shapeRenderer.setColor(1f, 0.9f, 0.2f, alpha);
        int points = 5;
        float angleStep = (float)Math.PI * 2 / points;
        float innerRadius = size * 0.4f;
        float outerRadius = size;
        
        for (int i = 0; i < points; i++) {
            float angle1 = i * angleStep - (float)Math.PI / 2;
            float angle2 = (i + 0.5f) * angleStep - (float)Math.PI / 2;
            float angle3 = (i + 1) * angleStep - (float)Math.PI / 2;
            
            float x1 = centerX + outerRadius * (float)Math.cos(angle1);
            float y1 = centerY + outerRadius * (float)Math.sin(angle1);
            float x2 = centerX + innerRadius * (float)Math.cos(angle2);
            float y2 = centerY + innerRadius * (float)Math.sin(angle2);
            float x3 = centerX + outerRadius * (float)Math.cos(angle3);
            float y3 = centerY + outerRadius * (float)Math.sin(angle3);
            
            shapeRenderer.triangle(centerX, centerY, x1, y1, x2, y2);
            shapeRenderer.triangle(centerX, centerY, x2, y2, x3, y3);
        }
    }
    
    /**
     * Draw trophy icon
     */
    private void drawTrophy(float centerX, float centerY, float size, float alpha, float pulse) {
        // Cup body
        shapeRenderer.setColor(1f * pulse, 0.84f * pulse, 0f, alpha);
        shapeRenderer.rect(centerX - size * 0.4f, centerY, size * 0.8f, size * 0.6f);
        
        // Cup top
        shapeRenderer.setColor(1f * pulse, 0.9f * pulse, 0.2f, alpha);
        shapeRenderer.rect(centerX - size * 0.5f, centerY + size * 0.6f, size, size * 0.2f);
        
        // Cup base
        shapeRenderer.setColor(0.8f * pulse, 0.7f * pulse, 0f, alpha);
        shapeRenderer.rect(centerX - size * 0.5f, centerY - size * 0.2f, size, size * 0.2f);
        
        // Cup stem
        shapeRenderer.rect(centerX - size * 0.15f, centerY - size * 0.4f, size * 0.3f, size * 0.2f);
        
        // Handles
        shapeRenderer.circle(centerX - size * 0.6f, centerY + size * 0.3f, size * 0.15f);
        shapeRenderer.circle(centerX + size * 0.6f, centerY + size * 0.3f, size * 0.15f);
    }
    
    /**
     * Draw "LEVEL COMPLETE" text using rectangles
     */
    private void drawLevelCompleteText(float startX, float startY, float alpha) {
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, alpha);
        
        float charWidth = 40;
        float charHeight = 50;
        float spacing = 10;
        
        // Simplified block letters "LEVEL COMPLETE!"
        // L
        shapeRenderer.rect(startX, startY - charHeight, 8, charHeight);
        shapeRenderer.rect(startX, startY - charHeight, charWidth * 0.6f, 8);
        
        // E
        startX += charWidth + spacing;
        shapeRenderer.rect(startX, startY - charHeight, 8, charHeight);
        shapeRenderer.rect(startX, startY, charWidth * 0.6f, 8);
        shapeRenderer.rect(startX, startY - charHeight / 2, charWidth * 0.5f, 8);
        shapeRenderer.rect(startX, startY - charHeight, charWidth * 0.6f, 8);
        
        // V (simplified as rectangle)
        startX += charWidth + spacing;
        shapeRenderer.rect(startX, startY - charHeight, charWidth * 0.7f, charHeight);
        
        // E
        startX += charWidth + spacing;
        shapeRenderer.rect(startX, startY - charHeight, 8, charHeight);
        shapeRenderer.rect(startX, startY, charWidth * 0.6f, 8);
        shapeRenderer.rect(startX, startY - charHeight / 2, charWidth * 0.5f, 8);
        shapeRenderer.rect(startX, startY - charHeight, charWidth * 0.6f, 8);
        
        // L
        startX += charWidth + spacing;
        shapeRenderer.rect(startX, startY - charHeight, 8, charHeight);
        shapeRenderer.rect(startX, startY - charHeight, charWidth * 0.6f, 8);
        
        // Additional text "COMPLETE!" can be added similarly
    }
    
    /**
     * Draw stats display
     */
    private void drawStats(float x, float y, float alpha) {
        shapeRenderer.setColor(0.3f, 0.8f, 0.3f, alpha);
        
        // Checkmark icon
        shapeRenderer.rect(x, y, 15, 3);
        shapeRenderer.rect(x + 15, y, 3, 15);
        shapeRenderer.rect(x + 18, y + 15, 25, 3);
        
        // "Completed" indicator bars
        shapeRenderer.rect(x + 60, y, 200, 20);
        
        // Performance indicator
        y -= 40;
        shapeRenderer.setColor(1f, 0.84f, 0f, alpha);
        shapeRenderer.rect(x, y, 15, 15);
        shapeRenderer.rect(x + 60, y, 150, 20);
    }
    
    @Override
    public void resize(int width, int height) {
        // Update viewport - ExtendViewport automatically scales to fill window without black bars
        viewport.update(width, height, true); // true = center camera
        
        // Update touch button positions for new screen size
        inputController.resize(width, height);
        
        // Debug output
        System.out.println("Window resized to: " + width + "x" + height);
        System.out.println("Viewport adjusted to: " + camera.viewportWidth + "x" + camera.viewportHeight);
        System.out.println("World coverage: " + 
            (camera.viewportWidth / worldWidth * 100) + "% width, " +
            (camera.viewportHeight / worldHeight * 100) + "% height");
    }
    
    @Override
    public void pause() {
    }
    
    @Override
    public void resume() {
    }
    
    @Override
    public void hide() {
    }
    
    /**
     * Handle debug mode keyboard controls
     */
    private void handleDebugControls() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F3)) {
            DebugMode.setEnabled(!DebugMode.isEnabled());
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F4)) {
            DebugMode.toggleStepMode();
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F5)) {
            DebugMode.stepFrame();
        }
        
        // Log debug info when enabled
        if (DebugMode.isEnabled()) {
            DebugMode.log("Player", "Position", player.getPosition());
            DebugMode.log("Player", "Velocity", player.getVelocity());
            DebugMode.log("Player", "Grounded", player.isGrounded());
            DebugMode.log("Performance", "FPS", Gdx.graphics.getFramesPerSecond());
            DebugMode.log("Performance", "Physics ms", String.format("%.3f", physicsTimeAvgMs));
            DebugMode.log("Collisions", "Platform", platformCollisionCount);
            DebugMode.log("Collisions", "Spike", spikeCollisionCount);
        }
    }
    
    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        if (player != null) {
            player.dispose();
        }
        DebugMode.dispose();
        TextureManager.getInstance().dispose();
    }
}
