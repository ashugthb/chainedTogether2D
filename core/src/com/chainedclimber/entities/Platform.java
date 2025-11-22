package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.utils.Constants;

public class Platform {
    private Rectangle bounds;
    private boolean isGround;
    
    public Platform(float x, float y, float width, float height, boolean isGround) {
        this.bounds = new Rectangle(x, y, width, height);
        this.isGround = isGround;
    }
    
    public void render(ShapeRenderer renderer) {
        // Draw platform fill
        if (isGround) {
            renderer.setColor(Constants.GROUND_COLOR[0], Constants.GROUND_COLOR[1], Constants.GROUND_COLOR[2], 1);
        } else {
            renderer.setColor(Constants.PLATFORM_COLOR[0], Constants.PLATFORM_COLOR[1], Constants.PLATFORM_COLOR[2], 1);
        }
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void renderBorder(ShapeRenderer renderer) {
        // Draw platform border for visibility
        if (isGround) {
            renderer.setColor(0.5f, 0.5f, 0.5f, 1); // Lighter border for ground
        } else {
            renderer.setColor(0.7f, 0.7f, 0.7f, 1); // Light gray border
        }
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public float getTop() {
        return bounds.y + bounds.height;
    }
}
