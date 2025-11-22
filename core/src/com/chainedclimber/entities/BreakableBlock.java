package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.utils.BlockType;

/**
 * Breakable block - Disappears after player lands on it once
 */
public class BreakableBlock {
    private Rectangle bounds;
    private float[] color;
    private boolean broken = false;
    private float breakTimer = 0;
    private float breakDelay = 0.5f; // Time before breaking
    private boolean playerOnTop = false;
    
    public BreakableBlock(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = BlockType.getColor(BlockType.BREAKABLE);
    }
    
    public void update(float delta) {
        if (playerOnTop && !broken) {
            breakTimer += delta;
            if (breakTimer >= breakDelay) {
                broken = true;
            }
        }
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        if (broken) return;
        
        // Flash effect when about to break
        float alpha = 1f;
        if (breakTimer > 0) {
            alpha = 0.5f + 0.5f * (float)Math.sin(breakTimer * 20);
        }
        
        shapeRenderer.setColor(color[0], color[1], color[2], alpha);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void renderBorder(ShapeRenderer shapeRenderer) {
        if (broken) return;
        
        shapeRenderer.setColor(0.6f, 0.4f, 0.2f, 1); // Brown border
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void setPlayerOnTop(boolean onTop) {
        this.playerOnTop = onTop;
        if (!onTop) {
            breakTimer = 0;
        }
    }
    
    public boolean isBroken() {
        return broken;
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public float getTop() {
        return bounds.y + bounds.height;
    }
}
