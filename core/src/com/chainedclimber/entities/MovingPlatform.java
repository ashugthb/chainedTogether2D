package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.utils.BlockType;

/**
 * Moving platform - Moves horizontally or vertically
 */
public class MovingPlatform {
    private Rectangle bounds;
    private float[] color;
    private float startX, startY;
    private float endX, endY;
    private float speed = 80f;
    private boolean movingToEnd = true;
    
    public MovingPlatform(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = BlockType.getColor(BlockType.MOVING_PLATFORM);
        this.startX = x;
        this.startY = y;
        // Default: move 200 pixels to the right
        this.endX = x + 200f;
        this.endY = y;
    }
    
    public void update(float delta) {
        if (movingToEnd) {
            bounds.x += speed * delta;
            bounds.y += (endY - startY) * speed * delta / Math.abs(endX - startX);
            
            if (Math.abs(bounds.x - endX) < 5 && Math.abs(bounds.y - endY) < 5) {
                movingToEnd = false;
            }
        } else {
            bounds.x -= speed * delta;
            bounds.y -= (endY - startY) * speed * delta / Math.abs(endX - startX);
            
            if (Math.abs(bounds.x - startX) < 5 && Math.abs(bounds.y - startY) < 5) {
                movingToEnd = true;
            }
        }
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color[0], color[1], color[2], 1);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void renderBorder(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0.6f, 0.4f, 0, 1); // Dark orange border
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public float getTop() {
        return bounds.y + bounds.height;
    }
}
