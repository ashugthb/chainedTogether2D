package com.chainedclimber;

import com.badlogic.gdx.Game;
import com.chainedclimber.screens.MainMenuScreen;

public class ChainedClimberGame extends Game {
    
    @Override
    public void create() {
        // Start with the main menu
        setScreen(new MainMenuScreen(this));
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
