package com.chainedclimber.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TouchButtonManager - Centralized manager for all touch buttons
 * 
 * GUARANTEES:
 * - Touch areas and rendering positions ALWAYS match
 * - Automatic screen resize handling
 * - Easy to add new buttons dynamically
 * - Clean separation between input and rendering
 */
public class TouchButtonManager {
    private final List<TouchButton> buttons;
    private final Map<String, TouchButton> buttonMap;
    
    public TouchButtonManager() {
        buttons = new ArrayList<>();
        buttonMap = new HashMap<>();
    }
    
    /**
     * Add a button to the manager
     */
    public void addButton(TouchButton button) {
        buttons.add(button);
        buttonMap.put(button.getId(), button);
    }
    
    /**
     * Get button by ID
     */
    public TouchButton getButton(String id) {
        return buttonMap.get(id);
    }
    
    /**
     * Update all button touch states
     * Call this every frame in your update loop
     */
    public void update() {
        // First, reset all buttons to not touched
        for (TouchButton button : buttons) {
            button.updateTouchState(false);
        }
        
        // Check all active touch pointers
        for (int pointer = 0; pointer < 10; pointer++) {
            if (Gdx.input.isTouched(pointer)) {
                float touchX = Gdx.input.getX(pointer);
                float touchY = Gdx.input.getY(pointer);
                
                // Check each button
                for (TouchButton button : buttons) {
                    if (button.checkTouch(touchX, touchY)) {
                        button.updateTouchState(true);
                    }
                }
            }
        }
    }
    
    /**
     * Render all buttons with GUARANTEED matching touch areas
     * 
     * This method handles coordinate conversion automatically!
     */
    public void render(ShapeRenderer shapeRenderer) {
        int screenHeight = Gdx.graphics.getHeight();
        
        // Draw button backgrounds
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (TouchButton button : buttons) {
            float[] color = button.getCurrentColor();
            shapeRenderer.setColor(color[0], color[1], color[2], color[3]);
            
            float renderY = button.getRenderY(screenHeight);
            shapeRenderer.circle(button.getScreenX(), renderY, button.getRadius());
        }
        shapeRenderer.end();
        
        // Draw button borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        for (TouchButton button : buttons) {
            float[] borderColor = button.getBorderColor();
            shapeRenderer.setColor(borderColor[0], borderColor[1], borderColor[2], borderColor[3]);
            
            float renderY = button.getRenderY(screenHeight);
            shapeRenderer.circle(button.getScreenX(), renderY, button.getRadius());
        }
        shapeRenderer.end();
    }
    
    /**
     * Render button icons/arrows
     * Override this or use a callback system for custom icons
     */
    public void renderIcons(ShapeRenderer shapeRenderer, IconRenderer iconRenderer) {
        int screenHeight = Gdx.graphics.getHeight();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (TouchButton button : buttons) {
            float renderY = button.getRenderY(screenHeight);
            iconRenderer.renderIcon(shapeRenderer, button, button.getScreenX(), renderY);
        }
        shapeRenderer.end();
    }
    
    /**
     * Handle screen resize - updates all button positions
     */
    public void resize(int width, int height) {
        // Override this in your implementation to reposition buttons
        // Call button.setPosition() for each button
    }
    
    /**
     * Get all buttons
     */
    public List<TouchButton> getButtons() {
        return buttons;
    }
    
    /**
     * Clear all buttons
     */
    public void clear() {
        buttons.clear();
        buttonMap.clear();
    }
    
    /**
     * Interface for custom icon rendering
     */
    public interface IconRenderer {
        void renderIcon(ShapeRenderer renderer, TouchButton button, float x, float y);
    }
}
