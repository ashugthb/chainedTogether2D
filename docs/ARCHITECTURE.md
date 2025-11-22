# Architecture Documentation

## ChainedClimber2D - Technical Architecture

This document describes the technical architecture, design patterns, and code organization of **ChainedClimber2D**.

## Technology Stack

### Core Framework

**LibGDX 1.12.1**
- Battle-tested Java game development framework
- Cross-platform (Android, Desktop, iOS, HTML5)
- Excellent 2D rendering performance
- Built-in utilities for common game tasks

### Why LibGDX?

| Feature | Benefit |
|---------|---------|
| Cross-platform | Write once, deploy to multiple platforms |
| Performance | Native-level performance on Android |
| Active Community | Extensive documentation and support |
| Mature | Stable API, used in thousands of released games |
| Free & Open Source | No licensing fees |

### Build System

**Gradle 7.4+**
- Dependency management
- Multi-module project structure
- Android build integration
- Automated build tasks

## Project Structure

### Module Organization

```
ChainedClimber2D/
├── core/          # Platform-independent game logic
├── android/       # Android-specific code
└── desktop/       # Desktop launcher (testing)
```

**Module Dependency Graph:**

```
┌──────────┐
│ android  │───┐
└──────────┘   │
               ├──→ ┌──────┐
┌──────────┐   │    │ core │
│ desktop  │───┘    └──────┘
└──────────┘
```

Both `android` and `desktop` modules depend on `core`, but not on each other.

### Core Module Structure

```
core/src/com/chainedclimber/
├── ChainedClimberGame.java       # Main game class
├── screens/                       # Screen implementations
│   ├── MainMenuScreen.java       # Main menu
│   └── GameScreen.java           # Gameplay screen
├── entities/                      # Game entities
│   ├── Player.java               # Player character
│   └── Platform.java             # Platform objects
├── systems/                       # Game systems
│   └── InputController.java      # Input handling
└── utils/                         # Utilities
    └── Constants.java            # Game constants
```

## Architecture Patterns

### Screen Pattern

LibGDX uses a **Screen-based architecture** for managing different game states (menu, gameplay, settings, etc.).

**Interface:**
```java
public interface Screen {
    void show();           // Called when screen becomes active
    void render(float delta);  // Called every frame
    void resize(int width, int height);  // Called on window resize
    void pause();          // Called when app loses focus
    void resume();         // Called when app regains focus
    void hide();           // Called when screen is no longer active
    void dispose();        // Called when screen is destroyed
}
```

**Implementation:**
- `MainMenuScreen` - Handles menu UI and navigation
- `GameScreen` - Handles gameplay logic and rendering

**Screen Management:**
```java
// In ChainedClimberGame.java
public void setScreen(Screen screen) {
    if (currentScreen != null) {
        currentScreen.hide();
    }
    currentScreen = screen;
    currentScreen.show();
}
```

### Entity-Component Pattern (Simplified)

Entities (`Player`, `Platform`) are self-contained objects with:
- **State**: Position, velocity, dimensions
- **Logic**: Update methods
- **Rendering**: Draw methods

**Example:**
```java
public class Player {
    // State
    private Vector2 position;
    private Vector2 velocity;
    
    // Logic
    public void update(float deltaTime) {
        // Apply physics
    }
    
    // Rendering
    public void render(ShapeRenderer renderer) {
        // Draw player
    }
}
```

### Game Loop Pattern

The classic game loop is implemented in LibGDX's `render()` method:

```
┌─────────────────────┐
│   Process Input     │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   Update Game       │
│   (Physics, AI)     │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│   Render Graphics   │
└──────────┬──────────┘
           │
           └───────────► Repeat (60 FPS target)
```

**Implementation:**
```java
@Override
public void render(float delta) {
    // 1. Process Input
    handleInput();
    
    // 2. Update Game State
    updatePhysics(delta);
    checkCollisions();
    
    // 3. Render
    clearScreen();
    renderEntities();
}
```

## Core Systems

### Rendering System

**LibGDX Rendering Pipeline:**

