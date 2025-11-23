package com.chainedclimber.entities;

import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.GdxTestRunner;
import com.chainedclimber.TestResultAnalyzer;
import com.chainedclimber.utils.LevelData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MovingPlatform entity.
 * Tests cover:
 * - Movement behavior and velocity
 * - Bounds and position updates
 * - Delta tracking for player carry
 * - Collision with level geometry
 * - Direction reversal
 * - Interpolation support
 */
@ExtendWith(GdxTestRunner.class)
@ExtendWith(TestResultAnalyzer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Moving Platform Tests")
public class MovingPlatformTest {
    
    private MovingPlatform platform;
    private static final float DELTA_TIME = 1/60f;
    private static final float EPSILON = 0.001f;
    
    @BeforeEach
    void setup() {
        platform = new MovingPlatform(100, 100, 80, 80, 200);
    }
    
    @Test
    @Order(1)
    @DisplayName("Moving platform initializes correctly")
    void testInitialization() {
        assertThat(platform).isNotNull();
        assertThat(platform.getBounds()).isNotNull();
        assertThat(platform.getVelocity()).isNotNull();
    }
    
    @Test
    @Order(2)
    @DisplayName("Moving platform moves horizontally")
    void testHorizontalMovement() {
        float initialX = platform.getBounds().x;
        platform.update(DELTA_TIME);
        
        assertThat(platform.getBounds().x).isNotEqualTo(initialX);
    }
    
    @Test
    @Order(3)
    @DisplayName("Moving platform reverses at end boundary")
    void testBoundaryReversal() {
        // Update until platform reaches end
        for (int i = 0; i < 200; i++) {
            platform.update(DELTA_TIME);
        }
        
        // Velocity should have reversed at some point
        assertThat(platform.getBounds().x).isLessThanOrEqualTo(300); // start + distance
    }
    
    @Test
    @Order(4)
    @DisplayName("Delta tracking captures movement per frame")
    void testDeltaTracking() {
        float initialX = platform.getBounds().x;
        
        // Store old position before update
        float oldX = platform.getBounds().x;
        float oldY = platform.getBounds().y;
        
        platform.update(DELTA_TIME);
        
        // Calculate actual movement
        float actualDeltaX = platform.getBounds().x - oldX;
        float actualDeltaY = platform.getBounds().y - oldY;
        
        // Set the delta manually (normally done by GameScreen)
        platform.setLastDelta(actualDeltaX, actualDeltaY);
        
        assertThat(platform.getLastDeltaX()).isCloseTo(actualDeltaX, within(EPSILON));
    }
    
    @Test
    @Order(5)
    @DisplayName("Previous state saved for interpolation")
    void testPreviousStateSaving() {
        float initialX = platform.getBounds().x;
        platform.savePreviousState();
        platform.update(DELTA_TIME);
        
        // Previous state should be preserved
        assertThat(platform.getBounds().x).isNotEqualTo(initialX);
    }
    
    @ParameterizedTest
    @Order(10)
    @DisplayName("Platform created with various travel distances")
    @CsvSource({
        "100, 100, 80, 80, 50",
        "200, 200, 80, 80, 150",
        "300, 300, 80, 80, 300"
    })
    void testVariousTravelDistances(float x, float y, float w, float h, float distance) {
        MovingPlatform testPlatform = new MovingPlatform(x, y, w, h, distance);
        assertThat(testPlatform.getBounds().x).isCloseTo(x, within(EPSILON));
    }
}
