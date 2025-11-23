package com.chainedclimber.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.chainedclimber.utils.Constants;
import com.chainedclimber.utils.LevelMatrix;

/**
 * Performance overlay that displays FPS, physics timing, collision counts, and map matrix
 * Designed with a clean, professional layout similar to debug overlays in production games
 */
public class PerformanceOverlay {
    private BitmapFont font;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;
    
    // Performance metrics
    private float currentFPS = 0f;
    private float physicsMs = 0f;
    private int physicsSteps = 0;
    private int platformCollisions = 0;
    private int spikeCollisions = 0;
    private int movingPlatformCollisions = 0;
    private int totalCollisionChecks = 0;
    private int spatialHashQueries = 0;
    private int renderedEntities = 0;
    private int totalEntities = 0;
    
    // Map matrix reference
    private LevelMatrix levelMatrix;
    
    // Layout constants
    private static final float PADDING = 10f;
    private static final float LINE_HEIGHT = 20f;
    private static final float SECTION_SPACING = 30f;
    private static final float OVERLAY_X = 10f;
    private static final float OVERLAY_Y_TOP = Constants.WORLD_HEIGHT - 10f;
    
    // Colors
    private static final Color BG_COLOR = new Color(0.05f, 0.05f, 0.1f, 0.85f);
    private static final Color BORDER_COLOR = new Color(0.2f, 0.6f, 0.9f, 0.9f);
    private static final Color HEADER_COLOR = new Color(0.3f, 0.8f, 1f, 1f);
    private static final Color TEXT_COLOR = new Color(0.9f, 0.9f, 0.95f, 1f);
    private static final Color VALUE_GOOD = new Color(0.3f, 1f, 0.3f, 1f);
    private static final Color VALUE_WARNING = new Color(1f, 0.8f, 0.2f, 1f);
    private static final Color VALUE_BAD = new Color(1f, 0.3f, 0.3f, 1f);
    
    public PerformanceOverlay(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.font = new BitmapFont();
        this.font.setColor(TEXT_COLOR);
        this.font.getData().setScale(1.2f); // Slightly larger for readability
        this.layout = new GlyphLayout();
    }
    
    public void setLevelMatrix(LevelMatrix matrix) {
        this.levelMatrix = matrix;
    }
    
    /**
     * Update performance metrics
     */
    public void updateMetrics(float fps, float physicsMs, int physicsSteps,
                             int platformCollisions, int spikeCollisions, 
                             int movingPlatformCollisions, int totalCollisionChecks,
                             int spatialHashQueries, int renderedEntities, int totalEntities) {
        this.currentFPS = fps;
        this.physicsMs = physicsMs;
        this.physicsSteps = physicsSteps;
        this.platformCollisions = platformCollisions;
        this.spikeCollisions = spikeCollisions;
        this.movingPlatformCollisions = movingPlatformCollisions;
        this.totalCollisionChecks = totalCollisionChecks;
        this.spatialHashQueries = spatialHashQueries;
        this.renderedEntities = renderedEntities;
        this.totalEntities = totalEntities;
    }
    
