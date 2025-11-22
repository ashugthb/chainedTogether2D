package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chainedclimber.utils.BlockType;
import com.chainedclimber.utils.LevelData;

/**
 * Moving platform - Moves horizontally with proper collision detection
 */
public class MovingPlatform {
    private Rectangle bounds;
    private float[] color;
    private float startX;
    private float endX;
    private float speed = 100f; // Pixels per second
    private boolean movingRight = true;
    private float travelDistance = 200f; // Default travel distance
    private Vector2 velocity;
    
    // Store last frame's movement delta for player riding
    private float lastDeltaX = 0;
    private float lastDeltaY = 0;
    
    // Collision detection with other entities - need references to check
    private LevelData levelData;
    
    public MovingPlatform(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = BlockType.getColor(BlockType.MOVING_PLATFORM);
        this.startX = x;
        this.endX = x + travelDistance;
        this.velocity = new Vector2(speed, 0);
    }
    
    public MovingPlatform(float x, float y, float width, float height, float distance) {
        this(x, y, width, height);
        this.travelDistance = distance;
        this.endX = x + distance;
    }
    
    public void setTravelDistance(float distance) {
        this.travelDistance = distance;
        this.endX = startX + distance;
    }
    
    public void setLevelData(LevelData levelData) {
        this.levelData = levelData;
    }
    
    public void update(float delta) {
        if (levelData == null) {
            // No collision data, just move between bounds
            updateSimpleMovement(delta);
            return;
        }
        
        float oldX = bounds.x;
        
        // Update position
        if (movingRight) {
            bounds.x += speed * delta;
            velocity.x = speed;
            
            // Check if reached end
            if (bounds.x >= endX) {
                bounds.x = endX;
                movingRight = false;
                velocity.x = -speed;
            }
        } else {
            bounds.x -= speed * delta;
            velocity.x = -speed;
            
            // Check if reached start
            if (bounds.x <= startX) {
                bounds.x = startX;
                movingRight = true;
                velocity.x = speed;
            }
        }
        
        // Check collision with all solid objects
        boolean hasCollision = false;
        
        // Check collision with static platforms
        for (Platform platform : levelData.platforms) {
            if (checkCollision(platform.getBounds())) {
                hasCollision = true;
                break;
            }
        }
        
        // Check collision with ice blocks (solid)
        if (!hasCollision) {
            for (IceBlock ice : levelData.iceBlocks) {
                if (checkCollision(ice.getBounds())) {
                    hasCollision = true;
                    break;
                }
            }
        }
        
        // Check collision with ramps (check bounds)
        if (!hasCollision) {
            for (Ramp ramp : levelData.ramps) {
                if (checkCollision(ramp.getBounds())) {
                    hasCollision = true;
                    break;
                }
            }
        }
        
        // Check collision with other moving platforms
        if (!hasCollision) {
            for (MovingPlatform other : levelData.movingPlatforms) {
                if (other != this && checkCollision(other.getBounds())) {
                    hasCollision = true;
                    break;
                }
            }
        }
        
        // If collision detected, reverse direction
        if (hasCollision) {
            bounds.x = oldX;
            movingRight = !movingRight;
            velocity.x = -velocity.x;
        }
    }
    
    private void updateSimpleMovement(float delta) {
        // Simple movement without collision detection
        if (movingRight) {
            bounds.x += speed * delta;
            velocity.x = speed;
            if (bounds.x >= endX) {
                bounds.x = endX;
                movingRight = false;
                velocity.x = -speed;
            }
        } else {
            bounds.x -= speed * delta;
            velocity.x = -speed;
            if (bounds.x <= startX) {
                bounds.x = startX;
                movingRight = true;
                velocity.x = speed;
            }
        }
    }
    
    private boolean checkCollision(Rectangle otherBounds) {
        return bounds.overlaps(otherBounds);
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
    
    public Vector2 getVelocity() {
        return velocity;
    }
    
    public void setLastDelta(float deltaX, float deltaY) {
        this.lastDeltaX = deltaX;
        this.lastDeltaY = deltaY;
    }
    
    public float getLastDeltaX() {
        return lastDeltaX;
    }
    
    public float getLastDeltaY() {
        return lastDeltaY;
    }
    
    public boolean isPlayerOnTop(Rectangle playerBounds) {
        // Check if player is standing on top of this platform
        float overlapBottom = (playerBounds.y + playerBounds.height) - bounds.y;
        float overlapTop = (bounds.y + bounds.height) - playerBounds.y;
        
        return playerBounds.overlaps(bounds) && overlapTop > overlapBottom && overlapBottom < 10;
    }
}
