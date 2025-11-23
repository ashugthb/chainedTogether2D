package com.chainedclimber;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.chainedclimber.ChainedClimberGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60); // Target 60 FPS (better for mobile/slower devices)
        config.useVsync(false); // Disable VSync for consistent frame timing on all devices
        config.setIdleFPS(30); // Save battery when window not focused
        config.setTitle("ChainedClimber2D");
        config.setWindowedMode(1280, 720); // Default window size
        config.setResizable(true); // Allow resizing for dynamic viewport
        
        new Lwjgl3Application(new ChainedClimberGame(), config);
    }
}
