package com.chainedclimber.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced debug mode system for runtime debugging and testing.
 * 
 * Features:
 * - Toggle on/off via keyboard (F3 key)
 * - Multiple debug visualization layers
 * - Real-time value monitoring
 * - Entity state inspection
 * - Collision box visualization
 * - Performance profiling
 * - Frame-by-frame stepping (F4 key)
 * - Debug logging with levels
 * 
 * Usage:
 *   DebugMode.setEnabled(true);
 *   DebugMode.log("Player", "Position", player.getPosition());
 *   DebugMode.drawBounds(shapeRenderer, entity.getBounds());
 */
public class DebugMode {
    
    private static boolean enabled = false;
    private static boolean stepMode = false;
    private static boolean stepNext = false;
    private static DebugLevel currentLevel = DebugLevel.INFO;
    
    private static final Map<String, Map<String, Object>> debugValues = new HashMap<>();
    private static final List<DebugMessage> messageLog = new ArrayList<>();
    private static final int MAX_LOG_MESSAGES = 50;
    
    private static BitmapFont debugFont;
    private static boolean fontsInitialized = false;
    
    // Debug colors
    private static final Color BOUNDS_COLOR = new Color(0, 1, 0, 0.8f);
    private static final Color VELOCITY_COLOR = new Color(1, 1, 0, 0.8f);
    private static final Color COLLISION_COLOR = new Color(1, 0, 0, 0.8f);
    private static final Color INFO_COLOR = new Color(0.8f, 0.8f, 1f, 1f);
    
    public enum DebugLevel {
        VERBOSE(0), DEBUG(1), INFO(2), WARNING(3), ERROR(4);
        
        final int level;
        DebugLevel(int level) { this.level = level; }
    }
    
    /**
     * Enable or disable debug mode
     */
    public static void setEnabled(boolean enable) {
        enabled = enable;
        if (enable) {
            System.out.println("=".repeat(80));
            System.out.println("DEBUG MODE ENABLED");
            System.out.println("F3: Toggle debug overlay");
            System.out.println("F4: Toggle step mode (frame-by-frame)");
            System.out.println("F5: Step one frame (in step mode)");
            System.out.println("=".repeat(80));
        } else {
            System.out.println("DEBUG MODE DISABLED");
        }
    }
    
    /**
     * Check if debug mode is enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set debug logging level
     */
    public static void setLevel(DebugLevel level) {
        currentLevel = level;
    }
    
    /**
     * Toggle step mode (frame-by-frame execution)
     */
    public static void toggleStepMode() {
        stepMode = !stepMode;
        System.out.println("Step Mode: " + (stepMode ? "ENABLED" : "DISABLED"));
    }
    
    /**
     * Check if execution should pause (step mode active and not stepping)
     */
    public static boolean shouldPause() {
        if (!stepMode) return false;
        if (stepNext) {
            stepNext = false;
            return false;
        }
        return true;
    }
    
    /**
     * Advance one frame in step mode
     */
    public static void stepFrame() {
        if (stepMode) {
            stepNext = true;
        }
    }
    
    /**
     * Log a debug value for an entity/system
     */
    public static void log(String category, String key, Object value) {
        log(category, key, value, DebugLevel.INFO);
    }
    
    /**
     * Log a debug value with specific level
     */
    public static void log(String category, String key, Object value, DebugLevel level) {
        if (!enabled || level.level < currentLevel.level) return;
        
        debugValues.computeIfAbsent(category, k -> new HashMap<>()).put(key, value);
        
        // Add to message log
        DebugMessage msg = new DebugMessage(
            category,
            key + " = " + value,
            level,
            System.currentTimeMillis()
        );
        
        messageLog.add(msg);
        if (messageLog.size() > MAX_LOG_MESSAGES) {
            messageLog.remove(0);
        }
        
        // Console output for important messages
        if (level.level >= DebugLevel.WARNING.level) {
            System.out.println("[" + level + "] " + category + ": " + key + " = " + value);
        }
    }
    
    /**
     * Log a formatted message
     */
    public static void logMessage(String category, String message, DebugLevel level) {
        if (!enabled || level.level < currentLevel.level) return;
        
        DebugMessage msg = new DebugMessage(category, message, level, System.currentTimeMillis());
        messageLog.add(msg);
        if (messageLog.size() > MAX_LOG_MESSAGES) {
            messageLog.remove(0);
        }
        
        if (level.level >= DebugLevel.WARNING.level) {
            System.out.println("[" + level + "] " + category + ": " + message);
        }
    }
    
    /**
     * Draw entity bounds for collision debugging
     */
    public static void drawBounds(ShapeRenderer renderer, Rectangle bounds) {
        drawBounds(renderer, bounds, BOUNDS_COLOR);
    }
    