```
Camera → Viewport → SpriteBatch/ShapeRenderer → Screen
```

**Components:**

1. **Camera** (`OrthographicCamera`)
   - Defines what portion of the game world is visible
   - Can follow the player vertically
   - Transforms world coordinates to screen coordinates

2. **Viewport** (`FitViewport`)
   - Handles different screen sizes/aspect ratios
   - Maintains consistent game world dimensions
   - Adds letterboxing/pillarboxing as needed

3. **ShapeRenderer**
   - Draws primitive shapes (rectangles, circles)
   - Used for minimal graphics in V1
   - Will be replaced with SpriteBatch (sprites) in future versions

**Rendering Order:**
```java
camera.update();
shapeRenderer.setProjectionMatrix(camera.combined);
shapeRenderer.begin(ShapeType.Filled);
    // Render platforms
    // Render player
shapeRenderer.end();
```

### Physics System

**Custom Lightweight Physics** (no Box2D in V1 for simplicity)

**Components:**
- Gravity: Constant downward acceleration
- Velocity: 2D vector (X, Y)
- Position: 2D vector (X, Y)

**Update Loop:**
```java
public void updatePhysics(float deltaTime) {
    // Apply gravity
    velocity.y -= Constants.GRAVITY * deltaTime;
    
    // Clamp fall speed
    if (velocity.y < -Constants.MAX_FALL_SPEED) {
        velocity.y = -Constants.MAX_FALL_SPEED;
    }
    
    // Update position
    position.x += velocity.x * deltaTime;
    position.y += velocity.y * deltaTime;
    
    // Check collisions
    checkPlatformCollisions();
}
```

**Collision Detection:**
Simple AABB (Axis-Aligned Bounding Box) collision:

```java
public boolean collides(Rectangle a, Rectangle b) {
    return a.x < b.x + b.width &&
           a.x + a.width > b.x &&
           a.y < b.y + b.height &&
           a.y + a.height > b.y;
}
```

### Input System

**Desktop Input:**
- LibGDX `Input.isKeyPressed()` for keyboard
- Polled every frame in `handleInput()`

**Mobile Input:**
- Custom on-screen buttons
- LibGDX `Gdx.input.isTouched()` for touch detection
- Circle-based touch areas for buttons

**Input Controller Pattern:**
```java
public class InputController {
    public boolean isLeftPressed();
    public boolean isRightPressed();
    public boolean isJumpPressed();
}
```

This abstraction allows the same game logic to work with both keyboard and touch input.

### Asset Management

**LibGDX AssetManager** (for future versions)

Currently using simple initialization:
```java
shapeRenderer = new ShapeRenderer();
camera = new OrthographicCamera();
```

Future versions will use AssetManager for:
- Texture loading
- Sound effects
- Music
- Fonts

## Class Responsibility Breakdown

### ChainedClimberGame

**Responsibility:** Application lifecycle and screen management

**Key Methods:**
- `create()` - Initialize game, set first screen
- `dispose()` - Clean up resources
- `setScreen()` - Change active screen (inherited from `Game`)

**Dependencies:** None (top-level)

---

### MainMenuScreen

**Responsibility:** Display menu, handle navigation

**Key Methods:**
- `show()` - Setup menu UI
- `render(float delta)` - Draw menu, detect button clicks
- `dispose()` - Clean up menu resources

**Dependencies:**
- `ChainedClimberGame` - To switch to GameScreen

---

### GameScreen

**Responsibility:** Gameplay loop, entity management

**Key Methods:**
- `show()` - Initialize game entities
- `render(float delta)` - Game loop (input, update, render)
- `resize()` - Handle screen size changes
- `dispose()` - Clean up game resources

**State:**
- `Player player`
- `List<Platform> platforms`
- `OrthographicCamera camera`
- `ShapeRenderer shapeRenderer`
- `InputController inputController`

**Dependencies:**
- `Player` - Player entity
- `Platform` - Platform entities
- `InputController` - Input handling

---

### Player

**Responsibility:** Player character state and logic

**State:**
- `Vector2 position` - Current position
- `Vector2 velocity` - Current velocity
- `boolean grounded` - On platform?
- `Rectangle bounds` - Collision box

