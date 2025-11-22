# Game Design Document

## ChainedClimber2D - Version 1.0

This document outlines the core game design, mechanics, and vision for **ChainedClimber2D**.

## Game Concept

### Vision

**ChainedClimber2D** is a cooperative 2D climbing platformer where players physically chained together must coordinate their movements to ascend an endless vertical challenge. Inspired by "Chained Together" but built for 2D with Mario-style platforming mechanics.

### Core Gameplay Loop

1. Players start at the bottom of a vertical level
2. Must climb upward using platforms
3. Coordination is essential (in multiplayer mode)
4. Falling is punished but not game-ending
5. Goal: Reach the highest point possible

### Target Audience

- Casual to mid-core gamers
- Fans of cooperative games
- Platformer enthusiasts
- Mobile gamers looking for challenging coordination games

## Version 1 Scope

### Included Features

✅ **Main Menu**
- Simple start screen
- "PLAY" button to enter game

✅ **Basic Platformer Mechanics**
- Horizontal movement (left/right)
- Jump with gravity
- Platform collision detection

✅ **Minimal Graphics**
- Colored rectangles for player
- Colored rectangles for platforms
- Clear visual distinction

✅ **Touch Controls**
- On-screen buttons for mobile
- Keyboard support for desktop testing

### Future Features (Not in V1)

❌ Multiplayer networking
❌ Chain physics between players
❌ Multiple levels
❌ Checkpoints
❌ High-quality sprites/textures
❌ Sound effects and music
❌ Particle effects
❌ Power-ups
❌ Leaderboards

## Game Mechanics

### Player Movement

#### Horizontal Movement

**Controls:**
- **Left**: Move left at constant speed
- **Right**: Move right at constant speed

**Parameters:**
- Movement Speed: 200 pixels/second
- Acceleration: Instant (for responsive feel)
- Deceleration: Instant when button released

**Design Philosophy:**
Mario-style responsive controls where the player immediately moves when pressing a direction and immediately stops when releasing.

#### Jump Mechanics

**Controls:**
- **Jump Button**: Jump if grounded

**Parameters:**
- Jump Force: 600 pixels/second upward velocity
- Gravity: 1200 pixels/second² downward acceleration
- Max Fall Speed: 800 pixels/second

**Jump Curve:**
The jump follows a natural parabolic arc:
- Initial burst of upward velocity
- Constant downward gravity
- Apex when upward velocity reaches 0
- Falling phase with increasing downward velocity
- Capped at max fall speed

**Design Philosophy:**
Jump should feel "floaty" enough to give players control in the air, but responsive enough to feel precise. The values are tuned for a balance between Mario-style platforming (more control) and realistic physics.

#### Collision Detection

**Platform Collision:**
- Player collides with platforms from above
- When player's bottom edge touches platform's top edge
- Player velocity is set to 0 and grounded state is set to true
- Player can jump again when grounded

**Collision Box:**
- Player: Rectangle matching sprite dimensions
- Platform: Rectangle matching platform dimensions
- Simple AABB (Axis-Aligned Bounding Box) collision

**Design Philosophy:**
Pixel-perfect collision isn't necessary for V1. Rectangular collision boxes provide predictable, fair gameplay and are computationally efficient.

### Physics System

**Coordinate System:**
- Origin (0,0) at bottom-left
- X-axis: Left to right
- Y-axis: Bottom to top
- Units: Pixels

**Physics Parameters:**

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Gravity | 1200 px/s² | Feels responsive, not too floaty |
| Jump Velocity | 600 px/s | Allows clearing ~2 character heights |
| Move Speed | 200 px/s | Fast enough to feel responsive |
| Max Fall Speed | 800 px/s | Prevents falling too fast to react |

**Frame-Independent Movement:**
All physics calculations use delta time (time since last frame) to ensure consistent behavior regardless of frame rate.

```java
// Example physics update
velocityY -= gravity * deltaTime;
positionY += velocityY * deltaTime;
```

### Level Design

#### Version 1 Test Level

**Layout:**
- Ground platform at bottom (wide, safe starting area)
- Series of platforms ascending vertically
- Platform spacing: 1.5 to 2 character heights (requires jumping)
- Horizontal offset: Varies to encourage left/right movement

**Platform Types (V1):**
- **Ground Platform**: 800x50 pixels, at Y=50
- **Climbing Platforms**: 200x30 pixels, various positions

**Example Platform Layout:**
```
Y=500  [Platform]
Y=400      [Platform]
Y=300  [Platform]
Y=200          [Platform]
Y=100  [Platform]
Y=50   [==== Ground Platform ====]
```

**Design Philosophy:**
The first level should be simple and forgiving to teach core mechanics. Platforms are large enough to land on comfortably, and spacing allows for mistakes.

### Visual Design (V1)

**Color Palette:**

| Element | Color | Hex Code |
|---------|-------|----------|
| Background | Dark Blue | #1a1a2e |
| Player | Bright Green | #00ff00 |
| Platforms | Gray | #888888 |
| Ground Platform | Dark Gray | #555555 |

**Player:**
- Rectangle: 32x64 pixels
- Color: Bright green (high contrast with background)
- Position: Center of rectangle represents player position

**Platforms:**
- Rectangles with varying widths
- Height: 30 pixels (standard)
- Color: Gray for regular platforms

**Camera:**
- Follows player vertically
- Fixed horizontal center (no horizontal scrolling in V1)
- Viewport: 800x1280 pixels (portrait orientation)

