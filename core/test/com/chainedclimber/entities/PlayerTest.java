package com.chainedclimber.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.chainedclimber.GdxTestRunner;
import com.chainedclimber.TestResultAnalyzer;
import com.chainedclimber.utils.Constants;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for Player entity.
 * Tests cover:
 * - Position and bounds management
 * - Movement (left, right, jump)
 * - Velocity and acceleration
 * - Collision detection with platforms
 * - Grounded state management
 * - Boundary constraints
 * - Edge cases and error conditions
 */
@ExtendWith(GdxTestRunner.class)
@ExtendWith(TestResultAnalyzer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Player Entity Tests")
public class PlayerTest {
    
    private Player player;
    private static final float TEST_X = 100f;
    private static final float TEST_Y = 200f;
    private static final float DELTA_TIME = 1/60f; // 60 FPS
    private static final float EPSILON = 0.001f; // Floating point comparison tolerance
    
    @BeforeEach
    void setup() {
        player = new Player(TEST_X, TEST_Y);
    }
    
    @AfterEach
    void cleanup() {
        if (player != null) {
            player.dispose();
        }
    }
    
    // ========== Initialization Tests ==========
    
    @Test
    @Order(1)
    @DisplayName("Player initializes with correct position")
    void testPlayerInitialization() {
        assertThat(player).isNotNull();
        assertThat(player.getPosition()).isNotNull();
        assertThat(player.getPosition().x).isCloseTo(TEST_X, within(EPSILON));
        assertThat(player.getPosition().y).isCloseTo(TEST_Y, within(EPSILON));
    }
    
    @Test
    @Order(2)
    @DisplayName("Player bounds match position and size")
    void testPlayerBounds() {
        Rectangle bounds = player.getBounds();
        assertThat(bounds).isNotNull();
        assertThat(bounds.x).isCloseTo(TEST_X, within(EPSILON));
        assertThat(bounds.y).isCloseTo(TEST_Y, within(EPSILON));
        assertThat(bounds.width).isCloseTo(Constants.PLAYER_WIDTH, within(EPSILON));
        assertThat(bounds.height).isCloseTo(Constants.PLAYER_HEIGHT, within(EPSILON));
    }
    
    @Test
    @Order(3)
    @DisplayName("Player starts with zero velocity")
    void testInitialVelocity() {
        Vector2 velocity = player.getVelocity();
        assertThat(velocity).isNotNull();
        assertThat(velocity.x).isCloseTo(0f, within(EPSILON));
        assertThat(velocity.y).isCloseTo(0f, within(EPSILON));
    }
    
    @Test
    @Order(4)
    @DisplayName("Player starts not grounded")
    void testInitialGroundedState() {
        assertThat(player.isGrounded()).isFalse();
    }
    
    // ========== Movement Tests ==========
    
    @Test
    @Order(10)
    @DisplayName("Move left sets negative horizontal velocity")
    void testMoveLeft() {
        player.moveLeft();
        assertThat(player.getVelocity().x).isNegative();
        assertThat(player.getVelocity().x).isCloseTo(-Constants.MOVE_SPEED, within(EPSILON));
    }
    
    @Test
    @Order(11)
    @DisplayName("Move right sets positive horizontal velocity")
    void testMoveRight() {
        player.moveRight();
        assertThat(player.getVelocity().x).isPositive();
        assertThat(player.getVelocity().x).isCloseTo(Constants.MOVE_SPEED, within(EPSILON));
    }
    
    @Test
    @Order(12)
    @DisplayName("Stop horizontal movement zeros X velocity")
    void testStopHorizontalMovement() {
        player.moveRight();
        player.stopHorizontalMovement();
        assertThat(player.getVelocity().x).isCloseTo(0f, within(EPSILON));
    }
    
    @Test
    @Order(13)
    @DisplayName("Jump sets upward velocity when grounded")
    void testJumpWhenGrounded() {
        player.setGrounded(true);
        player.jump();
        assertThat(player.getVelocity().y).isPositive();
        assertThat(player.getVelocity().y).isCloseTo(Constants.JUMP_VELOCITY, within(EPSILON));
        assertThat(player.isGrounded()).isFalse();
    }
    
    @Test
    @Order(14)
    @DisplayName("Jump does nothing when airborne")
    void testJumpWhenAirborne() {
        player.setGrounded(false);
        float initialVelY = player.getVelocity().y;
        player.jump();
        assertThat(player.getVelocity().y).isCloseTo(initialVelY, within(EPSILON));
    }
    
    // ========== Physics Update Tests ==========
    
    @Test
    @Order(20)
    @DisplayName("Gravity applies when not grounded")
    void testGravityApplication() {
        player.setGrounded(false);
        float initialVelY = player.getVelocity().y;
        player.update(DELTA_TIME);
        
        float expectedVelY = initialVelY - Constants.GRAVITY * DELTA_TIME;
        assertThat(player.getVelocity().y).isLessThan(initialVelY);
        assertThat(player.getVelocity().y).isCloseTo(expectedVelY, within(EPSILON));
    }
    
    @Test
    @Order(21)
    @DisplayName("Position updates based on velocity")
    void testPositionUpdate() {
        player.moveRight();
        float initialX = player.getPosition().x;
        player.update(DELTA_TIME);
        
        float expectedX = initialX + Constants.MOVE_SPEED * DELTA_TIME;
        assertThat(player.getPosition().x).isGreaterThan(initialX);
        assertThat(player.getPosition().x).isCloseTo(expectedX, within(EPSILON));
    }
    
    @Test
    @Order(22)
    @DisplayName("Fall speed is clamped to maximum")
    void testMaxFallSpeedClamp() {
        player.setGrounded(false);
        // Simulate many updates to exceed max fall speed
        for (int i = 0; i < 100; i++) {
            player.update(DELTA_TIME);
        }
        
        assertThat(player.getVelocity().y).isGreaterThanOrEqualTo(-Constants.MAX_FALL_SPEED);
    }
    
    @Test
    @Order(23)
    @DisplayName("Player stays within horizontal world bounds")
    void testHorizontalBounds() {
        // Test left boundary
        Player leftPlayer = new Player(-100, 100);
        leftPlayer.moveLeft();
        leftPlayer.update(DELTA_TIME);
        assertThat(leftPlayer.getPosition().x).isGreaterThanOrEqualTo(0);
        leftPlayer.dispose();
        
        // Test right boundary
        Player rightPlayer = new Player(Constants.WORLD_WIDTH + 100, 100);
        rightPlayer.moveRight();
        rightPlayer.update(DELTA_TIME);
        assertThat(rightPlayer.getPosition().x).isLessThanOrEqualTo(
            Constants.WORLD_WIDTH - Constants.PLAYER_WIDTH);
        rightPlayer.dispose();
    }
    
    // ========== Collision Tests ==========
    
    @Test
    @Order(30)
    @DisplayName("Landing on platform top sets grounded and stops fall")
    void testLandingOnPlatform() {
        // Create platform first
        Platform platform = new Platform(90, 100, 100, 20, false);
        
        // Position player so it overlaps platform from above (falling through it)
        // Player bottom should be slightly below platform top for overlap
        float platformTop = platform.getBounds().y + platform.getBounds().height;
        Player fallingPlayer = new Player(100, platformTop - 5); // 5 pixels overlap
        fallingPlayer.setGrounded(false);
        fallingPlayer.getVelocity().y = -100; // Falling downward
        
        // Update bounds to match position
        fallingPlayer.getBounds().setPosition(fallingPlayer.getPosition().x, fallingPlayer.getPosition().y);
        
        fallingPlayer.checkCollision(platform);
        
        assertThat(fallingPlayer.isGrounded()).isTrue();
        assertThat(fallingPlayer.getVelocity().y).isCloseTo(0f, within(EPSILON));
        assertThat(fallingPlayer.getPosition().y).isCloseTo(platformTop, within(EPSILON));
        
        fallingPlayer.dispose();
    }
    
    @Test
    @Order(31)
    @DisplayName("Hitting platform from below stops upward movement")
    void testHittingPlatformFromBelow() {
        // Create platform first
        Platform platform = new Platform(90, 100, 100, 20, false);
        
        // Position player so it overlaps platform from below (jumping into it)
        // Player top should be slightly above platform bottom for overlap
        float platformBottom = platform.getBounds().y;
        float playerHeight = Constants.PLAYER_HEIGHT;
        Player jumpingPlayer = new Player(100, platformBottom - playerHeight + 5); // 5 pixels overlap
        jumpingPlayer.getVelocity().y = 200; // Moving upward
        
        // Update bounds to match position
        jumpingPlayer.getBounds().setPosition(jumpingPlayer.getPosition().x, jumpingPlayer.getPosition().y);
        
        jumpingPlayer.checkCollision(platform);
        
        assertThat(jumpingPlayer.getVelocity().y).isCloseTo(0f, within(EPSILON));
        assertThat(jumpingPlayer.getPosition().y).isLessThanOrEqualTo(platformBottom - playerHeight);
        
        jumpingPlayer.dispose();
    }
    
    @Test
    @Order(32)
    @DisplayName("Hitting platform from left stops horizontal movement")
    void testHittingPlatformFromLeft() {
        // Create platform first
        Platform platform = new Platform(100, 100, 50, 20, false);
        
        // Position player so it overlaps platform from left
        // Player right edge should be slightly past platform left for overlap
        float platformLeft = platform.getBounds().x;
        float playerWidth = Constants.PLAYER_WIDTH;
        Player movingPlayer = new Player(platformLeft - playerWidth + 5, 105); // 5 pixels overlap
        movingPlayer.moveRight();
        
        // Update bounds to match position
        movingPlayer.getBounds().setPosition(movingPlayer.getPosition().x, movingPlayer.getPosition().y);
        
        movingPlayer.checkCollision(platform);
        
        assertThat(movingPlayer.getVelocity().x).isCloseTo(0f, within(EPSILON));
        assertThat(movingPlayer.getPosition().x).isLessThanOrEqualTo(platformLeft - playerWidth);
        
        movingPlayer.dispose();
    }
    
    @Test
    @Order(33)
    @DisplayName("Hitting platform from right stops horizontal movement")
    void testHittingPlatformFromRight() {
        // Create platform first
        Platform platform = new Platform(100, 100, 30, 20, false);
        
        // Position player so it overlaps platform from right
        // Player left edge should be slightly before platform right edge for overlap
        float platformRight = platform.getBounds().x + platform.getBounds().width;
        Player movingPlayer = new Player(platformRight - 5, 105); // 5 pixels overlap
        movingPlayer.moveLeft();
        
        // Update bounds to match position
        movingPlayer.getBounds().setPosition(movingPlayer.getPosition().x, movingPlayer.getPosition().y);
        
        movingPlayer.checkCollision(platform);
        
        assertThat(movingPlayer.getVelocity().x).isCloseTo(0f, within(EPSILON));
        assertThat(movingPlayer.getPosition().x).isGreaterThanOrEqualTo(platformRight);
        
        movingPlayer.dispose();
    }
    
    @Test
    @Order(34)
    @DisplayName("No collision when player and platform don't overlap")
    void testNoCollisionWhenSeparate() {
        Player separatePlayer = new Player(200, 200);
        Platform platform = new Platform(50, 50, 50, 20, false);
        
        float initialVelX = separatePlayer.getVelocity().x;
        float initialVelY = separatePlayer.getVelocity().y;
        boolean initialGrounded = separatePlayer.isGrounded();
        
        separatePlayer.checkCollision(platform);
        
        // Nothing should change
        assertThat(separatePlayer.getVelocity().x).isCloseTo(initialVelX, within(EPSILON));
        assertThat(separatePlayer.getVelocity().y).isCloseTo(initialVelY, within(EPSILON));
        assertThat(separatePlayer.isGrounded()).isEqualTo(initialGrounded);
        
        separatePlayer.dispose();
    }
    
    // ========== State Management Tests ==========
    
    @Test
    @Order(40)
    @DisplayName("Grounded state resets after update")
    void testGroundedStateReset() {
        player.setGrounded(true);
        player.update(DELTA_TIME);
        // Update should reset grounded to false (needs collision to set it true again)
        assertThat(player.isGrounded()).isFalse();
    }
    
    @Test
    @Order(41)
    @DisplayName("Bounce sets upward velocity and ungrounds player")
    void testBounce() {
        float bounceVel = 500f;
        player.setGrounded(true);
        player.bounce(bounceVel);
        
        assertThat(player.getVelocity().y).isCloseTo(bounceVel, within(EPSILON));
        assertThat(player.isGrounded()).isFalse();
    }
    
    @Test
    @Order(42)
    @DisplayName("Reset position clears velocity and grounds state")
    void testResetPosition() {
        Vector2 spawnPos = new Vector2(300, 400);
        
        player.moveRight();
        player.jump();
        player.resetPosition(spawnPos);
        
        assertThat(player.getPosition().x).isCloseTo(spawnPos.x, within(EPSILON));
        assertThat(player.getPosition().y).isCloseTo(spawnPos.y, within(EPSILON));
        assertThat(player.getVelocity().x).isCloseTo(0f, within(EPSILON));
        assertThat(player.getVelocity().y).isCloseTo(0f, within(EPSILON));
        assertThat(player.isGrounded()).isFalse();
    }
    
    // ========== Parameterized Tests ==========
    
    @ParameterizedTest
    @Order(50)
    @DisplayName("Player can be created at various positions")
    @CsvSource({
        "0, 0",
        "100, 200",
        "640, 360",
        "1000, 500"
    })
    void testPlayerCreationAtDifferentPositions(float x, float y) {
        Player testPlayer = new Player(x, y);
        assertThat(testPlayer.getPosition().x).isCloseTo(x, within(EPSILON));
        assertThat(testPlayer.getPosition().y).isCloseTo(y, within(EPSILON));
        testPlayer.dispose();
    }
    
    @ParameterizedTest
    @Order(51)
    @DisplayName("Gravity accumulates correctly over multiple frames")
    @ValueSource(ints = {1, 5, 10, 30})
    void testGravityAccumulation(int frames) {
        player.setGrounded(false);
        float initialVelY = player.getVelocity().y;
        
        for (int i = 0; i < frames; i++) {
            player.update(DELTA_TIME);
            // Keep player not grounded (update resets it)
            player.setGrounded(false);
        }
        
        float expectedVelY = initialVelY - Constants.GRAVITY * DELTA_TIME * frames;
        // Clamp to max fall speed
        expectedVelY = Math.max(expectedVelY, -Constants.MAX_FALL_SPEED);
        
        // Use wider tolerance for accumulated error over many frames
        float tolerance = EPSILON * frames;
        assertThat(player.getVelocity().y).isCloseTo(expectedVelY, within(tolerance));
    }
    
    // ========== Edge Case Tests ==========
    
    @Test
    @Order(60)
    @DisplayName("Player handles negative spawn position")
    void testNegativeSpawnPosition() {
        Player negativePlayer = new Player(-50, -50);
        assertThat(negativePlayer).isNotNull();
        negativePlayer.dispose();
    }
    
    @Test
    @Order(61)
    @DisplayName("Player handles very large coordinates")
    void testLargeCoordinates() {
        Player largePlayer = new Player(10000, 10000);
        assertThat(largePlayer).isNotNull();
        largePlayer.dispose();
    }
    
    @Test
    @Order(62)
    @DisplayName("Multiple rapid movement changes")
    void testRapidMovementChanges() {
        for (int i = 0; i < 10; i++) {
            player.moveLeft();
            player.update(DELTA_TIME);
            player.moveRight();
            player.update(DELTA_TIME);
            player.stopHorizontalMovement();
            player.update(DELTA_TIME);
        }
        
        // Player should still be valid
        assertThat(player.getPosition()).isNotNull();
        assertThat(player.getBounds()).isNotNull();
    }
    
    @Test
    @Order(63)
    @DisplayName("Previous position tracking for interpolation")
    void testPreviousPositionTracking() {
        float initialX = player.getPosition().x;
        player.savePreviousPosition();
        
        assertThat(player.getPreviousPosition()).isNotNull();
        assertThat(player.getPreviousPosition().x).isCloseTo(initialX, within(EPSILON));
        
        player.moveRight();
        player.update(DELTA_TIME);
        
        // Previous position should still be at initial position
        assertThat(player.getPreviousPosition().x).isCloseTo(initialX, within(EPSILON));
        // Current position should have moved
        assertThat(player.getPosition().x).isGreaterThan(initialX);
    }
}
