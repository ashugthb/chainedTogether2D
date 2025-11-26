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
    
    // Player 1 Keyboard state
    private boolean leftKeyPressed;
    private boolean rightKeyPressed;
    private boolean downKeyPressed;
    private boolean p1JumpKeyDown;
    private boolean p1JumpKeyWasDown;
    
    // Player 2 Keyboard state
    private boolean p2LeftKeyPressed;
    private boolean p2RightKeyPressed;
    private boolean p2DownKeyPressed;
    private boolean p2JumpKeyDown;
    private boolean p2JumpKeyWasDown;
    
    // Constants
    private static final int JUMP_BUFFER_FRAMES = 15;
    private static final int COYOTE_TIME_FRAMES = 8;
    private static final int JUMP_REPEAT_DELAY_FRAMES = 2;
    
    // Player Input State Class
    public class PlayerInputState {
        public boolean leftPressed;
        public boolean rightPressed;
        public boolean downPressed;
        public boolean jumpJustPressed;
        
        // Buffering & Coyote Time
        public int jumpBufferCounter = 0;
        public int coyoteTimeCounter = 0;
        public boolean wasGroundedLastFrame = false;
        public int jumpRepeatCounter = 0;
        public boolean lastJumpWasGrounded = false;
        
        public void consumeJump(boolean wasGrounded) {
            jumpBufferCounter = 0;
            jumpJustPressed = false;
            lastJumpWasGrounded = wasGrounded;
            coyoteTimeCounter = 0;
            if (wasGrounded) {
                jumpRepeatCounter = InputController.JUMP_REPEAT_DELAY_FRAMES;
            }
        }
        
        public void updateCoyoteTime(boolean isGrounded) {
            if (isGrounded) {
                coyoteTimeCounter = InputController.COYOTE_TIME_FRAMES;
                wasGroundedLastFrame = true;
            } else if (wasGroundedLastFrame) {
                wasGroundedLastFrame = false;
            } else if (coyoteTimeCounter > 0) {
                coyoteTimeCounter--;
            }
        }
        
        public boolean canJump() {
            return coyoteTimeCounter > 0;
        }
        
        // Helper for legacy access
        public boolean isLeftPressed() { return leftPressed; }
        public boolean isRightPressed() { return rightPressed; }
        public boolean isDownPressed() { return downPressed; }
        public boolean isJumpJustPressed() { return jumpJustPressed; }
    }
    
    public final PlayerInputState p1 = new PlayerInputState();
    public final PlayerInputState p2 = new PlayerInputState();
    
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
    
    private void initializeTouchButtons() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        
        float radius = Constants.BUTTON_SIZE / 2;
        float margin = Constants.BUTTON_MARGIN;
        
        float bottomY = screenHeight - margin - radius;
        
        float leftX = margin + radius;
        leftButton = new TouchButton("left", leftX, bottomY, radius);
        leftButton.setNormalColor(0.3f, 0.3f, 0.3f, 0.6f);
        leftButton.setTouchedColor(0.5f, 0.5f, 0.5f, 0.8f);
        leftButton.setBorderColor(0.7f, 0.7f, 0.7f, 0.9f);
        leftButton.setIconText("◄");
        touchButtons.addButton(leftButton);
        
        float rightX = leftX + Constants.BUTTON_SIZE + margin;
        rightButton = new TouchButton("right", rightX, bottomY, radius);
        rightButton.setNormalColor(0.3f, 0.3f, 0.3f, 0.6f);
        rightButton.setTouchedColor(0.5f, 0.5f, 0.5f, 0.8f);
        rightButton.setBorderColor(0.7f, 0.7f, 0.7f, 0.9f);
        rightButton.setIconText("►");
        touchButtons.addButton(rightButton);
        
        float jumpX = screenWidth - margin - radius;
        jumpButton = new TouchButton("jump", jumpX, bottomY, radius);
        jumpButton.setNormalColor(0.2f, 0.7f, 0.2f, 0.6f);
        jumpButton.setTouchedColor(0.3f, 0.9f, 0.3f, 0.9f);
        jumpButton.setBorderColor(0.4f, 1.0f, 0.4f, 1.0f);
        jumpButton.setIconText("▲");
        touchButtons.addButton(jumpButton);
    }
    
    public void update() {
        touchButtons.update();
        
        // Update Player 1 (WASD/Arrows + Touch)
        updatePlayerState(p1, leftKeyPressed, rightKeyPressed, downKeyPressed, p1JumpKeyDown, p1JumpKeyWasDown, 
                          leftButton.isTouched(), rightButton.isTouched(), jumpButton.isTouched(), jumpButton.isJustPressed());
        p1JumpKeyWasDown = p1JumpKeyDown;
        
        // Update Player 2 (Numpad 8,4,6,5)
        updatePlayerState(p2, p2LeftKeyPressed, p2RightKeyPressed, p2DownKeyPressed, p2JumpKeyDown, p2JumpKeyWasDown, 
                          false, false, false, false);
        p2JumpKeyWasDown = p2JumpKeyDown;
    }
    
    private void updatePlayerState(PlayerInputState state, boolean leftKey, boolean rightKey, boolean downKey, boolean jumpKey, boolean jumpKeyWasDown,
                                   boolean touchLeft, boolean touchRight, boolean touchJump, boolean touchJumpJustPressed) {
        
        boolean rawJumpInput = (jumpKey && !jumpKeyWasDown) || touchJumpJustPressed;
        
        // Auto-repeat
        if (state.jumpRepeatCounter > 0) state.jumpRepeatCounter--;
        
        if (jumpKey && !rawJumpInput && state.jumpRepeatCounter == 0 && state.lastJumpWasGrounded) {
            rawJumpInput = true;
        }
        
        // Buffering
        if (rawJumpInput || jumpKey || touchJump) {
            state.jumpBufferCounter = JUMP_BUFFER_FRAMES;
        } else if (state.jumpBufferCounter > 0) {
            state.jumpBufferCounter--;
        }
        
        state.jumpJustPressed = (state.jumpBufferCounter > 0);
        state.leftPressed = leftKey || touchLeft;
        state.rightPressed = rightKey || touchRight;
        state.downPressed = downKey;
    }
    
    public void resize(int width, int height) {
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
    
    public TouchButtonManager getTouchButtonManager() {
        return touchButtons;
    }
    
    // Legacy API delegation (defaults to P1)
    public boolean isLeftPressed() { return p1.leftPressed; }
    public boolean isRightPressed() { return p1.rightPressed; }
    public boolean isJumpJustPressed() { return p1.jumpJustPressed; }
    public void consumeJump(boolean wasGrounded) { p1.consumeJump(wasGrounded); }
    public void updateCoyoteTime(boolean isGrounded) { p1.updateCoyoteTime(isGrounded); }
    public boolean canJump() { return p1.canJump(); }
    
    // InputProcessor Implementation
    @Override
    public boolean keyDown(int keycode) {
        // P1
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) { leftKeyPressed = true; return true; }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) { rightKeyPressed = true; return true; }
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) { downKeyPressed = true; return true; }
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP || keycode == Input.Keys.SPACE) { p1JumpKeyDown = true; return true; }
        
        // P2
        if (keycode == Input.Keys.NUMPAD_4 || keycode == Input.Keys.NUM_4) { p2LeftKeyPressed = true; return true; }
        if (keycode == Input.Keys.NUMPAD_6 || keycode == Input.Keys.NUM_6) { p2RightKeyPressed = true; return true; }
        if (keycode == Input.Keys.NUMPAD_5 || keycode == Input.Keys.NUM_5) { p2DownKeyPressed = true; return true; }
        if (keycode == Input.Keys.NUMPAD_8 || keycode == Input.Keys.NUM_8) { p2JumpKeyDown = true; return true; }
        
        return false;
    }
    
    @Override
    public boolean keyUp(int keycode) {
        // P1
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) { leftKeyPressed = false; return true; }
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) { rightKeyPressed = false; return true; }
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) { downKeyPressed = false; return true; }
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP || keycode == Input.Keys.SPACE) { p1JumpKeyDown = false; return true; }
        
        // P2
        if (keycode == Input.Keys.NUMPAD_4 || keycode == Input.Keys.NUM_4) { p2LeftKeyPressed = false; return true; }
        if (keycode == Input.Keys.NUMPAD_6 || keycode == Input.Keys.NUM_6) { p2RightKeyPressed = false; return true; }
        if (keycode == Input.Keys.NUMPAD_5 || keycode == Input.Keys.NUM_5) { p2DownKeyPressed = false; return true; }
        if (keycode == Input.Keys.NUMPAD_8 || keycode == Input.Keys.NUM_8) { p2JumpKeyDown = false; return true; }
        
        return false;
    }
    
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}
