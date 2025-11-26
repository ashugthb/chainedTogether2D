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
    private Vector2 previousPosition;
    private Vector2 velocity;
    private boolean grounded;
    private Rectangle bounds;
    private float frictionMultiplier = 1.0f; // For ice blocks
    private float momentumX = 0f; // Momentum from moving platforms
    
    // Graphics
    private Texture playerTexture;
    private TextureRegion playerRegion;
    private boolean facingRight = true;
    private float animationTimer = 0f;
    private float[] color; // Player tint color
    
    public Player(float startX, float startY, float[] color) {
        this.position = new Vector2(startX, startY);
        this.previousPosition = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.grounded = false;
        this.bounds = new Rectangle(startX, startY, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        this.color = color;
        
        // Load player sprite (PNG with transparent background)
        try {
            playerTexture = new Texture(Gdx.files.internal("caveman.png"));
            playerTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            playerRegion = new TextureRegion(playerTexture);
        } catch (Exception e) {
            Gdx.app.error("Player", "Failed to load player texture: " + e.getMessage());
        }
    }
    
    // Overload for backward compatibility (defaults to Green)
    public Player(float startX, float startY) {
        this(startX, startY, Constants.PLAYER_COLOR);
    }
    
    public void update(float deltaTime, float worldWidth) {
        // Apply gravity
        if (!grounded) {
            velocity.y -= Constants.GRAVITY * deltaTime;
            
            // Clamp fall speed
            if (velocity.y < -Constants.MAX_FALL_SPEED) {
                velocity.y = -Constants.MAX_FALL_SPEED;
            }
            
            // Apply air resistance to momentum
            if (momentumX != 0) {
                // Slow decay of momentum in air
                momentumX *= 0.98f;
                if (Math.abs(momentumX) < 10f) momentumX = 0;
            }
        } else {
            // Grounded - reset momentum immediately (friction takes over)
            momentumX = 0;
        }
        
        // Update position with velocity AND momentum
        position.x += (velocity.x + momentumX) * deltaTime;
        position.y += velocity.y * deltaTime;
        
        // Keep player within horizontal world bounds (use actual world width)
        if (position.x < 0) {
            position.x = 0;
            velocity.x = 0;
        }
        if (position.x + Constants.PLAYER_WIDTH > worldWidth) {
            position.x = worldWidth - Constants.PLAYER_WIDTH;
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

    // Save previous position for render interpolation
    public void savePreviousPosition() {
        previousPosition.set(position);
    }

    public Vector2 getPreviousPosition() {
        return previousPosition;
    }

    // Render sprite with interpolation between previous and current physics states
    public void renderSpriteInterpolated(SpriteBatch batch, float alpha) {
        if (playerTexture != null && playerRegion != null) {
            // Flip sprite based on direction
            if (!playerRegion.isFlipX() && !facingRight) {
                playerRegion.flip(true, false);
            } else if (playerRegion.isFlipX() && facingRight) {
                playerRegion.flip(true, false);
            }

            // Interpolate position
            float interpX = previousPosition.x * (1f - alpha) + position.x * alpha;
            float interpY = previousPosition.y * (1f - alpha) + position.y * alpha;

            float renderWidth = Constants.PLAYER_WIDTH;
            float renderHeight = Constants.PLAYER_HEIGHT;
            float offsetY = -4; // Slight offset to make feet touch ground better

            batch.draw(playerRegion,
                       interpX, interpY + offsetY,
                       renderWidth, renderHeight);
        }
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
    
    public void addMomentum(float x) {
        this.momentumX = x;
    }
    
    public void jump() {
        // Execute jump immediately
        // Logic for "can I jump?" (grounded, coyote time, etc.) is handled by GameScreen/InputController
        velocity.y = Constants.JUMP_VELOCITY;
        grounded = false;
    }
    
    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }
    
    public void checkCollision(Platform platform) {
        Rectangle platformBounds = platform.getBounds();
        
        // Only check collision if there's actual overlap
        if (!bounds.overlaps(platformBounds)) {
            return;
        }
        
        float playerBottom = position.y;
        float playerTop = position.y + Constants.PLAYER_HEIGHT;
        float playerLeft = position.x;
        float playerRight = position.x + Constants.PLAYER_WIDTH;
        
        float platformTop = platformBounds.y + platformBounds.height;
        float platformBottom = platformBounds.y;
        float platformLeft = platformBounds.x;
        float platformRight = platformBounds.x + platformBounds.width;
        
        // Calculate overlap on each side
        float overlapLeft = playerRight - platformLeft;
        float overlapRight = platformRight - playerLeft;
        float overlapTop = platformTop - playerBottom;
        float overlapBottom = playerTop - platformBottom;
        
        // Add small threshold to prevent micro-jitter from tiny overlaps (0.5 pixels)
        final float COLLISION_THRESHOLD = 0.5f;
        
        // Find the smallest overlap (that's the side we hit)
        float minOverlap = Math.min(Math.min(overlapLeft, overlapRight), 
                                   Math.min(overlapTop, overlapBottom));
        
        // Only resolve if overlap is significant enough
        if (minOverlap < COLLISION_THRESHOLD) {
            return;
        }
        
        // Resolve collision based on smallest overlap with velocity check
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
        else if (minOverlap == overlapLeft && velocity.x > 0) {
            // Hit from left side (moving right)
            position.x = platformLeft - Constants.PLAYER_WIDTH;
            velocity.x = 0;
            bounds.setPosition(position.x, position.y);
        }
        else if (minOverlap == overlapRight && velocity.x < 0) {
            // Hit from right side (moving left)
            position.x = platformRight;
            velocity.x = 0;
            bounds.setPosition(position.x, position.y);
        }
    }
    
    // Legacy rendering with shapes (fallback)
    public void render(ShapeRenderer renderer) {
        // Render player body (using instance color)
        renderer.setColor(color[0], color[1], color[2], 1);
        renderer.rect(position.x, position.y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        
        // Add visual indicator when grounded (darker green bar at feet)
        if (grounded) {
            renderer.setColor(0f, 0.6f, 0f, 1);
            renderer.rect(position.x, position.y, Constants.PLAYER_WIDTH, 3);
        }
    }
    
    // Sprite-based rendering with smooth scaling
    public void renderSprite(SpriteBatch batch) {
        if (playerTexture != null && playerRegion != null) {
            // Flip sprite based on direction
            if (!playerRegion.isFlipX() && !facingRight) {
                playerRegion.flip(true, false);
            } else if (playerRegion.isFlipX() && facingRight) {
                playerRegion.flip(true, false);
            }
            
            // Draw the sprite with smooth scaling and slight offset for better visuals
            // Add a small Y offset to make sprite look more grounded
            float renderWidth = Constants.PLAYER_WIDTH;
            float renderHeight = Constants.PLAYER_HEIGHT;
            float offsetY = -4; // Slight offset to make feet touch ground better
            
            // Apply player tint
            batch.setColor(color[0], color[1], color[2], 1);
            batch.draw(playerRegion, 
                       position.x, position.y + offsetY, 
                       renderWidth, renderHeight);
            batch.setColor(1, 1, 1, 1); // Reset color
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
    
    public Vector2 getVelocity() {
        return velocity;
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
