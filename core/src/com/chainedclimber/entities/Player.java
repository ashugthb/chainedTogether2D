package com.chainedclimber.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chainedclimber.utils.Constants;

public class Player {
    private Vector2 position;
    private Vector2 velocity;
    private boolean grounded;
    private Rectangle bounds;
    private float frictionMultiplier = 1.0f; // For ice blocks
    
    // Graphics
    private Texture playerTexture;
    private TextureRegion playerRegion;
    private boolean facingRight = true;
    private float animationTimer = 0f;
    
    public Player(float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.grounded = false;
        this.bounds = new Rectangle(startX, startY, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        
        // Load player sprite
        try {
            playerTexture = new Texture(Gdx.files.internal("caveman.png"));
            playerRegion = new TextureRegion(playerTexture);
        } catch (Exception e) {
            Gdx.app.error("Player", "Failed to load player texture: " + e.getMessage());
        }
    }
    
    public void update(float deltaTime) {
        // Apply gravity
        if (!grounded) {
            velocity.y -= Constants.GRAVITY * deltaTime;
            
            // Clamp fall speed
            if (velocity.y < -Constants.MAX_FALL_SPEED) {
                velocity.y = -Constants.MAX_FALL_SPEED;
            }
        }
        
        // Update position
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;
        
        // Keep player within horizontal screen bounds
        if (position.x < 0) {
            position.x = 0;
            velocity.x = 0;
        }
        if (position.x + Constants.PLAYER_WIDTH > Constants.WORLD_WIDTH) {
            position.x = Constants.WORLD_WIDTH - Constants.PLAYER_WIDTH;
            velocity.x = 0;
        }
        
        // Prevent falling below world (safety net)
        if (position.y < -100) {
            position.y = Constants.PLATFORM_HEIGHT; // Reset to ground
            velocity.y = 0;
        }
        
        // Update bounds
        bounds.setPosition(position.x, position.y);
        
        // Reset grounded state (will be set by collision check)
        grounded = false;
    }
    
    public void moveLeft() {
        velocity.x = -Constants.MOVE_SPEED;
    }
    
    public void moveRight() {
        velocity.x = Constants.MOVE_SPEED;
    }
    
    public void stopHorizontalMovement() {
        velocity.x = 0;
    }
    
    public void jump() {
        // Allow immediate jump when grounded - no delay
        if (grounded) {
            velocity.y = Constants.JUMP_VELOCITY;
            grounded = false;
        }
    }
    
    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }
    
    public void checkCollision(Platform platform) {
        Rectangle platformBounds = platform.getBounds();
        
        float playerBottom = position.y;
        float playerTop = position.y + Constants.PLAYER_HEIGHT;
        float playerLeft = position.x;
        float playerRight = position.x + Constants.PLAYER_WIDTH;
        
        float platformTop = platformBounds.y + platformBounds.height;
        float platformBottom = platformBounds.y;
        float platformLeft = platformBounds.x;
        float platformRight = platformBounds.x + platformBounds.width;
        
        // Check if player overlaps platform
        if (bounds.overlaps(platformBounds)) {
            
            // Calculate overlap on each side
            float overlapLeft = playerRight - platformLeft;
            float overlapRight = platformRight - playerLeft;
            float overlapTop = platformTop - playerBottom;
            float overlapBottom = playerTop - platformBottom;
            
            // Find the smallest overlap (that's the side we hit)
            float minOverlap = Math.min(Math.min(overlapLeft, overlapRight), 
                                       Math.min(overlapTop, overlapBottom));
            
            // Resolve collision based on smallest overlap
            if (minOverlap == overlapTop && velocity.y <= 0) {
                // Landing on top of platform
                position.y = platformTop;
                velocity.y = 0;
                grounded = true;
                bounds.setPosition(position.x, position.y);
            }
            else if (minOverlap == overlapBottom && velocity.y > 0) {
                // Hit bottom of platform (jumping into it)
                position.y = platformBottom - Constants.PLAYER_HEIGHT;
                velocity.y = 0;
                bounds.setPosition(position.x, position.y);
            }
            else if (minOverlap == overlapLeft) {
                // Hit from left side
                position.x = platformLeft - Constants.PLAYER_WIDTH;
                velocity.x = 0;
                bounds.setPosition(position.x, position.y);
            }
            else if (minOverlap == overlapRight) {
                // Hit from right side
                position.x = platformRight;
                velocity.x = 0;
                bounds.setPosition(position.x, position.y);
            }
        }
    }
    
    // Legacy rendering with shapes (fallback)
    public void render(ShapeRenderer renderer) {
        // Render player body (bright green)
        renderer.setColor(Constants.PLAYER_COLOR[0], Constants.PLAYER_COLOR[1], Constants.PLAYER_COLOR[2], 1);
        renderer.rect(position.x, position.y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        
        // Add visual indicator when grounded (darker green bar at feet)
        if (grounded) {
            renderer.setColor(0f, 0.6f, 0f, 1);
            renderer.rect(position.x, position.y, Constants.PLAYER_WIDTH, 3);
        }
    }
    
    // Sprite-based rendering
    public void renderSprite(SpriteBatch batch) {
        if (playerTexture != null && playerRegion != null) {
            // Flip sprite based on direction
            if (!playerRegion.isFlipX() && !facingRight) {
                playerRegion.flip(true, false);
            } else if (playerRegion.isFlipX() && facingRight) {
                playerRegion.flip(true, false);
            }
            
            // Draw the sprite
            batch.draw(playerRegion, position.x, position.y, 
                       Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        }
    }
    
    // Update facing direction based on movement
    public void updateDirection(float deltaTime) {
        animationTimer += deltaTime;
        
        if (velocity.x > 0) {
            facingRight = true;
        } else if (velocity.x < 0) {
            facingRight = false;
        }
    }
    
    public Vector2 getPosition() {
        return position;
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public boolean isGrounded() {
        return grounded;
    }
    
    public void setFrictionMultiplier(float multiplier) {
        this.frictionMultiplier = multiplier;
    }
    
    public void resetFriction() {
        this.frictionMultiplier = 1.0f;
    }
    
    public void bounce(float bounceVelocity) {
        velocity.y = bounceVelocity;
        grounded = false;
    }
    
    public void resetPosition(Vector2 spawnPos) {
        position.set(spawnPos);
        velocity.set(0, 0);
        grounded = false;
    }
    
    // Dispose resources
    public void dispose() {
        if (playerTexture != null) {
            playerTexture.dispose();
        }
    }
}
