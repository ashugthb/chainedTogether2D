package com.chainedclimber.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
import com.chainedclimber.systems.ChainPhysics;
import com.chainedclimber.systems.InputController;
import com.chainedclimber.systems.RenderCuller;
import com.chainedclimber.systems.SpatialHash;
import com.chainedclimber.utils.BlockType;
import com.chainedclimber.utils.Constants;
import com.chainedclimber.utils.LevelData;
import com.chainedclimber.utils.LevelGenerator;
import com.chainedclimber.utils.LevelMatrix;
import com.chainedclimber.utils.ShaderFactory;
import com.chainedclimber.utils.TextureManager;

public class GameScreen implements Screen {
    private ChainedClimberGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    
    // FPS Overlay
    private com.badlogic.gdx.graphics.g2d.BitmapFont fpsFont;
    private StringBuilder fpsText = new StringBuilder();
    
    private Player player1;
    private Player player2;
    private LevelData levelData;
    private InputController inputController;
    private ChainPhysics chainPhysics;
    private ShaderProgram grayscaleShader;
    
    // Checkpoint system
    private Vector2 lastCheckpoint;
    private boolean levelComplete = false;
    private float levelCompleteTimer = 0f;
    private static final float VICTORY_DISPLAY_TIME = 3.0f;
    
    // Performance optimizations
    private RenderCuller renderCuller;
    private SpatialHash<Platform> platformSpatialHash;
    private SpatialHash<Spike> spikeSpatialHash;
    
    // DYNAMIC HIGH PERFORMANCE: Adaptive physics timestep
    // Automatically scales to match target frame rate (60, 90, 120, 144 FPS)
    private float accumulatedDelta = 0f;
    private static final float TARGET_FPS = 90f; // Target frame rate
    private static final float FIXED_TIME_STEP = 1f / TARGET_FPS; // Dynamic physics step (~11.1ms)
    private static final int MAX_PHYSICS_STEPS = 3; // Fewer steps at higher FPS
    
    // Adaptive frame timing for smooth gameplay across all devices
    private float smoothDelta = FIXED_TIME_STEP;
    private static final float DELTA_SMOOTHING = 0.2f; // Smooth out frame time spikes

    // Lightweight profiling
    private float physicsTimeAvgMs = 0;
    private int lastPhysicsSteps = 0;
    
    // Profiling counters
    private int renderedEntities = 0;
    private int totalEntities = 0;
    private int spatialHashQueryCount = 0;
    private int platformCollisionCount = 0;
    private int spikeCollisionCount = 0;
    private int movingPlatformCollisionCount = 0;
    private int totalCollisionChecks = 0;
    private int profileFrameCounter = 0;
    
    // Moving platform state
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
        
        // Initialize FPS overlay font
        fpsFont = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        fpsFont.setColor(1, 1, 0, 1); // Yellow color
        fpsFont.getData().setScale(1.5f); // Make it bigger and readable
        
        inputController = new InputController();
        
        // Initialize texture manager and preload textures
        TextureManager.getInstance().preloadGameTextures();
        
        // Initialize performance systems
        renderCuller = new RenderCuller();
        
        // Generate level from matrix - USE LEVEL 3 with simple moving platform test!
        currentLevelMatrix = LevelGenerator.createLevel3(); // Level 3: Simple test level with one moving platform
        // currentLevelMatrix.printMatrix(); // Debug output - DISABLED to see input debug logs
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
        
        // Initialize TWO players
        player1 = new Player(spawnPoint.x - 20, spawnPoint.y, new float[]{0.2f, 0.8f, 0.2f}); // Green
        player2 = new Player(spawnPoint.x + 20, spawnPoint.y, new float[]{0.2f, 0.2f, 0.8f}); // Blue
        
        // Initialize chain physics
        chainPhysics = new ChainPhysics();
        
        // Initialize shaders
        grayscaleShader = ShaderFactory.createGrayscaleShader();

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
        
        // DYNAMIC 90 FPS: Smooth delta to eliminate micro-stutters
        // At 90 FPS, individual frame variations become very noticeable
        smoothDelta = smoothDelta * (1f - DELTA_SMOOTHING) + delta * DELTA_SMOOTHING;
        float physicsDelta = Math.min(smoothDelta, FIXED_TIME_STEP * 2f); // Cap max timestep