**Design Philosophy:**
Minimal but clear. High contrast ensures readability. Simplicity allows focus on mechanics. All graphics are placeholders designed to be easily replaceable with sprites later.

### Controls

#### Mobile Touch Controls

**Layout:**
```
┌─────────────────────┐
│                     │
│     [JUMP]          │ <- Top-right corner
│                     │
│   Game Viewport     │
│                     │
│                     │
│  [LEFT]   [RIGHT]   │ <- Bottom corners
└─────────────────────┘
```

**Button Specifications:**
- **Size**: 100x100 pixels each
- **Position**: 
  - Left: Bottom-left corner (margin: 20px)
  - Right: Bottom-right corner (margin: 20px)
  - Jump: Top-right corner (margin: 20px)
- **Visual**: Semi-transparent circles with arrows/text
- **Feedback**: Highlight when pressed

**Touch Detection:**
- Simple circle-based touch detection
- Pressed state when finger is within button bounds
- Released state when finger leaves button or is lifted

#### Desktop Controls (Testing)

**Keyboard:**
- **W** or **Space**: Jump
- **A** or **Left Arrow**: Move left
- **D** or **Right Arrow**: Move right

**Design Philosophy:**
Desktop controls mirror mobile controls but use keyboard for faster testing during development.

### User Interface

#### Main Menu

**Elements:**
- Title text: "ChainedClimber2D"
- "PLAY" button (centered)
- Minimal, clean design

**Interaction:**
- Tap/click "PLAY" → Transition to GameScreen

#### In-Game HUD (Minimal)

**Version 1:**
- No HUD elements (focus on core mechanics)

**Future Versions:**
- Height counter
- Player indicators (in multiplayer)
- Timer (optional)

### Game Flow

**State Diagram:**

```
[Launch] → [Main Menu] → [Game Screen] → [Playing]
                ↑              ↓
                └──── [Back] ───┘
```

**Detailed Flow:**

1. **App Launch**
   - LibGDX initializes
   - Assets load (minimal in V1)
   - MainMenuScreen is set as active

2. **Main Menu**
   - Display title and Play button
   - Wait for user input
   - On Play: Transition to GameScreen

3. **Game Screen**
   - Initialize player and platforms
   - Start game loop
   - Accept input and update physics
   - Render game state

4. **Gameplay**
   - Player moves and jumps
   - Camera follows player
   - Collision detection
   - Continue until player quits (back button)

## Future Design Considerations

### Chain Mechanics (Version 2)

**Concept:**
Players connected by a physical chain that affects movement.

**Design Questions:**
- Chain length: How far apart can players be?
- Chain physics: Elastic or rigid?
- Breaking: Can the chain break? Consequences?
- Coordination: How does one player's jump affect another?

### Multiplayer (Version 3)

**Design Questions:**
- Player count: 2-4 players optimal?
- Camera: How to keep all players visible?
- Synchronization: How to handle latency?
- Respawning: What happens when one player falls?

### Advanced Levels (Version 4+)

**Concepts:**
- Moving platforms
- Hazards (spikes, falling rocks)
- Crumbling platforms
- Wind zones (push player)
- Checkpoints
- Multiple themed worlds

## Balancing Philosophy

**Core Principles:**
1. **Responsive Controls**: Player input should feel immediate
2. **Fair Challenges**: Difficulty from coordination, not unfair mechanics
3. **Forgiving Physics**: Allow some margin for error
4. **Clear Feedback**: Player should understand why they succeeded/failed
5. **Incremental Difficulty**: Ease players into challenges

**Tuning Approach:**
- Playtest frequently
- Iterate on physics values based on feel
- Adjust platform spacing based on player success rates
- Balance between challenge and frustration

## Success Metrics

**Version 1 Goals:**
- ✅ Smooth, responsive controls
- ✅ Clear visual feedback
- ✅ Stable frame rate (60 FPS target)
- ✅ No critical bugs
- ✅ Enjoyable single-player platforming

**Future Metrics:**
- Player retention (multiplayer)
- Average height reached
- Completion rate
- User satisfaction scores

## Technical Constraints

**Performance Targets:**
- **Frame Rate**: 60 FPS on mid-range Android devices
- **Memory**: < 100 MB RAM usage
- **Battery**: Minimal drain (efficient rendering)
- **Storage**: < 50 MB app size

**Platform Requirements:**
- **Min Android**: API 21 (Android 5.0, 2014)
- **Target Android**: API 33 (Android 13, 2022)
- **Screen Sizes**: Support 4" to 7" screens
- **Orientation**: Portrait mode only

---

## Appendix: Physics Calculations

### Jump Arc Calculation

Given:
- Jump velocity (V₀) = 600 px/s
- Gravity (g) = 1200 px/s²

**Time to reach apex:**
```
t = V₀ / g = 600 / 1200 = 0.5 seconds
```

**Maximum jump height:**
```
h = (V₀²) / (2g) = (600²) / (2 * 1200) = 150 pixels
```

**Total jump duration (up + down):**
```
t_total = 2 * t = 1.0 second
```

This means the player can clear platforms ~150 pixels above them, with a total jump taking 1 second.

### Character Height Reference

If character is 64 pixels tall:
- Jump height: 150 pixels ≈ 2.3 character heights
- Feels natural for platforming

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-22  
**Status:** Living document - will evolve with the game
