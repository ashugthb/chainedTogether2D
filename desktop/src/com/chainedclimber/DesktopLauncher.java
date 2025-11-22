package com.chainedclimber;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.chainedclimber.ChainedClimberGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("ChainedClimber2D");
        config.setWindowedMode(1280, 720); // Landscape mode
        config.setResizable(false);
        
        new Lwjgl3Application(new ChainedClimberGame(), config);
    }
}
