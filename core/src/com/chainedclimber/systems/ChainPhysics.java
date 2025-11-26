package com.chainedclimber.systems;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.chainedclimber.entities.Player;
import com.chainedclimber.utils.Constants;

/**
 * ChainPhysics - Handles the physical constraint between two players
 * Uses Position Based Dynamics (Verlet-style) for stability
 */
public class ChainPhysics {
    // Gameplay tuning
    private static final float MAX_CHAIN_LENGTH = 160.0f; // Max distance in pixels
    private static final int SOLVER_ITERATIONS = 4;       // Higher = stiffer chain
    private static final float CHAIN_THICKNESS = 4.0f;

    /**
     * Solve the chain constraint by modifying player positions directly
     * This pulls players together if they exceed the max chain length
     */
    public void solve(Player p1, Player p2) {
        // Iterate multiple times for stability (AAA standard)
        for (int i = 0; i < SOLVER_ITERATIONS; i++) {
            solveConstraint(p1, p2);
        }
    }

    private void solveConstraint(Player p1, Player p2) {
        Vector2 p1Pos = p1.getPosition();
        Vector2 p2Pos = p2.getPosition();
        
        // Center points (physics should act on center of mass, roughly)
        // But Player position is bottom-left. 
        // Let's apply forces to the body center for more natural rotation/pulling
        // For 2D AABB, we just modify the position directly.
        
        // 1. Get direction and distance
        // Vector from P1 to P2
        Vector2 delta = p2Pos.cpy().sub(p1Pos);
        float currentDist = delta.len();
        
        // 2. Check if chain is stretched
        if (currentDist > MAX_CHAIN_LENGTH) {
            float error = currentDist - MAX_CHAIN_LENGTH;
            
            // Normalize delta to get direction (P1 -> P2)
            Vector2 direction = delta.nor();
            
            // 3. Determine weights based on state (Inverse Mass / Weighting)
            // Scenario A: Both in Air -> Equal weight (0.5)
            float p1Weight = 0.5f;
            float p2Weight = 0.5f;
            
            boolean p1Grounded = p1.isGrounded();
            boolean p2Grounded = p2.isGrounded();
            
            // Scenario B: One Grounded (Anchor), One Jumping/Falling
            // The grounded player has friction/traction and resists the pull
            if (p1Grounded && !p2Grounded) {
                p1Weight = 0.0f; // P1 is immovable anchor (infinite mass relative to P2)
                p2Weight = 1.0f; // P2 takes all the correction
            } else if (!p1Grounded && p2Grounded) {
                p1Weight = 1.0f; // P1 takes all the correction
                p2Weight = 0.0f; // P2 is immovable anchor
            }
            
            // Scenario C: Both Grounded
            // If both are grounded, they drag each other equally (or we could check velocity)
            // For now, 0.5/0.5 is fair.
            
            // 4. Apply Positional Correction
            // Move P1 towards P2
            if (p1Weight > 0) {
                Vector2 p1Correction = direction.cpy().scl(error * p1Weight);
                p1Pos.add(p1Correction);
            }
            
            // Move P2 towards P1 (opposite direction)
            if (p2Weight > 0) {
                Vector2 p2Correction = direction.cpy().scl(-error * p2Weight);
                p2Pos.add(p2Correction);
            }
            
            // Update bounds immediately so subsequent collision checks work with new positions
            p1.getBounds().setPosition(p1Pos.x, p1Pos.y);
            p2.getBounds().setPosition(p2Pos.x, p2Pos.y);
            
            // Optional: Modify velocity to kill energy (damping)
            // This prevents the "spring" effect where they bounce back and forth
            // But for positional correction, it's less critical if we run collision right after.
        }
    }

    public void render(ShapeRenderer renderer, Player p1, Player p2) {
        // Draw chain
        renderer.setColor(0.7f, 0.7f, 0.7f, 1); // Light gray chain
        
        // Calculate center points for rendering
        float p1X = p1.getPosition().x + Constants.PLAYER_WIDTH / 2;
        float p1Y = p1.getPosition().y + Constants.PLAYER_HEIGHT / 2;
        float p2X = p2.getPosition().x + Constants.PLAYER_WIDTH / 2;
        float p2Y = p2.getPosition().y + Constants.PLAYER_HEIGHT / 2;
        
        // Draw the link
        renderer.rectLine(p1X, p1Y, p2X, p2Y, CHAIN_THICKNESS);
        
        // Draw anchor points (bolts)
        renderer.setColor(0.3f, 0.3f, 0.3f, 1);
        renderer.circle(p1X, p1Y, CHAIN_THICKNESS + 1);
        renderer.circle(p2X, p2Y, CHAIN_THICKNESS + 1);
    }
}
