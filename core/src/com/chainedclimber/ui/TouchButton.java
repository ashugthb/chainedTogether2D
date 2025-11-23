package com.chainedclimber.ui;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

/**
 * TouchButton - Unified touch button with guaranteed matching touch area and rendering
 * 
 * COORDINATE SYSTEM:
 * - ALL internal positions use SCREEN coordinates (Y=0 at TOP)
 * - Touch detection uses screen coordinates directly
 * - Rendering conversion happens ONLY at draw time
 * 
 * This ensures touch area and visual ALWAYS match perfectly!
 */
public class TouchButton {
    // Identity
    private final String id;
    
    // Position in SCREEN coordinates (Y=0 at TOP)
    private final Vector2 screenPosition;
    private float radius;
    
    // Touch detection (screen coordinates)
    private final Circle touchArea;
    
    // State
    private boolean touched;
    private boolean wasTouched;
    
    // Visual properties
    private float[] normalColor;
    private float[] touchedColor;
    private float[] borderColor;
    private String iconText;
    
    public TouchButton(String id, float screenX, float screenY, float radius) {
        this.id = id;
        this.screenPosition = new Vector2(screenX, screenY);
        this.radius = radius;
        this.touchArea = new Circle(screenX, screenY, radius);
        
        // Default colors
        this.normalColor = new float[]{0.3f, 0.3f, 0.3f, 0.6f};
        this.touchedColor = new float[]{0.5f, 0.5f, 0.5f, 0.8f};
        this.borderColor = new float[]{0.7f, 0.7f, 0.7f, 0.9f};
        this.iconText = "";
    }
    
    /**
     * Update button position (screen coordinates)
     */
    public void setPosition(float screenX, float screenY) {
        screenPosition.set(screenX, screenY);
        touchArea.setPosition(screenX, screenY);
    }
    
    /**
     * Check if touch point hits this button (screen coordinates)
     */
    public boolean checkTouch(float touchX, float touchY) {
        return touchArea.contains(touchX, touchY);
    }
    
    /**
     * Update touch state
     */
    public void updateTouchState(boolean isTouched) {
        wasTouched = touched;
        touched = isTouched;
    }
    
    /**
     * Check if button was just pressed this frame
     */
    public boolean isJustPressed() {
        return touched && !wasTouched;
    }
    
    /**
     * Get screen X position (for rendering conversion)
     */
    public float getScreenX() {
        return screenPosition.x;
    }
    
    /**
     * Get screen Y position (for rendering conversion)
     */
    public float getScreenY() {
        return screenPosition.y;
    }
    
    /**
     * Convert screen Y to OpenGL Y for rendering
     * Call this when drawing: renderY = button.getRenderY(screenHeight)
     */
    public float getRenderY(int screenHeight) {
        return screenHeight - screenPosition.y;
    }
    
    public float getRadius() {
        return radius;
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
        touchArea.setRadius(radius);
    }
    
    public boolean isTouched() {
        return touched;
    }
    
    public boolean wasTouched() {
        return wasTouched;
    }
    
    public String getId() {
        return id;
    }
    
    // Visual properties
    public void setNormalColor(float r, float g, float b, float a) {
        normalColor = new float[]{r, g, b, a};
    }
    
    public void setTouchedColor(float r, float g, float b, float a) {
        touchedColor = new float[]{r, g, b, a};
    }
    
    public void setBorderColor(float r, float g, float b, float a) {
        borderColor = new float[]{r, g, b, a};
    }
    
    public void setIconText(String text) {
        this.iconText = text;
    }
    
    public float[] getNormalColor() {
        return normalColor;
    }
    
    public float[] getTouchedColor() {
        return touchedColor;
    }
    
    public float[] getBorderColor() {
        return borderColor;
    }
    
    public String getIconText() {
        return iconText;
    }
    
    /**
     * Get current color based on touch state
     */
    public float[] getCurrentColor() {
        return touched ? touchedColor : normalColor;
    }
}