    /**
     * Draw entity bounds with custom color
     */
    public static void drawBounds(ShapeRenderer renderer, Rectangle bounds, Color color) {
        if (!enabled || renderer == null || bounds == null) return;
        
        renderer.setColor(color);
        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    /**
     * Draw velocity vector
     */
    public static void drawVelocity(ShapeRenderer renderer, Vector2 position, Vector2 velocity, float scale) {
        if (!enabled || renderer == null || position == null || velocity == null) return;
        
        renderer.setColor(VELOCITY_COLOR);
        float endX = position.x + velocity.x * scale;
        float endY = position.y + velocity.y * scale;
        renderer.line(position.x, position.y, endX, endY);
        
        // Arrow head
        float arrowSize = 5f;
        float angle = (float) Math.atan2(velocity.y, velocity.x);
        float x1 = endX - arrowSize * (float) Math.cos(angle - Math.PI / 6);
        float y1 = endY - arrowSize * (float) Math.sin(angle - Math.PI / 6);
        float x2 = endX - arrowSize * (float) Math.cos(angle + Math.PI / 6);
        float y2 = endY - arrowSize * (float) Math.sin(angle + Math.PI / 6);
        
        renderer.line(endX, endY, x1, y1);
        renderer.line(endX, endY, x2, y2);
    }
    
    /**
     * Draw collision point or area
     */
    public static void drawCollisionPoint(ShapeRenderer renderer, float x, float y, float radius) {
        if (!enabled || renderer == null) return;
        
        renderer.setColor(COLLISION_COLOR);
        renderer.circle(x, y, radius);
    }
    
    /**
     * Render debug overlay with all logged values
     */
    public static void renderOverlay(SpriteBatch batch) {
        if (!enabled) return;
        
        if (!fontsInitialized) {
            debugFont = new BitmapFont();
            debugFont.setColor(INFO_COLOR);
            fontsInitialized = true;
        }
        
        batch.begin();
        
        float y = Gdx.graphics.getHeight() - 20;
        float x = 10;
        
        // Header
        debugFont.setColor(Color.YELLOW);
        debugFont.draw(batch, "=== DEBUG MODE ACTIVE ===", x, y);
        y -= 25;
        
        // Step mode indicator
        if (stepMode) {
            debugFont.setColor(Color.ORANGE);
            debugFont.draw(batch, "[STEP MODE] Press F5 to advance frame", x, y);
            y -= 20;
        }
        
        // Debug values by category
        debugFont.setColor(INFO_COLOR);
        for (Map.Entry<String, Map<String, Object>> category : debugValues.entrySet()) {
            debugFont.setColor(Color.CYAN);
            debugFont.draw(batch, "--- " + category.getKey() + " ---", x, y);
            y -= 20;
            
            debugFont.setColor(INFO_COLOR);
            for (Map.Entry<String, Object> entry : category.getValue().entrySet()) {
                String text = "  " + entry.getKey() + ": " + entry.getValue();
                debugFont.draw(batch, text, x, y);
                y -= 18;
            }
            y -= 5;
        }
        
        // Recent messages
        if (!messageLog.isEmpty()) {
            y -= 10;
            debugFont.setColor(Color.YELLOW);
            debugFont.draw(batch, "--- Recent Messages ---", x, y);
            y -= 20;
            
            int shown = Math.min(10, messageLog.size());
            for (int i = messageLog.size() - shown; i < messageLog.size(); i++) {
                DebugMessage msg = messageLog.get(i);
                debugFont.setColor(getLevelColor(msg.level));
                debugFont.draw(batch, "[" + msg.level + "] " + msg.category + ": " + msg.message, x, y);
                y -= 18;
            }
        }
        
        batch.end();
    }
    
    /**
     * Clear all debug values
     */
    public static void clear() {
        debugValues.clear();
    }
    
    /**
     * Clear specific category
     */
    public static void clearCategory(String category) {
        debugValues.remove(category);
    }
    
    /**
     * Get color for debug level
     */
    private static Color getLevelColor(DebugLevel level) {
        switch (level) {
            case ERROR: return Color.RED;
            case WARNING: return Color.ORANGE;
            case INFO: return Color.WHITE;
            case DEBUG: return Color.LIGHT_GRAY;
            case VERBOSE: return Color.GRAY;
            default: return Color.WHITE;
        }
    }
    
    /**
     * Cleanup resources
     */
    public static void dispose() {
        if (debugFont != null) {
            debugFont.dispose();
        }
    }
    
    /**
     * Debug message data class
     */
    private static class DebugMessage {
        final String category;
        final String message;
        final DebugLevel level;
        final long timestamp;
        
        DebugMessage(String category, String message, DebugLevel level, long timestamp) {
            this.category = category;
            this.message = message;
            this.level = level;
            this.timestamp = timestamp;
        }
    }
}