    /**
     * Render the performance overlay
     */
    public void render(SpriteBatch batch) {
        float y = OVERLAY_Y_TOP;
        
        // Calculate overlay dimensions
        float maxWidth = 350f;
        float matrixHeight = levelMatrix != null ? calculateMatrixHeight() : 0f;
        float metricsHeight = 10 * LINE_HEIGHT + 3 * SECTION_SPACING + PADDING * 2;
        float totalHeight = metricsHeight + matrixHeight + (matrixHeight > 0 ? SECTION_SPACING : 0);
        
        // Draw background panel
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(BG_COLOR);
        shapeRenderer.rect(OVERLAY_X, y - totalHeight, maxWidth, totalHeight);
        shapeRenderer.end();
        
        // Draw border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(OVERLAY_X, y - totalHeight, maxWidth, totalHeight);
        shapeRenderer.end();
        
        // Start text rendering
        batch.begin();
        
        y -= PADDING + LINE_HEIGHT;
        
        // === SECTION 1: FPS & Frame Timing ===
        font.setColor(HEADER_COLOR);
        font.draw(batch, "=== PERFORMANCE ===", OVERLAY_X + PADDING, y);
        y -= LINE_HEIGHT;
        
        // FPS with color coding
        font.setColor(TEXT_COLOR);
        font.draw(batch, "FPS:", OVERLAY_X + PADDING, y);
        font.setColor(getFPSColor(currentFPS));
        font.draw(batch, String.format("%.1f", currentFPS), OVERLAY_X + PADDING + 120, y);
        y -= LINE_HEIGHT;
        
        // Physics timing
        font.setColor(TEXT_COLOR);
        font.draw(batch, "Physics:", OVERLAY_X + PADDING, y);
        font.setColor(getPhysicsColor(physicsMs));
        font.draw(batch, String.format("%.3f ms", physicsMs), OVERLAY_X + PADDING + 120, y);
        y -= LINE_HEIGHT;
        
        // Physics steps per frame
        font.setColor(TEXT_COLOR);
        font.draw(batch, "Steps/Frame:", OVERLAY_X + PADDING, y);
        font.setColor(physicsSteps > 1 ? VALUE_WARNING : VALUE_GOOD);
        font.draw(batch, String.valueOf(physicsSteps), OVERLAY_X + PADDING + 120, y);
        y -= SECTION_SPACING;
        
        // === SECTION 2: Collision Stats ===
        font.setColor(HEADER_COLOR);
        font.draw(batch, "=== COLLISIONS ===", OVERLAY_X + PADDING, y);
        y -= LINE_HEIGHT;
        
        font.setColor(TEXT_COLOR);
        font.draw(batch, "Platform:", OVERLAY_X + PADDING, y);
        font.setColor(VALUE_GOOD);
        font.draw(batch, String.valueOf(platformCollisions), OVERLAY_X + PADDING + 120, y);
        y -= LINE_HEIGHT;
        
        font.setColor(TEXT_COLOR);
        font.draw(batch, "Spike:", OVERLAY_X + PADDING, y);
        font.setColor(VALUE_GOOD);
        font.draw(batch, String.valueOf(spikeCollisions), OVERLAY_X + PADDING + 120, y);
        y -= LINE_HEIGHT;
        
        font.setColor(TEXT_COLOR);
        font.draw(batch, "Moving Plat:", OVERLAY_X + PADDING, y);
        font.setColor(VALUE_GOOD);
        font.draw(batch, String.valueOf(movingPlatformCollisions), OVERLAY_X + PADDING + 120, y);
        y -= LINE_HEIGHT;
        
        font.setColor(TEXT_COLOR);
        font.draw(batch, "Total Checks:", OVERLAY_X + PADDING, y);
        font.setColor(TEXT_COLOR);
        font.draw(batch, String.valueOf(totalCollisionChecks), OVERLAY_X + PADDING + 120, y);
        y -= LINE_HEIGHT;
        
        font.setColor(TEXT_COLOR);
        font.draw(batch, "Hash Queries:", OVERLAY_X + PADDING, y);
        font.setColor(VALUE_GOOD);
        font.draw(batch, String.valueOf(spatialHashQueries), OVERLAY_X + PADDING + 120, y);
        y -= SECTION_SPACING;
        
        // === SECTION 3: Render Stats ===
        font.setColor(HEADER_COLOR);
        font.draw(batch, "=== RENDERING ===", OVERLAY_X + PADDING, y);
        y -= LINE_HEIGHT;
        
        font.setColor(TEXT_COLOR);
        font.draw(batch, "Visible:", OVERLAY_X + PADDING, y);
        font.setColor(VALUE_GOOD);
        font.draw(batch, String.format("%d / %d", renderedEntities, totalEntities), OVERLAY_X + PADDING + 120, y);
        y -= LINE_HEIGHT;
        
        float cullPercent = totalEntities > 0 ? (1f - (float)renderedEntities / totalEntities) * 100f : 0f;
        font.setColor(TEXT_COLOR);
        font.draw(batch, "Culled:", OVERLAY_X + PADDING, y);
        font.setColor(cullPercent > 30 ? VALUE_GOOD : VALUE_WARNING);
        font.draw(batch, String.format("%.1f%%", cullPercent), OVERLAY_X + PADDING + 120, y);
        y -= SECTION_SPACING;
        
        // === SECTION 4: Map Matrix (Mini-map style) ===
        if (levelMatrix != null) {
            font.setColor(HEADER_COLOR);
            font.draw(batch, "=== MAP MATRIX ===", OVERLAY_X + PADDING, y);
            y -= LINE_HEIGHT;
            
            renderMiniMap(batch, OVERLAY_X + PADDING, y);
        }
        
        batch.end();
    }
    
