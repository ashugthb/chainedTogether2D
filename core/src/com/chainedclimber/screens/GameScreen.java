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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.chainedclimber.ChainedClimberGame;
import com.chainedclimber.entities.BouncyBlock;
import com.chainedclimber.entities.BreakableBlock;
import com.chainedclimber.entities.Checkpoint;
import com.chainedclimber.entities.Goal;
import com.chainedclimber.entities.IceBlock;
import com.chainedclimber.entities.MovingPlatform;
import com.chainedclimber.entities.Platform;
import com.chainedclimber.entities.Player;
import com.chainedclimber.entities.Spike;
import com.chainedclimber.systems.InputController;
import com.chainedclimber.systems.RenderCuller;
import com.chainedclimber.systems.SpatialHash;
import com.chainedclimber.utils.Constants;
import com.chainedclimber.utils.LevelData;
import com.chainedclimber.utils.LevelGenerator;
import com.chainedclimber.utils.LevelMatrix;

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
    private static final int MAX_PHYSICS_STEPS = 5; // Prevent spiral of death
    
    // Render counters for debugging
    private int renderedEntities = 0;
    private int totalEntities = 0;
    
    public GameScreen(ChainedClimberGame game) {
        this.game = game;
        
        // Setup camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        camera.position.set(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2, 0);
        camera.update();
        
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true); // Optimization: auto-switch shape types
        spriteBatch = new SpriteBatch();
        inputController = new InputController();
        
        // Initialize performance systems
        renderCuller = new RenderCuller();
        platformSpatialHash = new SpatialHash<>(Constants.WORLD_WIDTH / 8f); // 8x8 grid
        spikeSpatialHash = new SpatialHash<>(Constants.WORLD_WIDTH / 8f);
        
        // Generate level from matrix - USE LEVEL 2 with all block types!
        LevelMatrix levelMatrix = LevelGenerator.createLevel2();
        levelMatrix.printMatrix(); // Debug output
        levelData = LevelGenerator.generateAllEntities(levelMatrix);
        
        // Build spatial hashes for ultra-fast collision detection
        buildSpatialHashes();
        
        // Count total entities for performance metrics
        totalEntities = levelData.platforms.size() + levelData.spikes.size() + 
                       levelData.bouncyBlocks.size() + levelData.iceBlocks.size() +
                       levelData.movingPlatforms.size() + levelData.breakableBlocks.size() +
                       levelData.checkpoints.size() + levelData.goals.size();
        
        System.out.println("Performance: Total entities loaded: " + totalEntities);
        
        // Create player (start on ground platform at bottom-left)
        float startX = 100; // Start 100 pixels from left
        float startY = levelMatrix.getCellHeight(); // Start on ground level
        player = new Player(startX, startY);
        lastCheckpoint = new Vector2(startX, startY);
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
        if (levelComplete) {
            levelCompleteTimer += delta;
            renderVictoryScreen();
            
            // After displaying victory screen, could return to main menu
            // For now, just keep showing the victory screen
            return;
        }
        
        // Fixed timestep physics for consistent simulation across devices
        accumulatedDelta += Math.min(delta, 0.25f); // Cap delta to prevent death spiral
        
        int physicsSteps = 0;
        while (accumulatedDelta >= FIXED_TIME_STEP && physicsSteps < MAX_PHYSICS_STEPS) {
            updatePhysics(FIXED_TIME_STEP);
            accumulatedDelta -= FIXED_TIME_STEP;
            physicsSteps++;
        }
        
        // Update rendering
        renderFrame();
    }
    
    /**
     * Fixed timestep physics update for consistent simulation
     */
    private void updatePhysics(float delta) {
        // Handle input
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
        
        // Update game state
        player.update(delta);
        
        // Update moving platforms
        for (MovingPlatform mp : levelData.movingPlatforms) {
            mp.update(delta);
        }
        
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
        
        // Check collisions only with nearby platforms
        for (Platform platform : nearbyPlatforms) {
            player.checkCollision(platform);
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
        
        // Check collisions with ice blocks
        for (IceBlock ice : levelData.iceBlocks) {
            if (player.getBounds().overlaps(ice.getBounds())) {
                float overlapTop = (ice.getBounds().y + ice.getBounds().height) - player.getBounds().y;
                if (overlapTop < 10) { // Player is on top
                    player.setFrictionMultiplier(ice.getFrictionMultiplier());
                }
            }
        }
        
        // Check collisions with moving platforms
        for (MovingPlatform mp : levelData.movingPlatforms) {
            player.checkCollision(new Platform(mp.getBounds().x, mp.getBounds().y, 
                                              mp.getBounds().width, mp.getBounds().height, false));
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
        
        // OPTIMIZED: Check collisions only with nearby spikes
        for (Spike spike : nearbySpikes) {
            if (spike.checkCollision(playerBounds)) {
                // Player hit spike - respawn at last checkpoint
                player.resetPosition(lastCheckpoint);
                System.out.println("Player hit spike! Respawning at checkpoint.");
            }
        }
        
        // Check checkpoint activation
        for (Checkpoint cp : levelData.checkpoints) {
            if (!cp.isActivated() && cp.checkCollision(player.getBounds())) {
                cp.activate();
                lastCheckpoint = cp.getSpawnPosition();
                System.out.println("Checkpoint activated!");
            }
        }
        
        // Check goal completion
        for (Goal goal : levelData.goals) {
            if (goal.checkCollision(player.getBounds())) {
                levelComplete = true;
                System.out.println("LEVEL COMPLETE!");
            }
        }
        
    }
    
    /**
     * Optimized rendering with frustum culling
     */
    private void renderFrame() {
        // Update camera to follow player
        float targetCameraX = player.getPosition().x + Constants.PLAYER_WIDTH / 2;
        float targetCameraY = player.getPosition().y + Constants.PLAYER_HEIGHT / 2;
        
        // Clamp camera X position to keep it within world bounds
        if (targetCameraX < Constants.WORLD_WIDTH / 2) {
            camera.position.x = Constants.WORLD_WIDTH / 2;
        } else if (targetCameraX > Constants.WORLD_WIDTH - Constants.WORLD_WIDTH / 2) {
            camera.position.x = Constants.WORLD_WIDTH / 2;
        } else {
            camera.position.x = targetCameraX;
        }
        
        // Clamp camera Y position
        if (targetCameraY > Constants.WORLD_HEIGHT / 2) {
            camera.position.y = targetCameraY;
        } else {
            camera.position.y = Constants.WORLD_HEIGHT / 2;
        }
        
        camera.update();
        
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
                mp.render(shapeRenderer);
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
        
        // Render player with sprite
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        player.updateDirection(Gdx.graphics.getDeltaTime());
        player.renderSprite(spriteBatch);
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
                mp.renderBorder(shapeRenderer);
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
        // Set up orthographic projection for UI (no camera movement)
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw control buttons with semi-transparent gray
        
        // LEFT button - bottom-left corner
        float leftCenterX = inputController.getLeftButtonPos().x + Constants.BUTTON_SIZE / 2;
        float leftCenterY = inputController.getLeftButtonPos().y + Constants.BUTTON_SIZE / 2;
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.6f);
        shapeRenderer.circle(leftCenterX, leftCenterY, Constants.BUTTON_SIZE / 2);
        
        // RIGHT button - next to left button
        float rightCenterX = inputController.getRightButtonPos().x + Constants.BUTTON_SIZE / 2;
        float rightCenterY = inputController.getRightButtonPos().y + Constants.BUTTON_SIZE / 2;
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.6f);
        shapeRenderer.circle(rightCenterX, rightCenterY, Constants.BUTTON_SIZE / 2);
        
        // JUMP button - top-right corner
        float jumpCenterX = inputController.getJumpButtonPos().x + Constants.BUTTON_SIZE / 2;
        float jumpCenterY = inputController.getJumpButtonPos().y + Constants.BUTTON_SIZE / 2;
        shapeRenderer.setColor(0.2f, 0.7f, 0.2f, 0.6f); // Green tint for jump
        shapeRenderer.circle(jumpCenterX, jumpCenterY, Constants.BUTTON_SIZE / 2);
        
        shapeRenderer.end();
        
        // Draw button borders for better visibility
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        
        // Left button border
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.9f);
        shapeRenderer.circle(leftCenterX, leftCenterY, Constants.BUTTON_SIZE / 2);
        
        // Right button border
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.9f);
        shapeRenderer.circle(rightCenterX, rightCenterY, Constants.BUTTON_SIZE / 2);
        
        // Jump button border
        shapeRenderer.setColor(0.3f, 0.9f, 0.3f, 0.9f); // Bright green border
        shapeRenderer.circle(jumpCenterX, jumpCenterY, Constants.BUTTON_SIZE / 2);
        
        shapeRenderer.end();
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
        
        // Banner background - gold with pulse
        float bannerWidth = 800;
        float bannerHeight = 150;
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
        viewport.update(width, height);
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
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        player.dispose();
    }
}
