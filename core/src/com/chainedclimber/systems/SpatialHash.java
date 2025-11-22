package com.chainedclimber.systems;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Spatial hash for ultra-fast collision detection
 * Optimized for millions of queries per frame
 */
public class SpatialHash<T> {
    private final float cellSize;
    private final ObjectMap<Long, Array<T>> cells;
    private final Array<T> queryResult;
    private final Rectangle tmpRect = new Rectangle();
    
    public SpatialHash(float cellSize) {
        this.cellSize = cellSize;
        this.cells = new ObjectMap<>(256); // Pre-allocate reasonable size
        this.queryResult = new Array<>(32); // Reusable query result
    }
    
    /**
     * Add entity to spatial hash
     */
    public void insert(T entity, Rectangle bounds) {
        int minCellX = (int)(bounds.x / cellSize);
        int minCellY = (int)(bounds.y / cellSize);
        int maxCellX = (int)((bounds.x + bounds.width) / cellSize);
        int maxCellY = (int)((bounds.y + bounds.height) / cellSize);
        
        for (int x = minCellX; x <= maxCellX; x++) {
            for (int y = minCellY; y <= maxCellY; y++) {
                long key = hashKey(x, y);
                Array<T> cell = cells.get(key);
                if (cell == null) {
                    cell = new Array<>(8);
                    cells.put(key, cell);
                }
                cell.add(entity);
            }
        }
    }
    
    /**
     * Query entities near a rectangle (reuses internal array - no allocation)
     */
    public Array<T> query(Rectangle bounds) {
        queryResult.clear();
        
        int minCellX = (int)(bounds.x / cellSize);
        int minCellY = (int)(bounds.y / cellSize);
        int maxCellX = (int)((bounds.x + bounds.width) / cellSize);
        int maxCellY = (int)((bounds.y + bounds.height) / cellSize);
        
        for (int x = minCellX; x <= maxCellX; x++) {
            for (int y = minCellY; y <= maxCellY; y++) {
                long key = hashKey(x, y);
                Array<T> cell = cells.get(key);
                if (cell != null) {
                    for (T entity : cell) {
                        if (!queryResult.contains(entity, true)) {
                            queryResult.add(entity);
                        }
                    }
                }
            }
        }
        
        return queryResult;
    }
    
    /**
     * Clear all cells for rebuilding
     */
    public void clear() {
        for (Array<T> cell : cells.values()) {
            cell.clear();
        }
    }
    
    /**
     * Fast hash function for cell coordinates
     */
    private long hashKey(int x, int y) {
        return ((long)x << 32) | (y & 0xFFFFFFFFL);
    }
}
