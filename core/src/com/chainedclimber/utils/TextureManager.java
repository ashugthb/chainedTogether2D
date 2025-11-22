package com.chainedclimber.utils;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Efficient texture management with caching
 * Loads textures once and reuses them
 */
public class TextureManager {
    private static TextureManager instance;
    private Map<String, Texture> textureCache;
    private Map<String, TextureRegion> regionCache;
    
    private TextureManager() {
        textureCache = new HashMap<>();
        regionCache = new HashMap<>();
    }
    
    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }
    
    /**
     * Load or get cached texture
     */
    public Texture getTexture(String filename) {
        if (!textureCache.containsKey(filename)) {
            try {
                Texture texture = new Texture(Gdx.files.internal(filename));
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                textureCache.put(filename, texture);
                Gdx.app.log("TextureManager", "Loaded texture: " + filename);
            } catch (Exception e) {
                Gdx.app.error("TextureManager", "Failed to load texture: " + filename);
                return null;
            }
        }
        return textureCache.get(filename);
    }
    
    /**
     * Load or get cached texture region
     */
    public TextureRegion getTextureRegion(String filename) {
        if (!regionCache.containsKey(filename)) {
            Texture texture = getTexture(filename);
            if (texture != null) {
                TextureRegion region = new TextureRegion(texture);
                regionCache.put(filename, region);
            } else {
                return null;
            }
        }
        return regionCache.get(filename);
    }
    
    /**
     * Preload commonly used textures
     */
    public void preloadGameTextures() {
        Gdx.app.log("TextureManager", "Preloading game textures...");
        
        // Load all game textures
        getTexture("caveman.png");
        getTexture("platform.png");
        getTexture("ice.png");
        getTexture("ramp_right.png");
        getTexture("ramp_left.png");
        getTexture("spike.png");
        
        Gdx.app.log("TextureManager", "Textures preloaded: " + textureCache.size());
    }
    
    /**
     * Dispose all textures (call on game exit)
     */
    public void dispose() {
        for (Texture texture : textureCache.values()) {
            texture.dispose();
        }
        textureCache.clear();
        regionCache.clear();
        Gdx.app.log("TextureManager", "All textures disposed");
    }
    
    /**
     * Check if texture exists
     */
    public boolean hasTexture(String filename) {
        return textureCache.containsKey(filename) || Gdx.files.internal(filename).exists();
    }
}
