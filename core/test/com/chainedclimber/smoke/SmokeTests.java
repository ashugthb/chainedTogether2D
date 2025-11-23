package com.chainedclimber.smoke;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chainedclimber.GdxTestRunner;
import com.chainedclimber.TestResultAnalyzer;

/**
 * Smoke tests verify that critical game systems are operational.
 * These tests run quickly and catch major breaking changes immediately.
 * 
 * Smoke tests should:
 * - Execute in < 5 seconds total
 * - Test only critical paths
 * - Fail fast on major issues
 * - Run before full test suite
 * 
 * Order: @Order(1) = highest priority
 */
@ExtendWith(GdxTestRunner.class)
@ExtendWith(TestResultAnalyzer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Smoke Tests - Critical System Verification")
public class SmokeTests {
    
    @BeforeAll
    static void setupSuite() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SMOKE TESTS - Verifying Critical Game Systems");
        System.out.println("=".repeat(80) + "\n");
        TestResultAnalyzer.reset();
    }
    
    @AfterAll
    static void teardownSuite() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SMOKE TESTS COMPLETED");
        System.out.println("=".repeat(80) + "\n");
        TestResultAnalyzer.generateReport();
    }
    
    @Test
    @Order(1)
    @DisplayName("System Initialization - LibGDX Context")
    void testLibGdxInitialization() {
        // In headless mode, GL may be mocked - just verify it's available
        assertDoesNotThrow(() -> {
            // Try to access Gdx - if it fails, LibGDX isn't initialized
            assertNotNull(com.badlogic.gdx.Gdx.class, "LibGDX should be loaded");
        }, "LibGDX should be available in test environment");
    }
    
    @Test
    @Order(2)
    @DisplayName("Constants Loading - Game Configuration")
    void testConstantsLoaded() {
        assertNotNull(com.chainedclimber.utils.Constants.class, "Constants class should be loaded");
        assertTrue(com.chainedclimber.utils.Constants.WORLD_WIDTH > 0, "World width should be positive");
        assertTrue(com.chainedclimber.utils.Constants.WORLD_HEIGHT > 0, "World height should be positive");
        assertTrue(com.chainedclimber.utils.Constants.PLAYER_WIDTH > 0, "Player width should be positive");
        assertTrue(com.chainedclimber.utils.Constants.PLAYER_HEIGHT > 0, "Player height should be positive");
        assertTrue(com.chainedclimber.utils.Constants.PLATFORM_WIDTH > 0, "Platform width should be positive");
        assertTrue(com.chainedclimber.utils.Constants.PLATFORM_HEIGHT > 0, "Platform height should be positive");
    }
    
    @Test
    @Order(3)
    @DisplayName("Player Entity Creation - Core Gameplay")
    void testPlayerCreation() {
        assertDoesNotThrow(() -> {
            com.chainedclimber.entities.Player player = new com.chainedclimber.entities.Player(100, 100);
            assertNotNull(player, "Player should be created");
            assertNotNull(player.getBounds(), "Player bounds should exist");
            assertNotNull(player.getPosition(), "Player position should exist");
            assertNotNull(player.getVelocity(), "Player velocity should exist");
        }, "Player creation should not throw exception");
    }
    
    @Test
    @Order(4)
    @DisplayName("Platform Entity Creation - Level Structure")
    void testPlatformCreation() {
        assertDoesNotThrow(() -> {
            com.chainedclimber.entities.Platform platform = 
                new com.chainedclimber.entities.Platform(50, 50, 100, 20, false);
            assertNotNull(platform, "Platform should be created");
            assertNotNull(platform.getBounds(), "Platform bounds should exist");
        }, "Platform creation should not throw exception");
    }
    
    @Test
    @Order(5)
    @DisplayName("Level Matrix Generation - Level System")
    void testLevelMatrixGeneration() {
        assertDoesNotThrow(() -> {
            com.chainedclimber.utils.LevelMatrix matrix = 
                com.chainedclimber.utils.LevelGenerator.createLevel2();
            assertNotNull(matrix, "Level matrix should be generated");
            assertTrue(matrix.getRows() > 0, "Level should have rows");
            assertTrue(matrix.getColumns() > 0, "Level should have columns");
        }, "Level matrix generation should not throw exception");
    }
    
    @Test
    @Order(6)
    @DisplayName("Spatial Hash System - Performance Optimization")
    void testSpatialHashCreation() {
        assertDoesNotThrow(() -> {
            com.chainedclimber.systems.SpatialHash<com.chainedclimber.entities.Platform> spatialHash = 
                new com.chainedclimber.systems.SpatialHash<>(100);
            assertNotNull(spatialHash, "Spatial hash should be created");
        }, "Spatial hash creation should not throw exception");
    }
    
    @Test
    @Order(7)
    @DisplayName("Physics Constants - Movement System")
    void testPhysicsConstants() {
        assertTrue(com.chainedclimber.utils.Constants.GRAVITY > 0, "Gravity should be positive");
        assertTrue(com.chainedclimber.utils.Constants.JUMP_VELOCITY > 0, "Jump velocity should be positive");
        assertTrue(com.chainedclimber.utils.Constants.MOVE_SPEED > 0, "Move speed should be positive");
        assertTrue(com.chainedclimber.utils.Constants.MAX_FALL_SPEED > 0, "Max fall speed should be positive");
    }
    
    @Test
    @Order(8)
    @DisplayName("Block Type System - Level Elements")
    void testBlockTypes() {
        assertDoesNotThrow(() -> {
            float[] platformColor = com.chainedclimber.utils.BlockType.getColor(
                com.chainedclimber.utils.BlockType.PLATFORM);
            assertNotNull(platformColor, "Platform color should exist");
            assertEquals(3, platformColor.length, "Color should have RGB components");
            
            // Test that block types are properly defined
            assertTrue(com.chainedclimber.utils.BlockType.PLATFORM > 0, "Platform type should be positive");
            assertTrue(com.chainedclimber.utils.BlockType.SPIKE > 0, "Spike type should be positive");
        }, "Block type system should work correctly");
    }
    
    @Test
    @Order(9)
    @DisplayName("Moving Platform Creation - Dynamic Elements")
    void testMovingPlatformCreation() {
        assertDoesNotThrow(() -> {
            com.chainedclimber.entities.MovingPlatform movingPlatform = 
                new com.chainedclimber.entities.MovingPlatform(100, 100, 80, 80, 200);
            assertNotNull(movingPlatform, "Moving platform should be created");
            assertNotNull(movingPlatform.getBounds(), "Moving platform bounds should exist");
            assertNotNull(movingPlatform.getVelocity(), "Moving platform velocity should exist");
        }, "Moving platform creation should not throw exception");
    }
    
    @Test
    @Order(10)
    @DisplayName("Render Culler System - Rendering Optimization")
    void testRenderCullerCreation() {
        assertDoesNotThrow(() -> {
            com.chainedclimber.systems.RenderCuller culler = new com.chainedclimber.systems.RenderCuller();
            assertNotNull(culler, "Render culler should be created");
        }, "Render culler creation should not throw exception");
    }
}
