package com.chainedclimber.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.chainedclimber.utils.Constants;

public class InputController {
    // Touch button positions
    private Vector2 leftButtonPos;
    private Vector2 rightButtonPos;
    private Vector2 jumpButtonPos;
    
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean jumpPressed;
    private boolean jumpJustPressed;
    
    public InputController() {
        // Position buttons at BOTTOM for easy thumb access
        // Left and Right buttons - bottom-left corner
        leftButtonPos = new Vector2(Constants.BUTTON_MARGIN, Constants.BUTTON_MARGIN);
        rightButtonPos = new Vector2(Constants.BUTTON_MARGIN * 2 + Constants.BUTTON_SIZE, Constants.BUTTON_MARGIN);
        
        // Jump button - bottom-right corner (easy right thumb access)
        jumpButtonPos = new Vector2(Constants.WORLD_WIDTH - Constants.BUTTON_SIZE - Constants.BUTTON_MARGIN, 
                                     Constants.BUTTON_MARGIN);
    }
    
    public void update() {
        leftPressed = false;
        rightPressed = false;
        jumpJustPressed = false;
        
        // Check keyboard (for desktop testing)
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            leftPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            rightPressed = true;
        }
        // Jump triggers immediately when pressed (not just on first frame)
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            jumpJustPressed = true;
        }
        
        // Check touch (for Android)
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                float touchX = Gdx.input.getX(i);
                float touchY = Constants.WORLD_HEIGHT - Gdx.input.getY(i); // Flip Y coordinate
                
                // Convert screen coordinates to world coordinates
                touchX = touchX * Constants.WORLD_WIDTH / Gdx.graphics.getWidth();
                touchY = touchY * Constants.WORLD_HEIGHT / Gdx.graphics.getHeight();
                
                // Check left button
                if (isTouchInButton(touchX, touchY, leftButtonPos)) {
                    leftPressed = true;
                }
                
                // Check right button
                if (isTouchInButton(touchX, touchY, rightButtonPos)) {
                    rightPressed = true;
                }
                
                // Check jump button
                if (isTouchInButton(touchX, touchY, jumpButtonPos)) {
                    jumpPressed = true;
                }
            }
        }
        
        // Handle jump (just pressed logic for touch)
        if (jumpPressed) {
            jumpJustPressed = true;
            jumpPressed = false; // Reset for next frame
        }
    }
    
    private boolean isTouchInButton(float touchX, float touchY, Vector2 buttonPos) {
        return touchX >= buttonPos.x && touchX <= buttonPos.x + Constants.BUTTON_SIZE &&
               touchY >= buttonPos.y && touchY <= buttonPos.y + Constants.BUTTON_SIZE;
    }
    
    public boolean isLeftPressed() {
        return leftPressed;
    }
    
    public boolean isRightPressed() {
        return rightPressed;
    }
    
    public boolean isJumpJustPressed() {
        return jumpJustPressed;
    }
    
    public Vector2 getLeftButtonPos() {
        return leftButtonPos;
    }
    
    public Vector2 getRightButtonPos() {
        return rightButtonPos;
    }
    
    public Vector2 getJumpButtonPos() {
        return jumpButtonPos;
    }
}
