package com.chainedclimber.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;

/**
 * Frustum culling for rendering only visible entities
 * Critical for performance with large maps
 */
public class RenderCuller {
    private final Rectangle frustum = new Rectangle();
    private static final float MARGIN = 100f; // Extra margin for smooth rendering
    
    /**
     * Update frustum from camera (call once per frame)
     */
    public void update(OrthographicCamera camera) {
        float halfWidth = camera.viewportWidth * camera.zoom / 2f;
        float halfHeight = camera.viewportHeight * camera.zoom / 2f;
        
        frustum.x = camera.position.x - halfWidth - MARGIN;
        frustum.y = camera.position.y - halfHeight - MARGIN;
        frustum.width = camera.viewportWidth * camera.zoom + 2 * MARGIN;
        frustum.height = camera.viewportHeight * camera.zoom + 2 * MARGIN;
    }
    
    /**
     * Check if rectangle is visible in camera frustum
     */
    public boolean isVisible(Rectangle bounds) {
        return frustum.overlaps(bounds);
    }
    
    /**
     * Check if point is visible
     */
    public boolean isVisible(float x, float y) {
        return frustum.contains(x, y);
    }
    
    public Rectangle getFrustum() {
        return frustum;
    }
}
