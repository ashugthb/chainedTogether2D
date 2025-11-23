package com.chainedclimber.utils;

public class Constants {
    // Screen/World dimensions - Reduced for smaller visible area
    // Tighter camera focus on player for vertical climbing
    public static final int WORLD_WIDTH = 640;
    public static final int WORLD_HEIGHT = 480;
    
    // Physics constants
    public static final float GRAVITY = 1200f;  // Pixels per second squared
    public static final float JUMP_VELOCITY = 650f;  // Pixels per second
    public static final float MOVE_SPEED = 250f;  // Pixels per second
    public static final float MAX_FALL_SPEED = 800f;  // Pixels per second
    
    // Player dimensions (scaled to match reference image - player is ~1.5x platform height)
    // Using 2:3 aspect ratio for better character proportions
    public static final float PLAYER_WIDTH = 53f;   // ~0.67x platform width
    public static final float PLAYER_HEIGHT = 80f;  // 1.5x player width for proper proportions
    
    // Platform dimensions (square blocks like in reference image)
    public static final float PLATFORM_HEIGHT = 80f;  // Square platforms (80x80)
    public static final float GROUND_PLATFORM_WIDTH = 1280f; // Full width for landscape
    public static final float PLATFORM_WIDTH = 80f;  // Square platforms
    
    // Colors (RGB values 0-1)
    public static final float[] BACKGROUND_COLOR = {0.1f, 0.1f, 0.18f};  // Dark blue
    public static final float[] PLAYER_COLOR = {0f, 1f, 0f};  // Bright green
    public static final float[] PLATFORM_COLOR = {0.53f, 0.53f, 0.53f};  // Gray
    public static final float[] GROUND_COLOR = {0.33f, 0.33f, 0.33f};  // Dark gray
    
    // Button dimensions
    public static final float BUTTON_SIZE = 100f;  // Touch-friendly buttons
    public static final float BUTTON_MARGIN = 20f;
}
