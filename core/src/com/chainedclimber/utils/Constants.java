package com.chainedclimber.utils;

public class Constants {
    // Screen/World dimensions (Landscape mode)
    public static final int WORLD_WIDTH = 1280;
    public static final int WORLD_HEIGHT = 800;
    
    // Physics constants
    public static final float GRAVITY = 1200f;  // Pixels per second squared
    public static final float JUMP_VELOCITY = 650f;  // Pixels per second (increased for better reach)
    public static final float MOVE_SPEED = 200f;  // Pixels per second
    public static final float MAX_FALL_SPEED = 800f;  // Pixels per second
    
    // Player dimensions
    public static final float PLAYER_WIDTH = 32f;
    public static final float PLAYER_HEIGHT = 64f;
    
    // Platform dimensions
    public static final float PLATFORM_HEIGHT = 30f;
    public static final float GROUND_PLATFORM_WIDTH = 1280f; // Full width for landscape
    public static final float PLATFORM_WIDTH = 200f;
    
    // Colors (RGB values 0-1)
    public static final float[] BACKGROUND_COLOR = {0.1f, 0.1f, 0.18f};  // Dark blue
    public static final float[] PLAYER_COLOR = {0f, 1f, 0f};  // Bright green
    public static final float[] PLATFORM_COLOR = {0.53f, 0.53f, 0.53f};  // Gray
    public static final float[] GROUND_COLOR = {0.33f, 0.33f, 0.33f};  // Dark gray
    
    // Button dimensions
    public static final float BUTTON_SIZE = 100f;
    public static final float BUTTON_MARGIN = 20f;
}
