package com.chainedclimber;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;

/**
 * JUnit5 extension for running LibGDX tests in headless mode.
 * This allows testing LibGDX components without requiring a graphics context.
 * 
 * Usage: @ExtendWith(GdxTestRunner.class)
 */
public class GdxTestRunner implements BeforeAllCallback, AfterAllCallback {
    
    private static HeadlessApplication application;
    private static final Map<String, Object> testContext = new HashMap<>();
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (application == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            config.updatesPerSecond = 60;
            
            application = new HeadlessApplication(new ApplicationListener() {
                @Override
                public void create() {
                    // Mock GL20 for headless testing
                    Gdx.gl = Mockito.mock(GL20.class);
                    Gdx.gl20 = Mockito.mock(GL20.class);
                }
                
                @Override
                public void resize(int width, int height) {}
                
                @Override
                public void render() {}
                
                @Override
                public void pause() {}
                
                @Override
                public void resume() {}
                
                @Override
                public void dispose() {}
            }, config);
            
            // Give the application time to initialize
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // Keep application running for other tests
        // It will be cleaned up when JVM exits
    }
    
    /**
     * Store test context data that can be shared across tests
     */
    public static void setTestContext(String key, Object value) {
        testContext.put(key, value);
    }
    
    /**
     * Retrieve test context data
     */
    public static Object getTestContext(String key) {
        return testContext.get(key);
    }
    
    /**
     * Clear test context
     */
    public static void clearTestContext() {
        testContext.clear();
    }
}
