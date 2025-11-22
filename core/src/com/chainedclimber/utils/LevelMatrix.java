package com.chainedclimber.utils;

/**
 * Matrix-based level generator
 * Creates platforms based on a 2D grid where:
 * - 1 = Platform
 * - 0 = Air (empty space)
 * 
 * Grid is defined from top to bottom, left to right
 */
public class LevelMatrix {
    
    // Grid dimensions
    private int gridRows;
    private int gridColumns;
    
    // Cell dimensions in pixels
    private float cellWidth;
    private float cellHeight;
    
    // Level data (1 = platform, 0 = air)
    private int[][] matrix;
    
    /**
     * Create a level matrix
     * @param gridRows Number of rows (height divisions)
     * @param gridColumns Number of columns (width divisions)
     */
    public LevelMatrix(int gridRows, int gridColumns) {
        this.gridRows = gridRows;
        this.gridColumns = gridColumns;
        
        // Calculate cell dimensions based on world size
        this.cellWidth = (float) Constants.WORLD_WIDTH / gridColumns;
        this.cellHeight = (float) Constants.WORLD_HEIGHT / gridRows;
        
        // Initialize empty matrix
        this.matrix = new int[gridRows][gridColumns];
    }
    
    /**
     * Set the entire level matrix at once
     * @param matrix 2D array where 1=platform, 0=air
     *               Array goes from TOP to BOTTOM, LEFT to RIGHT
     */
    public void setMatrix(int[][] matrix) {
        if (matrix.length != gridRows) {
            throw new IllegalArgumentException("Matrix rows must be " + gridRows + ", got " + matrix.length);
        }
        
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i].length != gridColumns) {
                throw new IllegalArgumentException("Matrix columns must be " + gridColumns + 
                                                   ", got " + matrix[i].length + " at row " + i);
            }
        }
        
        // Copy matrix (deep copy)
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridColumns; col++) {
                this.matrix[row][col] = matrix[row][col];
            }
        }
    }
    
    /**
     * Set a single cell value
     * @param row Row index (0 = top)
     * @param col Column index (0 = left)
     * @param value 1 for platform, 0 for air
     */
    public void setCell(int row, int col, int value) {
        if (row < 0 || row >= gridRows || col < 0 || col >= gridColumns) {
            throw new IllegalArgumentException("Cell position out of bounds: (" + row + ", " + col + ")");
        }
        this.matrix[row][col] = value;
    }
    
    /**
     * Get cell value
     * @param row Row index (0 = top)
     * @param col Column index (0 = left)
     * @return 1 if platform, 0 if air
     */
    public int getCell(int row, int col) {
        if (row < 0 || row >= gridRows || col < 0 || col >= gridColumns) {
            return 0; // Out of bounds = air
        }
        return matrix[row][col];
    }
    
    /**
     * Convert grid coordinates to world coordinates
     * Returns the BOTTOM-LEFT corner of the cell in world space
     * 
     * @param row Row index (0 = top of screen)
     * @param col Column index (0 = left of screen)
     * @return Array [x, y] in world coordinates
     */
    public float[] gridToWorld(int row, int col) {
        float worldX = col * cellWidth;
        // Flip Y: row 0 is at top, but world Y increases upward
        float worldY = Constants.WORLD_HEIGHT - ((row + 1) * cellHeight);
        
        return new float[] { worldX, worldY };
    }
    
    /**
     * Get the width of each grid cell in pixels
     */
    public float getCellWidth() {
        return cellWidth;
    }
    
    /**
     * Get the height of each grid cell in pixels
     */
    public float getCellHeight() {
        return cellHeight;
    }
    
    /**
     * Get number of rows
     */
    public int getRows() {
        return gridRows;
    }
    
    /**
     * Get number of columns
     */
    public int getColumns() {
        return gridColumns;
    }
    
    /**
     * Get the full matrix
     */
    public int[][] getMatrix() {
        return matrix;
    }
    
    /**
     * Check if a cell contains a platform
     */
    public boolean isPlatform(int row, int col) {
        return getCell(row, col) == 1;
    }
    
    /**
     * Print matrix to console for debugging with block type visualization
     */
    public void printMatrix() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("Level Matrix (" + gridRows + "x" + gridColumns + "):");
        System.out.println("═══════════════════════════════════════");
        for (int row = 0; row < gridRows; row++) {
            System.out.print("│ ");
            for (int col = 0; col < gridColumns; col++) {
                System.out.print(BlockType.getDisplayChar(matrix[row][col]) + " ");
            }
            System.out.println("│");
        }
        System.out.println("═══════════════════════════════════════");
        
        // Print legend
        System.out.println("\nBlock Legend:");
        System.out.println("  · = Air    █ = Platform    ▲ = Spike");
        System.out.println("  ≈ = Bouncy ❄ = Ice        ○ = Coin");
        System.out.println("  ⚑ = Checkpoint  ★ = Goal");
    }
}