**Key Methods:**
- `update(float delta)` - Apply physics
- `render(ShapeRenderer renderer)` - Draw player
- `moveLeft()` / `moveRight()` - Horizontal movement
- `jump()` - Apply jump velocity (if grounded)
- `checkCollision(Platform platform)` - Collision detection

**Dependencies:**
- `Constants` - Physics values

---

### Platform

**Responsibility:** Platform entity

**State:**
- `Rectangle bounds` - Position and size

**Key Methods:**
- `render(ShapeRenderer renderer)` - Draw platform

**Dependencies:** None (data class)

---

### InputController

**Responsibility:** Abstract input handling

**Key Methods:**
- `update()` - Check input state
- `isLeftPressed()` - Is left being pressed?
- `isRightPressed()` - Is right being pressed?
- `isJumpPressed()` - Is jump being pressed?

**Platform-Specific:**
- Desktop: Checks keyboard
- Android: Checks touch input on virtual buttons

**Dependencies:**
- `Gdx.input` - LibGDX input system

---

### Constants

**Responsibility:** Centralized configuration

**Values:**
```java
public class Constants {
    // Screen
    public static final int WORLD_WIDTH = 800;
    public static final int WORLD_HEIGHT = 1280;
    
    // Physics
    public static final float GRAVITY = 1200f;
    public static final float JUMP_VELOCITY = 600f;
    public static final float MOVE_SPEED = 200f;
    public static final float MAX_FALL_SPEED = 800f;
    
    // Player
    public static final float PLAYER_WIDTH = 32f;
    public static final float PLAYER_HEIGHT = 64f;
    
    // Platforms
    public static final float PLATFORM_HEIGHT = 30f;
}
```

## Data Flow

### Game Initialization

```
App Start
    ↓
AndroidLauncher.onCreate()
    ↓
LibGDX initializes
    ↓
ChainedClimberGame.create()
    ↓
Set MainMenuScreen as active
    ↓
MainMenuScreen.show()
```

### Menu → Game Transition

```
User taps "PLAY"
    ↓
MainMenuScreen detects click
    ↓
game.setScreen(new GameScreen(game))
    ↓
MainMenuScreen.hide()
    ↓
GameScreen.show()
    ↓
Initialize player, platforms, camera
    ↓
Game loop starts
```

### Game Loop Data Flow

```
┌─────────────────────────────────┐
│ GameScreen.render(delta)        │
└────────────┬────────────────────┘
             │
    ┌────────▼────────┐
    │ Input           │
    │ controller.     │
    │ update()        │
    └────────┬────────┘
             │
    ┌────────▼────────────┐
    │ Player              │
    │ player.update()     │
    │  - Apply physics    │
    │  - Check collisions │
    └────────┬────────────┘
             │
    ┌────────▼────────────┐
    │ Camera              │
    │ Follow player       │
    └────────┬────────────┘
             │
    ┌────────▼────────────┐
    │ Render              │
    │  - Platforms        │
    │  - Player           │
    │  - UI               │
    └─────────────────────┘
```

## Android Integration

### AndroidManifest.xml

**Key Configurations:**
- Screen orientation: Portrait
- Hardware acceleration: Enabled
- Permissions: None required (offline game)

### Native Library Handling

LibGDX includes native libraries for optimal performance:
- Graphics rendering
- Audio playback
- Input handling

Gradle automatically packages these in the APK.

### Android Lifecycle

LibGDX abstracts Android lifecycle events:

| Android Event | LibGDX Method |
|---------------|---------------|
| `onCreate()` | `create()` |
| `onPause()` | `pause()` |
| `onResume()` | `resume()` |
| `onDestroy()` | `dispose()` |

## Performance Considerations

### Frame Rate

**Target:** 60 FPS

**Optimization Strategies:**
- Minimal draw calls (batch rendering in future)
- Simple physics (no complex simulations)
- Object pooling (future optimization)
- Avoid garbage collection during gameplay

### Memory Management

