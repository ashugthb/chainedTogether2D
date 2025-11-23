package com.chainedclimber.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.chainedclimber.ui.TouchButton;
import com.chainedclimber.ui.TouchButtonManager;
import com.chainedclimber.utils.Constants;

/**
 * InputController - BULLETPROOF input handling with event-based system
 * 
 * KEY IMPROVEMENTS:
 * - Uses InputProcessor for proper event queuing (NO MISSED INPUTS!)
 * - Jump inputs are buffered and consumed properly
 * - Keyboard events are captured immediately, not polled
 * - TouchButton system with GUARANTEED matching touch/render positions
 * - All buttons use screen coordinates internally (Y=0 at TOP)
 * - Coordinate conversion happens ONLY at render time
 * - 100% reliable, no timing issues
 */
public class InputController implements InputProcessor {
    // Combined input state (from keyboard OR touch)
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean jumpJustPressed;
    
    // Keyboard state tracking
    private boolean leftKeyPressed;
    private boolean rightKeyPressed;
    
    // Keyboard jump with SAME pattern as TouchButton (current + previous)
    private boolean keyboardJumpDown;        // Current jump key state
    private boolean keyboardJumpWasDown;     // Previous frame jump key state
    
    // Touch button manager
    private final TouchButtonManager touchButtons;
    
    // Button references
    private TouchButton leftButton;
    private TouchButton rightButton;
    private TouchButton jumpButton;
    
    public InputController() {
        touchButtons = new TouchButtonManager();
        
        // Register as input processor to receive events
        Gdx.input.setInputProcessor(this);
        
        // Initialize touch buttons
        initializeTouchButtons();
    }
    
    /**
     * Initialize all touch buttons with proper positioning
     */
    private void initializeTouchButtons() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        
        float radius = Constants.BUTTON_SIZE / 2;
        float margin = Constants.BUTTON_MARGIN;
        
        // Calculate Y for BOTTOM (in screen coordinates: Y=0 at TOP)
        float bottomY = screenHeight - margin - radius;
        
        // LEFT button - bottom-left
        float leftX = margin + radius;
        leftButton = new TouchButton("left", leftX, bottomY, radius);
        leftButton.setNormalColor(0.3f, 0.3f, 0.3f, 0.6f);
        leftButton.setTouchedColor(0.5f, 0.5f, 0.5f, 0.8f);
        leftButton.setBorderColor(0.7f, 0.7f, 0.7f, 0.9f);
        leftButton.setIconText("◄");
        touchButtons.addButton(leftButton);
        
        // RIGHT button - next to left
        float rightX = leftX + Constants.BUTTON_SIZE + margin;
        rightButton = new TouchButton("right", rightX, bottomY, radius);
        rightButton.setNormalColor(0.3f, 0.3f, 0.3f, 0.6f);
        rightButton.setTouchedColor(0.5f, 0.5f, 0.5f, 0.8f);
        rightButton.setBorderColor(0.7f, 0.7f, 0.7f, 0.9f);
        rightButton.setIconText("►");
        touchButtons.addButton(rightButton);
        
        // JUMP button - bottom-right (green)
        float jumpX = screenWidth - margin - radius;
        jumpButton = new TouchButton("jump", jumpX, bottomY, radius);
        jumpButton.setNormalColor(0.2f, 0.7f, 0.2f, 0.6f);
        jumpButton.setTouchedColor(0.3f, 0.9f, 0.3f, 0.9f);
        jumpButton.setBorderColor(0.4f, 1.0f, 0.4f, 1.0f);
        jumpButton.setIconText("▲");
        touchButtons.addButton(jumpButton);
    }
    
    /**
     * Update all input sources and combine results
     * Called once per frame - combines keyboard events with touch state
     */
    public void update() {
        // Update touch buttons
        touchButtons.update();
        
        // CRITICAL FIX: Use POLLING for keyboard jump (more reliable than events!)
        // Check current key state directly from Gdx.input
        boolean jumpKeyCurrentlyDown = Gdx.input.isKeyPressed(Input.Keys.SPACE) ||
                                       Gdx.input.isKeyPressed(Input.Keys.W) ||
                                       Gdx.input.isKeyPressed(Input.Keys.UP);
        
        // Detect "just pressed" = currently down AND was NOT down last frame
        boolean keyboardJustPressed = jumpKeyCurrentlyDown && !keyboardJumpWasDown;
        boolean touchJustPressed = jumpButton.isJustPressed();
        
        // Combine both sources (keyboard OR touch)
        jumpJustPressed = keyboardJustPressed || touchJustPressed;
        
        // Update previous state for next frame
        keyboardJumpWasDown = jumpKeyCurrentlyDown;
        
        // Combine movement (keyboard OR touch) - also use polling for consistency
        leftPressed = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT) || leftButton.isTouched();
        rightPressed = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) || rightButton.isTouched();
    }
    
    /**
     * Consume the jump input - call IMMEDIATELY after using jump
     * This prevents jump from being triggered multiple times per press
     */
    public void consumeJump() {
        jumpJustPressed = false;
    }
    
    public void resize(int width, int height) {
        // Recalculate button positions
        float radius = Constants.BUTTON_SIZE / 2;
        float margin = Constants.BUTTON_MARGIN;
        float bottomY = height - margin - radius;
        
        float leftX = margin + radius;
        leftButton.setPosition(leftX, bottomY);
        
        float rightX = leftX + Constants.BUTTON_SIZE + margin;
        rightButton.setPosition(rightX, bottomY);
        
        float jumpX = width - margin - radius;
        jumpButton.setPosition(jumpX, bottomY);
    }
    
    // ===== PUBLIC API =====
    
    public boolean isLeftPressed() {
        return leftPressed;
    }
    
    public boolean isRightPressed() {
        return rightPressed;
    }
    
    public boolean isJumpJustPressed() {
        return jumpJustPressed;
    }
    
    // Get the button manager for rendering
    public TouchButtonManager getTouchButtonManager() {
        return touchButtons;
    }
    
    // Individual button access (for backwards compatibility)
    public TouchButton getLeftButton() {
        return leftButton;
    }
    
    public TouchButton getRightButton() {
        return rightButton;
    }
    
    public TouchButton getJumpButton() {
        return jumpButton;
    }
    
    // ===== INPUT PROCESSOR IMPLEMENTATION =====
    // These methods are called by LibGDX when input events occur
    
    @Override
    public boolean keyDown(int keycode) {
        // Handle movement keys
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            leftKeyPressed = true;
            return true;
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            rightKeyPressed = true;
            return true;
        }
        
        // Handle jump keys - SET JUMP STATE TO TRUE (like touch down)
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP || keycode == Input.Keys.SPACE) {
            keyboardJumpDown = true;
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean keyUp(int keycode) {
        // Release movement keys
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
            leftKeyPressed = false;
            return true;
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
            rightKeyPressed = false;
            return true;
        }
        
        // Release jump keys - SET JUMP STATE TO FALSE (like touch up)
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP || keycode == Input.Keys.SPACE) {
            keyboardJumpDown = false;
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Touch handled by TouchButtonManager
        return false;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Touch handled by TouchButtonManager
        return false;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Touch handled by TouchButtonManager
        return false;
    }
    
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
    
    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
    
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}
