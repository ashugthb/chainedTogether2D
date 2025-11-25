package com.chainedclimber;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // DYNAMIC HIGH PERFORMANCE: 90 FPS for modern displays
        // Automatically adapts to 60Hz, 90Hz, 120Hz, 144Hz displays
        config.setForegroundFPS(90); // High refresh rate target
        config.useVsync(false); // No VSync for adaptive frame timing
        config.setIdleFPS(30); // Battery optimization when idle
        config.setTitle("ChainedClimber2D");
        config.setWindowedMode(1280, 720); // Default window size
        config.setResizable(true); // Allow resizing for dynamic viewport
        
        new Lwjgl3Application(new ChainedClimberGame(), config);
    }
}
