package com.chainedclimber.utils;

/**
 * Block types for the level matrix
 * Each block has unique properties and interactions
 */
public class BlockType {
    
    // Basic blocks (0-9)
    public static final int AIR = 0;
    public static final int PLATFORM = 1;
    public static final int SPIKE = 2;
    public static final int BOUNCY = 3;
    public static final int ICE = 4;
    public static final int MOVING_PLATFORM = 5;
    public static final int BREAKABLE = 6;
    public static final int CHECKPOINT = 7;
    public static final int GOAL = 8;
    public static final int COIN = 9;
    public static final int SPAWN_POINT = 19;  // Player spawn location
    public static final int RAMP_RIGHT = 17;  // Ramp going up to the right
    public static final int RAMP_LEFT = 18;   // Ramp going up to the left
    
    // Advanced blocks (10+)
    public static final int ONE_WAY_PLATFORM = 10;
    public static final int STICKY_WALL = 11;
    public static final int SPEED_BOOST = 12;
    public static final int TELEPORTER = 13;
    public static final int CHAIN_ANCHOR = 14;
    public static final int WIND_LEFT = 15;
    public static final int WIND_RIGHT = 16;
    
    /**
     * Check if block is solid (player can stand on)
     */
    public static boolean isSolid(int blockType) {
        switch (blockType) {
            case PLATFORM:
            case ICE:
            case MOVING_PLATFORM:
            case BREAKABLE:
            case BOUNCY:
            case RAMP_RIGHT:
            case RAMP_LEFT:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Check if block is hazardous (damages player)
     */
    public static boolean isHazard(int blockType) {
        return blockType == SPIKE;
    }
    
    /**
     * Check if block is collectible (disappears when touched)
     */
    public static boolean isCollectible(int blockType) {
        return blockType == COIN;
    }
    
    /**
     * Check if block is passable (player can move through)
     */
    public static boolean isPassable(int blockType) {
        switch (blockType) {
            case AIR:
            case CHECKPOINT:
            case GOAL:
            case COIN:
            case SPEED_BOOST:
            case TELEPORTER:
            case WIND_LEFT:
            case WIND_RIGHT:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Check if block has special interaction
     */
    public static boolean hasInteraction(int blockType) {
        switch (blockType) {
            case BOUNCY:
            case ICE:
            case CHECKPOINT:
            case GOAL:
            case COIN:
            case SPEED_BOOST:
            case TELEPORTER:
            case WIND_LEFT:
            case WIND_RIGHT:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get block name for debugging
     */
    public static String getName(int blockType) {
        switch (blockType) {
            case AIR: return "Air";
            case PLATFORM: return "Platform";
            case SPIKE: return "Spike";
            case BOUNCY: return "Bouncy";
            case ICE: return "Ice";
            case MOVING_PLATFORM: return "Moving Platform";
            case BREAKABLE: return "Breakable";
            case CHECKPOINT: return "Checkpoint";
            case GOAL: return "Goal";
            case COIN: return "Coin";
            case RAMP_RIGHT: return "Ramp Right";
            case RAMP_LEFT: return "Ramp Left";
            case SPAWN_POINT: return "Spawn Point";
            case ONE_WAY_PLATFORM: return "One-Way Platform";
            case STICKY_WALL: return "Sticky Wall";
            case SPEED_BOOST: return "Speed Boost";
            case TELEPORTER: return "Teleporter";
            case CHAIN_ANCHOR: return "Chain Anchor";
            case WIND_LEFT: return "Wind Left";
            case WIND_RIGHT: return "Wind Right";
            default: return "Unknown (" + blockType + ")";
        }
    }
    
    /**
     * Get visual character for matrix display
     */
    public static String getDisplayChar(int blockType) {
        switch (blockType) {
            case AIR: return "·";
            case PLATFORM: return "█";
            case SPIKE: return "▲";
            case BOUNCY: return "≈";
            case ICE: return "❄";
            case MOVING_PLATFORM: return "▬";
            case BREAKABLE: return "▒";
            case CHECKPOINT: return "⚑";
            case GOAL: return "★";
            case COIN: return "○";
            case RAMP_RIGHT: return "/";
            case RAMP_LEFT: return "\\\\";
            case SPAWN_POINT: return "@";
            case ONE_WAY_PLATFORM: return "⌃";
            case STICKY_WALL: return "▓";
            case SPEED_BOOST: return "»";
            case TELEPORTER: return "◉";
            case CHAIN_ANCHOR: return "⚓";
            case WIND_LEFT: return "←";
            case WIND_RIGHT: return "→";
            default: return "?";
        }
    }
    
    /**
     * Get color for rendering (RGB)
     */
    public static float[] getColor(int blockType) {
        switch (blockType) {
            case PLATFORM: return new float[]{0.5f, 0.5f, 0.5f}; // Gray
            case SPIKE: return new float[]{0.8f, 0.1f, 0.1f}; // Red
            case BOUNCY: return new float[]{0.2f, 0.8f, 0.9f}; // Cyan
            case ICE: return new float[]{0.7f, 0.9f, 1.0f}; // Light blue
            case MOVING_PLATFORM: return new float[]{0.6f, 0.6f, 0.3f}; // Yellow-gray
            case BREAKABLE: return new float[]{0.6f, 0.4f, 0.2f}; // Brown
            case CHECKPOINT: return new float[]{0.3f, 0.8f, 0.3f}; // Green
            case GOAL: return new float[]{1.0f, 0.8f, 0.0f}; // Gold
            case COIN: return new float[]{1.0f, 0.9f, 0.2f}; // Yellow
            case RAMP_RIGHT: return new float[]{0.7f, 0.6f, 0.4f}; // Tan
            case RAMP_LEFT: return new float[]{0.7f, 0.6f, 0.4f}; // Tan
            case SPAWN_POINT: return new float[]{0.0f, 1.0f, 0.0f}; // Bright green
            case ONE_WAY_PLATFORM: return new float[]{0.4f, 0.7f, 0.4f}; // Green
            case STICKY_WALL: return new float[]{0.5f, 0.3f, 0.2f}; // Dark brown
            case SPEED_BOOST: return new float[]{1.0f, 0.5f, 0.0f}; // Orange
            case TELEPORTER: return new float[]{0.6f, 0.2f, 0.8f}; // Purple
            case CHAIN_ANCHOR: return new float[]{0.3f, 0.3f, 0.3f}; // Dark gray
            case WIND_LEFT: return new float[]{0.7f, 0.7f, 0.9f}; // Light purple
            case WIND_RIGHT: return new float[]{0.7f, 0.7f, 0.9f}; // Light purple
            default: return new float[]{1.0f, 1.0f, 1.0f}; // White
        }
    }
}
