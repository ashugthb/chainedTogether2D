package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.utils.BlockType;

/**
 * Bouncy block - Launches player upward when landed on
 */
public class BouncyBlock {
    private Rectangle bounds;
    private float[] color;
    private float bounceVelocity = 900f; // Stronger than normal jump
    
    public BouncyBlock(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = BlockType.getColor(BlockType.BOUNCY);
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color[0], color[1], color[2], 1);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void renderBorder(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0, 0.7f, 0.7f, 1); // Darker cyan border
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public float getTop() {
        return bounds.y + bounds.height;
    }
    
    public float getBounceVelocity() {
        return bounceVelocity;
    }
}