        // Reset collision counters for this frame
        platformCollisionCount = 0;
        spikeCollisionCount = 0;
        movingPlatformCollisionCount = 0;
        totalCollisionChecks = 0;
        spatialHashQueryCount = 0;

        long physicsStart = System.nanoTime();
        
        // Single physics update with adaptive timestep
        updatePhysics(physicsDelta);
        
        long physicsElapsedNanos = System.nanoTime() - physicsStart;

        // Update profiling averages
        float physicsMs = physicsElapsedNanos / 1_000_000f;
        physicsTimeAvgMs = physicsTimeAvgMs * 0.9f + physicsMs * 0.1f;
        lastPhysicsSteps = 1;
        
        profileFrameCounter++;
        if (profileFrameCounter % 90 == 0) { // Report every 90 frames (~1 second at 90 FPS)
            int currentFPS = Gdx.graphics.getFramesPerSecond();
            float frameTime = delta * 1000f; // milliseconds
            String status = currentFPS >= TARGET_FPS * 0.95f ? "OPTIMAL" : 
                           currentFPS >= TARGET_FPS * 0.80f ? "GOOD" : "ADJUST";
            Gdx.app.log("Perf", String.format("FPS: %d/%.0f | Frame: %.2fms | Physics: %.2fms | %s", 
                currentFPS, TARGET_FPS, frameTime, physicsTimeAvgMs, status));
        }

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
                // Update platform (Deterministic logic handles delta calculation internally)
                mp.update(delta);
            }
        }
        
        // STEP 2: Update Input
        inputController.update();
        
        // STEP 3: Apply Forces & Integrate (Predict New Positions)
        applyForcesAndIntegrate(player1, inputController.p1, delta);
        applyForcesAndIntegrate(player2, inputController.p2, delta);
        
        // STEP 4: Solve Chain Constraint (Pull them together)
        chainPhysics.solve(player1, player2);
        
        // STEP 5: Solve Map Collisions (Keep them out of walls)
        resolveCollisions(player1);
        resolveCollisions(player2);
        
        // STEP 6: Update other entities
        for (BreakableBlock bb : levelData.breakableBlocks) {
            bb.update(delta);
        }
        for (Checkpoint cp : levelData.checkpoints) {
            cp.update(delta);
        }
        for (Goal goal : levelData.goals) {
            goal.update(delta);
        }
    }

    private void applyForcesAndIntegrate(Player player, InputController.PlayerInputState inputState, float delta) {
        // Check if player is standing on a moving platform
        MovingPlatform ridingPlatform = null;
        Rectangle pBounds = player.getBounds();
        
        for (MovingPlatform mp : levelData.movingPlatforms) {
            Rectangle mpBounds = mp.getBounds();
            float playerBottom = pBounds.y;
            float platformTop = mpBounds.y + mpBounds.height;
            float verticalDistance = Math.abs(playerBottom - platformTop);
            boolean horizontalOverlap = (pBounds.x < mpBounds.x + mpBounds.width) && 
                                       (pBounds.x + pBounds.width > mpBounds.x);
            
            boolean standingOnPlatform = horizontalOverlap && 
                                        (verticalDistance <= 5) && 
                                        (player.getVelocity().y <= 10);
            
            if (standingOnPlatform) {
                ridingPlatform = mp;
                if (player.getVelocity().y <= 0) {
                    player.setGrounded(true);
                    // Apply platform velocity to position directly (before integration)
                    player.getPosition().x += mp.getLastDeltaX();
                    player.getPosition().y += mp.getLastDeltaY();
                    player.getBounds().setPosition(player.getPosition().x, player.getPosition().y);
                }
                break;
            }
        }

        // Cache grounded state
        boolean wasGroundedBeforeUpdate = player.isGrounded();
        
        // Update coyote time
        inputState.updateCoyoteTime(wasGroundedBeforeUpdate);
        
        // Horizontal movement (Input Force)
        if (inputState.isLeftPressed()) {
            player.moveLeft();
        } else if (inputState.isRightPressed()) {
            player.moveRight();
        } else {
            player.stopHorizontalMovement();
        }
        
        // Jump Logic (Apply Impulse)
        boolean wantsToJump = inputState.isJumpJustPressed();
        boolean canJump = inputState.canJump();
        
        if (wantsToJump && (wasGroundedBeforeUpdate || canJump)) {
            player.jump();
            if (ridingPlatform != null) {
                player.addMomentum(ridingPlatform.getVelocity().x);
            }
            inputState.consumeJump(wasGroundedBeforeUpdate);
        }
        
        // Integrate (Gravity + Velocity -> Position)
        player.update(delta, worldWidth);
        
        // Reset friction
        player.resetFriction();
    }

    private void resolveCollisions(Player player) {
        Rectangle playerBounds = player.getBounds();
        Array<Platform> nearbyPlatforms = platformSpatialHash.query(playerBounds);
        Array<Spike> nearbySpikes = spikeSpatialHash.query(playerBounds);
        
        spatialHashQueryCount += 2;
        
        for (Platform platform : nearbyPlatforms) {
            player.checkCollision(platform);
            platformCollisionCount++;
            totalCollisionChecks++;
        }
        
        for (BouncyBlock bouncy : levelData.bouncyBlocks) {
            if (player.getBounds().overlaps(bouncy.getBounds())) {
                float overlapBottom = (player.getBounds().y + player.getBounds().height) - bouncy.getBounds().y;
                float overlapTop = (bouncy.getBounds().y + bouncy.getBounds().height) - player.getBounds().y;
                if (overlapTop < overlapBottom && overlapTop > 0) {
                    player.bounce(bouncy.getBounceVelocity());
                }
            }
        }
        
        for (IceBlock ice : levelData.iceBlocks) {
            Platform icePlatform = new Platform(ice.getBounds().x, ice.getBounds().y,
                                               ice.getBounds().width, ice.getBounds().height, false);
            player.checkCollision(icePlatform);
            if (player.getBounds().overlaps(ice.getBounds())) {
                float overlapTop = (ice.getBounds().y + ice.getBounds().height) - player.getBounds().y;
                if (overlapTop < 10 && overlapTop > 0) {
                    player.setFrictionMultiplier(ice.getFrictionMultiplier());
                }
            }
        }
        
        for (MovingPlatform mp : levelData.movingPlatforms) {
            Rectangle mpBounds = mp.getBounds();
            Platform solidPlatform = new Platform(mpBounds.x, mpBounds.y, 
                                                 mpBounds.width, mpBounds.height, false);
            player.checkCollision(solidPlatform);
            movingPlatformCollisionCount++;
            totalCollisionChecks++;
        }
        
        for (Ramp ramp : levelData.ramps) {
            if (ramp.checkCollision(player.getBounds(), player.getPosition(), player.getVelocity())) {
                player.setGrounded(true);
            }
        }
        
        for (BreakableBlock bb : levelData.breakableBlocks) {
            if (!bb.isBroken()) {
                Platform tempPlatform = new Platform(bb.getBounds().x, bb.getBounds().y,
                                                    bb.getBounds().width, bb.getBounds().height, false);
                player.checkCollision(tempPlatform);
                float overlapTop = (bb.getBounds().y + bb.getBounds().height) - player.getBounds().y;
                if (overlapTop < 10 && overlapTop > 0) {
                    bb.setPlayerOnTop(true);
                } else {
                    bb.setPlayerOnTop(false);
                }
            }
        }
        
        for (Spike spike : nearbySpikes) {
            spikeCollisionCount++;
            totalCollisionChecks++;
            if (spike.checkCollision(playerBounds)) {
                player.resetPosition(lastCheckpoint);
            }
        }
        
        for (Checkpoint cp : levelData.checkpoints) {
            if (!cp.isActivated() && cp.checkCollision(player.getBounds())) {
                cp.activate();
                lastCheckpoint = cp.getSpawnPosition();
            }
        }
        
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
        
        // 1. Calculate center position between both players
        float playerCenterX = (player1.getPosition().x + player2.getPosition().x) / 2 + Constants.PLAYER_WIDTH / 2;
        float playerCenterY = (player1.getPosition().y + player2.getPosition().y) / 2 + Constants.PLAYER_HEIGHT / 2;
        
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
        
        // MOBILE OPTIMIZATION: Batch ALL filled shapes in ONE begin/end call
        // This reduces GPU state changes from 14 to 1 = MASSIVE performance gain
        renderedEntities = 0;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Render ALL filled shapes in a single batch
        for (Platform platform : levelData.platforms) {
            if (renderCuller.isVisible(platform.getBounds())) {
                platform.render(shapeRenderer);
                renderedEntities++;
            }
        }
        
        // Render Chain (Filled Shape)
        chainPhysics.render(shapeRenderer, player1, player2);
        
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
                // Use interpolated rendering for 144Hz smoothness
                // Calculate alpha (fraction of time between physics steps)
                // For now, we use 1.0 as we are running physics at high rate, 
                // but this supports future decoupling
                mp.renderInterpolated(shapeRenderer, 1.0f);
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
        
        // MOBILE OPTIMIZATION: Batch ALL textures in ONE begin/end call
        // Single batch = minimal GPU state changes = maximum performance
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        
        // 1. Draw Background/Level Entities (Normal)
        spriteBatch.setShader(null);

        // Render ALL textured entities in a single batch
        for (Platform platform : levelData.platforms) {
            if (renderCuller.isVisible(platform.getBounds())) {
                platform.renderTexture(spriteBatch);
            }
        }
        for (IceBlock ice : levelData.iceBlocks) {
            if (renderCuller.isVisible(ice.getBounds())) {
                ice.renderTexture(spriteBatch);
            }
        }
        for (Ramp ramp : levelData.ramps) {
            if (renderCuller.isVisible(ramp.getBounds())) {
                ramp.renderTexture(spriteBatch);
            }
        }
        for (Spike spike : levelData.spikes) {
            if (renderCuller.isVisible(spike.getBounds())) {
                spike.renderTexture(spriteBatch);
            }
        }
        
        // 2. Flush the batch to send background to GPU
        spriteBatch.flush();

        // 3. Switch to Grayscale for Character
        if (grayscaleShader != null) {
            spriteBatch.setShader(grayscaleShader);
        }

        // Player rendering
        player1.updateDirection(Gdx.graphics.getDeltaTime());
        player1.renderSprite(spriteBatch);
        player2.updateDirection(Gdx.graphics.getDeltaTime());
        player2.renderSprite(spriteBatch);
        
        // 4. Reset for next frame
        spriteBatch.setShader(null);

        spriteBatch.end();
        
        // MOBILE OPTIMIZATION: Batch ALL line rendering in ONE begin/end call
        // Set line width ONCE before loop (not per entity)
        Gdx.gl.glLineWidth(2);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        // Render ALL borders in a single batch
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
                // Use interpolated rendering for 144Hz smoothness
                mp.renderBorderInterpolated(shapeRenderer, 1.0f);
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
        
        // FPS OVERLAY (professional monitoring for high FPS gaming)
        int fps = Gdx.graphics.getFramesPerSecond();
        fpsText.setLength(0);
        fpsText.append("FPS: ").append(fps);
        
        spriteBatch.begin();
        fpsFont.draw(spriteBatch, fpsText, 10, screenHeight - 10);
        spriteBatch.end();
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
            DebugMode.log("P1", "Pos", player1.getPosition());
            DebugMode.log("P1", "Vel", player1.getVelocity());
            DebugMode.log("P1", "Grounded", player1.isGrounded());
            DebugMode.log("P2", "Pos", player2.getPosition());
            DebugMode.log("P2", "Vel", player2.getVelocity());
            DebugMode.log("P2", "Grounded", player2.isGrounded());
            DebugMode.log("Perf", "FPS", Gdx.graphics.getFramesPerSecond());
            DebugMode.log("Perf", "Phys ms", String.format("%.3f", physicsTimeAvgMs));
            DebugMode.log("Col", "Plat", platformCollisionCount);
            DebugMode.log("Col", "Spike", spikeCollisionCount);
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
        if (fpsFont != null) {
            fpsFont.dispose();
        }
        if (player1 != null) {
            player1.dispose();
        }
        if (player2 != null) {
            player2.dispose();
        }
        if (grayscaleShader != null) {
            grayscaleShader.dispose();
        }
        // Dispose textures
        TextureManager.getInstance().dispose();
    }
}
