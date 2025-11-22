package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.utils.BlockType;

/**
 * Ice block - Slippery platform with reduced friction
 */
public class IceBlock {
    private Rectangle bounds;
    private float[] color;
    private float frictionMultiplier = 0.3f; // Very slippery
    
    public IceBlock(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = BlockType.getColor(BlockType.ICE);
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color[0], color[1], color[2], 1);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void renderBorder(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0.5f, 0.8f, 1f, 1); // Light blue border
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public float getTop() {
        return bounds.y + bounds.height;
    }
    
    public float getFrictionMultiplier() {
        return frictionMultiplier;
    }
}