    /**
     * Render a compact mini-map representation of the level matrix
     */
    private void renderMiniMap(SpriteBatch batch, float startX, float startY) {
        int rows = levelMatrix.getRows();
        int cols = levelMatrix.getColumns();
        
        // Calculate cell size to fit in overlay
        float maxMapWidth = 320f;
        float cellSize = Math.min(maxMapWidth / cols, 12f); // Max 12px per cell
        
        float y = startY;
        
        batch.end(); // End text batch to draw shapes
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int row = 0; row < rows; row++) {
            float x = startX;
            for (int col = 0; col < cols; col++) {
                Color cellColor = getBlockColor(levelMatrix.getCell(row, col));
                shapeRenderer.setColor(cellColor);
                shapeRenderer.rect(x, y - cellSize, cellSize - 1, cellSize - 1);
                x += cellSize;
            }
            y -= cellSize;
        }
        
        shapeRenderer.end();
        
        // Draw mini-map border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(startX, startY - (cellSize * rows), cellSize * cols, cellSize * rows);
        shapeRenderer.end();
        
        batch.begin(); // Resume text batch
    }
    
    /**
     * Calculate height needed for matrix display
     */
    private float calculateMatrixHeight() {
        int rows = levelMatrix.getRows();
        int cols = levelMatrix.getColumns();
        float maxMapWidth = 320f;
        float cellSize = Math.min(maxMapWidth / cols, 12f);
        return (cellSize * rows) + LINE_HEIGHT + PADDING;
    }
    
    /**
     * Get color based on FPS performance
     */
    private Color getFPSColor(float fps) {
        if (fps >= 55) return VALUE_GOOD;
        if (fps >= 30) return VALUE_WARNING;
        return VALUE_BAD;
    }
    
    /**
     * Get color based on physics timing
     */
    private Color getPhysicsColor(float ms) {
        if (ms < 5.0f) return VALUE_GOOD;
        if (ms < 10.0f) return VALUE_WARNING;
        return VALUE_BAD;
    }
    
    /**
     * Get color for block type in mini-map
     */
    private Color getBlockColor(int blockType) {
        switch (blockType) {
            case 0: // AIR
                return new Color(0.1f, 0.1f, 0.15f, 1f);
            case 1: // PLATFORM
                return new Color(0.4f, 0.3f, 0.2f, 1f);
            case 2: // SPIKE
                return new Color(1f, 0.2f, 0.2f, 1f);
            case 3: // BOUNCY
                return new Color(1f, 0.5f, 1f, 1f);
            case 4: // ICE
                return new Color(0.5f, 0.8f, 1f, 1f);
            case 5: // COIN
                return new Color(1f, 0.9f, 0.2f, 1f);
            case 6: // MOVING_PLATFORM
                return new Color(1f, 0.6f, 0.2f, 1f);
            case 7: // SPAWN_POINT
                return new Color(0.3f, 1f, 0.3f, 1f);
            case 8: // CHECKPOINT
                return new Color(0.2f, 0.6f, 1f, 1f);
            case 9: // GOAL
                return new Color(1f, 0.8f, 0.1f, 1f);
            case 10: // BREAKABLE
                return new Color(0.6f, 0.4f, 0.3f, 1f);
            case 11: // RAMP_RIGHT
            case 12: // RAMP_LEFT
                return new Color(0.5f, 0.4f, 0.3f, 1f);
            default:
                return new Color(0.5f, 0.5f, 0.5f, 1f);
        }
    }
    
    /**
     * Dispose resources
     */
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
    }
}
