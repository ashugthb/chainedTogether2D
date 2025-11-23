package com.chainedclimber.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.chainedclimber.ChainedClimberGame;
import com.chainedclimber.utils.Constants;

public class MainMenuScreen implements Screen {
    private ChainedClimberGame game;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private BitmapFont buttonFont;
    private Rectangle playButton;
    private GlyphLayout layout;
    
    public MainMenuScreen(ChainedClimberGame game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.layout = new GlyphLayout();
        
        // Create fonts
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3.0f); // Large title
        this.titleFont.setColor(Color.WHITE);
        
        this.subtitleFont = new BitmapFont();
        this.subtitleFont.getData().setScale(1.5f); // Subtitle
        this.subtitleFont.setColor(0.8f, 0.8f, 0.8f, 1); // Light gray
        
        this.buttonFont = new BitmapFont();
        this.buttonFont.getData().setScale(2.0f); // Medium button text
        this.buttonFont.setColor(Color.WHITE);
        
        // Create play button (centered, lower part of screen)
        // Use window dimensions for menu, not world dimensions
        float buttonWidth = 240;
        float buttonHeight = 80;
        float buttonX = (1280 - buttonWidth) / 2; // Use window width
        float buttonY = 720 / 3; // Use window height
        this.playButton = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);
    }
    
    @Override
    public void show() {
    }
    
    @Override
    public void render(float delta) {
        // Clear screen with dark background
        Gdx.gl.glClearColor(Constants.BACKGROUND_COLOR[0], Constants.BACKGROUND_COLOR[1], 
                           Constants.BACKGROUND_COLOR[2], 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Draw shapes (button background)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw play button background
        shapeRenderer.setColor(0.2f, 0.7f, 0.2f, 1); // Green button
        shapeRenderer.rect(playButton.x, playButton.y, playButton.width, playButton.height);
        
        shapeRenderer.end();
        
        // Draw button border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(0.15f, 0.55f, 0.15f, 1); // Darker green border
        shapeRenderer.rect(playButton.x, playButton.y, playButton.width, playButton.height);
        shapeRenderer.end();
        
        // Draw text
        batch.begin();
        
        // Draw title "ChainedClimber 2D"
        String titleText = "ChainedClimber 2D";
        layout.setText(titleFont, titleText);
        float titleX = (1280 - layout.width) / 2; // Use window width
        float titleY = 720 * 0.7f; // Use window height
        titleFont.draw(batch, titleText, titleX, titleY);
        
        // Draw subtitle
        String subtitleText = "Climb Together!";
        layout.setText(subtitleFont, subtitleText);
        float subtitleX = (1280 - layout.width) / 2; // Use window width
        float subtitleY = 720 * 0.65f; // Use window height
        subtitleFont.draw(batch, subtitleText, subtitleX, subtitleY);
        
        // Draw "PLAY" text on button
        String buttonText = "PLAY";
        layout.setText(buttonFont, buttonText);
        float textX = playButton.x + (playButton.width - layout.width) / 2;
        float textY = playButton.y + (playButton.height + layout.height) / 2;
        buttonFont.draw(batch, buttonText, textX, textY);
        
        batch.end();
        
        // Check for input
        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Flip Y
            
            // Convert to screen coordinates
            touchX = touchX * 1280 / Gdx.graphics.getWidth(); // Use window width
            touchY = touchY * 720 / Gdx.graphics.getHeight(); // Use window height
            
            if (playButton.contains(touchX, touchY)) {
                // Start game
                game.setScreen(new GameScreen(game));
                dispose();
            }
        }
    }
    
    @Override
    public void resize(int width, int height) {
    }
    
    @Override
    public void pause() {
    }
    
    @Override
    public void resume() {
    }
    
    @Override
    public void hide() {
    }
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        titleFont.dispose();
        subtitleFont.dispose();
        buttonFont.dispose();
    }
}
