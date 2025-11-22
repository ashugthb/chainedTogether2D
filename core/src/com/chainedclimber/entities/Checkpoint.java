package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chainedclimber.utils.BlockType;

/**
 * Checkpoint - Saves player's respawn position
 */
public class Checkpoint {
    private Rectangle bounds;
    private float[] color;
    private boolean activated = false;
    private float pulseTimer = 0;
    
    public Checkpoint(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = BlockType.getColor(BlockType.CHECKPOINT);
    }
    
    public void update(float delta) {
        pulseTimer += delta;
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        // Pulse effect when activated
        float alpha = activated ? 0.7f + 0.3f * (float)Math.sin(pulseTimer * 3) : 0.5f;
        
        shapeRenderer.setColor(color[0], color[1], color[2], alpha);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void renderBorder(ShapeRenderer shapeRenderer) {
        float brightness = activated ? 1f : 0.5f;
        shapeRenderer.setColor(0, brightness, 0, 1);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public boolean checkCollision(Rectangle playerBounds) {
        return bounds.overlaps(playerBounds);
    }
    
    public void activate() {
        activated = true;
    }
    
    public boolean isActivated() {
        return activated;
    }
    
    public Vector2 getSpawnPosition() {
        return new Vector2(bounds.x, bounds.y + bounds.height);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
}
