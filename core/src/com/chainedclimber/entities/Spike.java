package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.utils.BlockType;
import com.chainedclimber.utils.TextureManager;

/**
 * Spike block - Hazard that kills/damages player on touch
 */
public class Spike {
    private Rectangle bounds;
    private float[] color;
    private static TextureRegion spikeTexture;
    
    public Spike(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = BlockType.getColor(BlockType.SPIKE);
        
        // Load texture once
        if (spikeTexture == null) {
            spikeTexture = TextureManager.getInstance().getTextureRegion("spike.png");
        }
    }
    
    // Texture-based rendering
    public void renderTexture(SpriteBatch batch) {
        if (spikeTexture != null) {
            batch.draw(spikeTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
    
    // Legacy rendering
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color[0], color[1], color[2], 1);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void renderBorder(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0.6f, 0, 0, 1); // Dark red border
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public boolean checkCollision(Rectangle playerBounds) {
        return bounds.overlaps(playerBounds);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
}
