package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.utils.BlockType;

/**
 * Goal - Level completion trigger
 */
public class Goal {
    private Rectangle bounds;
    private float[] color;
    private float animTimer = 0;
    
    public Goal(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = BlockType.getColor(BlockType.GOAL);
    }
    
    public void update(float delta) {
        animTimer += delta;
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        // Animated glow effect
        float glow = 0.8f + 0.2f * (float)Math.sin(animTimer * 4);
        
        shapeRenderer.setColor(color[0] * glow, color[1] * glow, color[2] * glow, 1);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void renderBorder(ShapeRenderer shapeRenderer) {
        float brightness = 0.8f + 0.2f * (float)Math.sin(animTimer * 4);
        shapeRenderer.setColor(brightness, brightness, 0, 1);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public boolean checkCollision(Rectangle playerBounds) {
        return bounds.overlaps(playerBounds);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
}
