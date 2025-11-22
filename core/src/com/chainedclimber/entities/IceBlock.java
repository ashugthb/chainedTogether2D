package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.utils.BlockType;
import com.chainedclimber.utils.TextureManager;

/**
 * Ice block - Slippery platform with reduced friction
 */
public class IceBlock {
    private Rectangle bounds;
    private float[] color;
    private float frictionMultiplier = 0.3f; // Very slippery
    private static TextureRegion iceTexture;
    
    public IceBlock(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = BlockType.getColor(BlockType.ICE);
        
        // Load texture once
        if (iceTexture == null) {
            iceTexture = TextureManager.getInstance().getTextureRegion("ice.png");
        }
    }
    
    // Texture-based rendering
    public void renderTexture(SpriteBatch batch) {
        if (iceTexture != null) {
            float tileWidth = 200f;
            float x = bounds.x;
            while (x < bounds.x + bounds.width) {
                float drawWidth = Math.min(tileWidth, bounds.x + bounds.width - x);
                batch.draw(iceTexture, x, bounds.y, drawWidth, bounds.height);
                x += tileWidth;
            }
        }
    }
    
    // Legacy rendering
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