**Asset Management:**
- Currently minimal assets (just ShapeRenderer)
- Future: Use AssetManager for efficient loading/unloading
- Dispose of resources in `dispose()` methods

**Object Creation:**
- Avoid creating objects in update loop
- Reuse Vector2 objects where possible
- Use object pools for frequently created objects (future)

### Battery Efficiency

- Cap frame rate at 60 FPS (no unlimited rendering)
- Pause rendering when app is in background
- Minimal background processing

## Testing Strategy

### Desktop Testing

**Advantages:**
- Instant build and run
- Easy debugging
- Hot reload (with certain IDE setups)

**Usage:**
Primary development platform. Test gameplay mechanics and logic on desktop before deploying to Android.

### Android Emulator

**Usage:**
Test Android-specific features:
- Touch controls
- Screen sizes
- Performance on target API levels

### Physical Device

**Usage:**
Final validation before release:
- Real performance testing
- Actual touch experience
- Battery impact

## Future Architecture Enhancements

### Multiplayer Architecture (V3)

**Client-Server Model:**
- Server: Authoritative game state
- Clients: Send input, receive state updates
- Prediction: Client-side prediction for responsiveness
- Reconciliation: Correct client state based on server

**Technology Options:**
- WebSockets for real-time communication
- LibGDX Net API
- Custom protocol for game state synchronization

### Entity Component System (Future)

For more complex games, consider:
- Ashley (LibGDX's ECS library)
- Separate data (components) from logic (systems)
- Better performance for many entities

### State Management (Future)

**For more complex UI:**
- Menu stack for navigating between screens
- Game state patterns (paused, playing, game over)
- Save/load system

## Build Process

### Gradle Tasks

**Common Tasks:**
```bash
# Desktop
gradlew desktop:run

# Android Debug
gradlew android:assembleDebug

# Android Release
gradlew android:assembleRelease

# Clean
gradlew clean
```

### Build Flow

```
Source Code (.java)
    ↓
Gradle Compile
    ↓
[Desktop]           [Android]
    ↓                   ↓
Desktop JAR         DEX Files
    ↓                   ↓
Run locally         Package APK
                        ↓
                    Sign APK
                        ↓
                    Install
```

## Dependencies

### Core Module

```gradle
dependencies {
    api "com.badlogicgames.gdx:gdx:1.12.1"
}
```

### Android Module

```gradle
dependencies {
    implementation project(":core")
    api "com.badlogicgames.gdx:gdx-backend-android:1.12.1"
    natives "com.badlogicgames.gdx:gdx-platform:1.12.1:natives-armeabi-v7a"
    natives "com.badlogicgames.gdx:gdx-platform:1.12.1:natives-arm64-v8a"
    natives "com.badlogicgames.gdx:gdx-platform:1.12.1:natives-x86"
    natives "com.badlogicgames.gdx:gdx-platform:1.12.1:natives-x86_64"
}
```

### Desktop Module

```gradle
dependencies {
    implementation project(":core")
    api "com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.1"
    api "com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop"
}
```

## Debugging

### Logging

LibGDX provides built-in logging:

```java
Gdx.app.log("ChainedClimber", "Player position: " + player.getPosition());
```

**Log Levels:**
- `LOG` - General info
- `DEBUG` - Debug information
- `ERROR` - Errors

**View Android Logs:**
```bash
adb logcat | findstr ChainedClimber
```

### Visual Debugging

**Debug Rendering:**
```java
if (DEBUG_MODE) {
    shapeRenderer.begin(ShapeType.Line);
    // Draw collision boxes
    shapeRenderer.rect(player.getBounds());
    shapeRenderer.end();
}
```

## Code Style Guidelines

**Naming Conventions:**
- Classes: `PascalCase`
- Methods: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Variables: `camelCase`

**Organization:**
- Group related methods
- Public methods first, private methods last
- Keep methods short and focused
- Comment complex logic

## Resources

- **LibGDX Wiki**: https://libgdx.com/wiki/
- **LibGDX API**: https://libgdx.badlogicgames.com/ci/nightlies/docs/api/
- **Gradle Docs**: https://docs.gradle.org/

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-22
