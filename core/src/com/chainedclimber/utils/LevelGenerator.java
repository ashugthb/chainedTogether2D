package com.chainedclimber.utils;

import java.util.ArrayList;
import java.util.List;

import com.chainedclimber.entities.BouncyBlock;
import com.chainedclimber.entities.BreakableBlock;
import com.chainedclimber.entities.Checkpoint;
import com.chainedclimber.entities.Goal;
import com.chainedclimber.entities.IceBlock;
import com.chainedclimber.entities.MovingPlatform;
import com.chainedclimber.entities.Platform;
import com.chainedclimber.entities.Ramp;
import com.chainedclimber.entities.Spike;

/**
 * Generates all entities from a level matrix
 * Optimizes by merging adjacent horizontal platforms into single platforms
 */
public class LevelGenerator {
    
    /**
     * Generate all entities from a level matrix
     * Creates platforms, spikes, bouncy blocks, ice, moving platforms, etc.
     * 
     * @param matrix The level matrix with block type data
     * @return LevelData containing all entity lists
     */
    public static LevelData generateAllEntities(LevelMatrix matrix) {
        LevelData levelData = new LevelData();
        
        int rows = matrix.getRows();
        int cols = matrix.getColumns();
        float cellWidth = matrix.getCellWidth();
        float cellHeight = matrix.getCellHeight();
        
        // Process each cell in the matrix
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int blockType = matrix.getCell(row, col);
                
                if (blockType == BlockType.AIR) {
                    continue; // Skip empty cells
                }
                
                float[] worldPos = matrix.gridToWorld(row, col);
                float x = worldPos[0];
                float y = worldPos[1];
                
                // Check if this is ground row (bottom row)
                boolean isGround = (row == rows - 1);
                
                // Create appropriate entity based on block type
                switch (blockType) {
                    case BlockType.PLATFORM:
                        levelData.platforms.add(new Platform(x, y, cellWidth, cellHeight, isGround));
                        break;
                    case BlockType.SPIKE:
                        levelData.spikes.add(new Spike(x, y, cellWidth, cellHeight));
                        break;
                    case BlockType.BOUNCY:
                        levelData.bouncyBlocks.add(new BouncyBlock(x, y, cellWidth, cellHeight));
                        break;
                    case BlockType.ICE:
                        levelData.iceBlocks.add(new IceBlock(x, y, cellWidth, cellHeight));
                        break;
                    case BlockType.MOVING_PLATFORM:
                        // Moving platform - will be configured with paths later
                        MovingPlatform mp = new MovingPlatform(x, y, cellWidth, cellHeight);
                        levelData.movingPlatforms.add(mp);
                        break;
                    case BlockType.BREAKABLE:
                        levelData.breakableBlocks.add(new BreakableBlock(x, y, cellWidth, cellHeight));
                        break;
                    case BlockType.CHECKPOINT:
                        levelData.checkpoints.add(new Checkpoint(x, y, cellWidth, cellHeight));
                        break;
                    case BlockType.GOAL:
                        levelData.goals.add(new Goal(x, y, cellWidth, cellHeight));
                        break;
                    case BlockType.RAMP_RIGHT:
                        levelData.ramps.add(new Ramp(x, y, cellWidth, cellHeight, true));
                        break;
                    case BlockType.RAMP_LEFT:
                        levelData.ramps.add(new Ramp(x, y, cellWidth, cellHeight, false));
                        break;
                }
            }
        }
        
        // Configure moving platform paths if specified
        List<MovingPlatformConfig> mpConfigs = matrix.getMovingPlatformPaths();
        if (!mpConfigs.isEmpty() && !levelData.movingPlatforms.isEmpty()) {
            configureMovingPlatforms(levelData, mpConfigs, cellWidth);
        }
        
        return levelData;
    }
    
    /**
     * Configure moving platforms with their travel distances
     * Matches moving platforms to their path configurations
     */
    private static void configureMovingPlatforms(LevelData levelData, 
                                                 List<MovingPlatformConfig> configs,
                                                 float cellWidth) {
        int platformIndex = 0;
        for (MovingPlatformConfig config : configs) {
            if (platformIndex >= levelData.movingPlatforms.size()) {
                break; // No more platforms to configure
            }
            
            MovingPlatform platform = levelData.movingPlatforms.get(platformIndex);
            float travelDistance = config.getTravelDistance(cellWidth);
            platform.setTravelDistance(travelDistance);
            
            platformIndex++;
        }
    }
    
    /**
     * Generate platforms from a level matrix (legacy method)
     * Automatically merges adjacent horizontal platforms for optimization
     * 
     * @param matrix The level matrix with platform data
     * @return List of platform entities
     */
    public static List<Platform> generateFromMatrix(LevelMatrix matrix) {
        List<Platform> platforms = new ArrayList<>();
        
        int rows = matrix.getRows();
        int cols = matrix.getColumns();
        float cellWidth = matrix.getCellWidth();
        float cellHeight = matrix.getCellHeight();
        
        // Process each row
        for (int row = 0; row < rows; row++) {
            int startCol = -1;
            
            // Scan columns in this row
            for (int col = 0; col <= cols; col++) {
                boolean isPlatform = (col < cols) && matrix.isPlatform(row, col);
                
                if (isPlatform && startCol == -1) {
                    // Start of a platform segment
                    startCol = col;
                }
                else if (!isPlatform && startCol != -1) {
                    // End of a platform segment - create platform
                    float[] worldPos = matrix.gridToWorld(row, startCol);
                    float platformWidth = (col - startCol) * cellWidth;
                    
                    // Determine if this is ground (bottom row)
                    boolean isGround = (row == rows - 1);
                    
                    platforms.add(new Platform(
                        worldPos[0],           // x position
                        worldPos[1],           // y position
                        platformWidth,         // width (merged segments)
                        cellHeight,            // height
                        isGround               // is ground platform
                    ));
                    
                    startCol = -1;
                }
            }
        }
        
        return platforms;
    }
    
    /**
     * Generate platforms without merging (one platform per cell)
     * Use this if you need individual platform collision handling
     * 
     * @param matrix The level matrix with platform data
     * @return List of platform entities (one per cell)
     */
    public static List<Platform> generateUnmerged(LevelMatrix matrix) {
        List<Platform> platforms = new ArrayList<>();
        
        int rows = matrix.getRows();
        int cols = matrix.getColumns();
        float cellWidth = matrix.getCellWidth();
        float cellHeight = matrix.getCellHeight();
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (matrix.isPlatform(row, col)) {
                    float[] worldPos = matrix.gridToWorld(row, col);
                    boolean isGround = (row == rows - 1);
                    
                    platforms.add(new Platform(
                        worldPos[0],
                        worldPos[1],
                        cellWidth,
                        cellHeight,
                        isGround
                    ));
                }
            }
        }
        
        return platforms;
    }
    
    /**
     * Create a simple test level matrix
     * 10 rows x 16 columns
     */
    public static LevelMatrix createTestLevel() {
        LevelMatrix level = new LevelMatrix(10, 16);
        
        // Define level (1 = platform, 0 = air)
        int[][] design = {
            // Row 0 (top)
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
            {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1},
            {0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}  // Row 9 (ground)
        };
        
        level.setMatrix(design);
        return level;
    }
    
    /**
     * Create level 1 - Simple climbing challenge
     */
    public static LevelMatrix createLevel1() {
        LevelMatrix level = new LevelMatrix(10, 16);
        
        int[][] design = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
        
        level.setMatrix(design);
        return level;
    }
    
    /**
     * Create level 2 - Demonstrates different block types
     * Uses BlockType constants for clarity
     */
    public static LevelMatrix createLevel2() {
        LevelMatrix level = new LevelMatrix(10, 16);
        
        // Shorthand for block types
        int A = BlockType.AIR;
        int P = BlockType.PLATFORM;
        int S = BlockType.SPIKE;
        int B = BlockType.BOUNCY;
        int I = BlockType.ICE;
        int C = BlockType.COIN;
        int G = BlockType.GOAL;
        int K = BlockType.CHECKPOINT;
        
        int[][] design = {
        // 16 Columns (0-15)
        {A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A}, // Row 0
        {A, G, A, A, A, A, A, A, A, A, A, A, A, A, A, A}, // Row 1: Goal Top Left
        {P, P, P, A, A, A, A, A, A, A, A, A, C, A, C, A}, // Row 2: Goal Platform
        {A, A, A, A, A, A, I, I, I, P, A, A, P, A, P, A}, // Row 3: Ice approach
        {A, A, A, A, A, A, A, A, A, A, A, S, P, S, P, S}, // Row 4: Spikes under jumps
        {A, A, A, A, K, A, A, A, A, A, P, P, P, P, P, P}, // Row 5: Checkpoint
        {A, A, A, P, P, P, A, A, A, A, A, A, A, A, A, A}, // Row 6
        {A, C, A, A, A, A, A, B, A, A, A, A, A, A, A, A}, // Row 7: Bouncy helper
        {P, P, A, S, S, S, A, P, A, S, S, A, A, A, A, A}, // Row 8: Hazard floor
        {P, P, P, P, P, P, P, P, P, P, P, P, P, P, P, P}  // Row 9: Base Floor
    };
        
        level.setMatrix(design);
        return level;
    }
    
    /**
     * Create level 3 - Moving Platforms Showcase
     * Demonstrates easy configuration: "platform moves from column X to column Y"
     * @ marks the spawn point in the matrix
     */
    public static LevelMatrix createLevel3() {
        LevelMatrix level = new LevelMatrix(10, 16);
        
        // SIMPLE TEST LEVEL - Just one moving platform to debug
        int A = BlockType.AIR;
        int P = BlockType.PLATFORM;
        int M = BlockType.MOVING_PLATFORM;
        int X = BlockType.SPAWN_POINT;
        
        int[][] design = {
    // Cols: 0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    /* R0 */ {A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A},
    /* R1 */ {A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A},
    /* R2 */ {A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A},
    /* R3 */ {A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A},
    /* R4 */ {A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A},
    /* R5 */ {A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A},
    /* R6 */ {A, A, A, X, A, A, A, A, A, A, A, A, A, A, A, A}, // Spawn ABOVE platform (player will fall onto it)
    /* R7 */ {A, A, A, M, A, A, A, A, A, A, A, A, A, A, A, A}, // Moving platform
    /* R8 */ {A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A},
    /* R9 */ {P, P, P, P, P, P, P, P, P, P, P, P, P, P, P, P}  // Ground
    };
    
    level.setMatrix(design);
    
    // Add moving platform path: oscillates horizontally between columns 0 and 4
    level.addMovingPlatformPath(7, 0, 4);
    
    return level;
    }
}
