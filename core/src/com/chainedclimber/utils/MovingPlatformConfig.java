package com.chainedclimber.utils;

/**
 * Configuration for moving platforms in level generation
 * Makes it easy to specify "move from column X to column Y"
 */
public class MovingPlatformConfig {
    public int row;
    public int startCol;
    public int endCol;
    public float speed;
    
    public MovingPlatformConfig(int row, int startCol, int endCol) {
        this.row = row;
        this.startCol = startCol;
        this.endCol = endCol;
        this.speed = 100f; // Default speed
    }
    
    public MovingPlatformConfig(int row, int startCol, int endCol, float speed) {
        this.row = row;
        this.startCol = startCol;
        this.endCol = endCol;
        this.speed = speed;
    }
    
    /**
     * Calculate travel distance in world units
     */
    public float getTravelDistance(float cellWidth) {
        return Math.abs(endCol - startCol) * cellWidth;
    }
    
    /**
     * Get the starting column (leftmost)
     */
    public int getStartColumn() {
        return Math.min(startCol, endCol);
    }
}
