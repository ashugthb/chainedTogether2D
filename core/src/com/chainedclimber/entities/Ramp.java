package com.chainedclimber.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chainedclimber.utils.BlockType;
import com.chainedclimber.utils.TextureManager;

/**
 * Ramp block - Diagonal platform for climbing
 * Supports both left and right-facing ramps
 */
public class Ramp {
    private Rectangle bounds;
    private float[] color;
    private boolean facingRight; // true = ramp goes up to the right, false = up to the left
    
    public Ramp(float x, float y, float width, float height, boolean facingRight) {
        this.bounds = new Rectangle(x, y, width, height);
        this.facingRight = facingRight;
        this.color = BlockType.getColor(facingRight ? BlockType.RAMP_RIGHT : BlockType.RAMP_LEFT);
    }
    
    /**
     * Get the height of the ramp at a given x position
     * Returns the y coordinate of the ramp surface at that x position
     */
    public float getHeightAtX(float x) {
        if (x < bounds.x || x > bounds.x + bounds.width) {
            return bounds.y; // Outside ramp bounds
        }
        
        float relativeX = x - bounds.x;
        float progress = relativeX / bounds.width;
        
        if (facingRight) {
            // Ramp goes up from left to right
            return bounds.y + (progress * bounds.height);
        } else {
            // Ramp goes up from right to left
            return bounds.y + ((1.0f - progress) * bounds.height);
        }
    }
    
    /**
     * Check if a point is inside the ramp (below the diagonal surface)
     */
    public boolean containsPoint(float x, float y) {
        if (!bounds.contains(x, y)) {
            return false;
        }
        
        float rampHeight = getHeightAtX(x);
        return y < rampHeight;
    }
    
    /**
     * Get the slope normal vector for physics calculations
     */
    public Vector2 getSlopeNormal() {
        if (facingRight) {
            return new Vector2(-1, 1).nor(); // Slope up-right
        } else {
            return new Vector2(1, 1).nor(); // Slope up-left
        }
    }
    
    /**
     * Check collision with player and adjust position properly
     * Prevents player from going inside ramp and handles all edges
     */
    public boolean checkCollision(Rectangle playerBounds, Vector2 playerPosition, Vector2 playerVelocity) {
        // Check if player overlaps with ramp bounds
        if (!playerBounds.overlaps(bounds)) {
            return false;
        }
        
        // Get player's center X and bottom
        float playerCenterX = playerBounds.x + playerBounds.width / 2;
        float playerBottom = playerBounds.y;
        float playerLeft = playerBounds.x;
        float playerRight = playerBounds.x + playerBounds.width;
        
        // Get ramp height at player's position
        float rampHeight = getHeightAtX(playerCenterX);
        
        boolean onRamp = false;
        
        // Check for vertical wall collision (solid edges)
        if (facingRight) {
            // Right-facing ramp: / - left edge is vertical wall
            float leftWallX = bounds.x;
            // Check if player is hitting the left vertical wall
            if (playerRight > leftWallX && playerLeft <= leftWallX + 2) {
                // Check if player is within the height of the wall
                if (playerBounds.y + playerBounds.height > bounds.y && 
                    playerBounds.y < bounds.y + bounds.height) {
                    // Push player out of wall
                    playerPosition.x = leftWallX - playerBounds.width;
                    if (playerVelocity != null) playerVelocity.x = 0;
                    playerBounds.x = playerPosition.x;
                    return true;
                }
            }
        } else {
            // Left-facing ramp: \\ - right edge is vertical wall
            float rightWallX = bounds.x + bounds.width;
            // Check if player is hitting the right vertical wall
            if (playerLeft < rightWallX && playerRight >= rightWallX - 2) {
                // Check if player is within the height of the wall
                if (playerBounds.y + playerBounds.height > bounds.y && 
                    playerBounds.y < bounds.y + bounds.height) {
                    // Push player out of wall
                    playerPosition.x = rightWallX;
                    if (playerVelocity != null) playerVelocity.x = 0;
                    playerBounds.x = playerPosition.x;
                    return true;
                }
            }
        }
        
        // Check if player is on the ramp surface (approaching from above)
        // Only snap to ramp if player is coming down onto it
        if (playerBottom <= rampHeight + 5 && playerBottom >= rampHeight - 15) {
            // Make sure player is within ramp bounds horizontally
            if (playerCenterX >= bounds.x && playerCenterX <= bounds.x + bounds.width) {
                // Snap player to ramp surface
                playerPosition.y = rampHeight;
                playerBounds.y = rampHeight;
                if (playerVelocity != null && playerVelocity.y < 0) {
                    playerVelocity.y = 0;
                }
                onRamp = true;
            }
        }
        
        // Prevent player from going through ramp from below
        if (playerBounds.y + playerBounds.height > rampHeight && playerBounds.y < rampHeight) {
            // Player is intersecting ramp - check if they're trying to go through from below
            if (playerVelocity != null && playerVelocity.y > 0) {
                // Coming from below - block them
                playerPosition.y = rampHeight - playerBounds.height;
                playerBounds.y = playerPosition.y;
                playerVelocity.y = 0;
                return true;
            }
        }
        
        return onRamp;
    }
    
    // Texture-based rendering
    public void renderTexture(SpriteBatch batch) {
        TextureManager tm = TextureManager.getInstance();
        TextureRegion rampTexture = facingRight ? 
            tm.getTextureRegion("ramp_right.png") : 
            tm.getTextureRegion("ramp_left.png");
        
        if (rampTexture != null) {
            batch.draw(rampTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
    
    // Legacy rendering
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(color[0], color[1], color[2], 1);
        
        // Draw triangle for ramp
        if (facingRight) {
            // Ramp going up to the right: /
            shapeRenderer.triangle(
                bounds.x, bounds.y,                           // Bottom left
                bounds.x + bounds.width, bounds.y,           // Bottom right
                bounds.x + bounds.width, bounds.y + bounds.height  // Top right
            );
        } else {
            // Ramp going up to the left: \
            shapeRenderer.triangle(
                bounds.x, bounds.y,                           // Bottom left
                bounds.x + bounds.width, bounds.y,           // Bottom right
                bounds.x, bounds.y + bounds.height           // Top left
            );
        }
    }
    
    public void renderBorder(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0.5f, 0.4f, 0.2f, 1); // Brown border
        
        // Draw triangle outline
        if (facingRight) {
            shapeRenderer.line(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
            shapeRenderer.line(bounds.x + bounds.width, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
            shapeRenderer.line(bounds.x + bounds.width, bounds.y + bounds.height, bounds.x, bounds.y);
        } else {
            shapeRenderer.line(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
            shapeRenderer.line(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
            shapeRenderer.line(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y);
        }
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public boolean isFacingRight() {
        return facingRight;
    }
    
    public float getTop() {
        return bounds.y + bounds.height;
    }
}
