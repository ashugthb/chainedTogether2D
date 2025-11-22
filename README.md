# ChainedClimber2D

A 2D Android climbing platformer game where multiple chained players must coordinate to climb upward together. Inspired by "Chained Together" but in 2D with Mario-style platforming mechanics.

## Overview

**ChainedClimber2D** is a cooperative climbing game where coordination between players is essential to progress. This first version establishes the core platforming mechanics and UI foundation, with multiplayer chain mechanics planned for future releases.

## Current Version: v0.1.0 (Alpha)

This is the **first version** focusing on:
- ✅ Main menu with Play button
- ✅ Basic 2D platformer level
- ✅ Single character with jump, left, and right movement
- ✅ Simple physics (gravity, collision detection)
- ✅ Minimal graphics (colored shapes - ready for texture upgrades later)

## Features (Version 1)

### Main Menu
- Clean start screen
- "PLAY" button to start the game
- Simple, intuitive navigation

### Gameplay
- **Player Movement**: Left and right directional controls
- **Jump Mechanics**: Natural jumping with gravity
- **Platforms**: Vertical climbing challenge with multiple platforms
- **Physics**: Realistic gravity and collision detection
- **Touch Controls**: On-screen controls optimized for mobile

### Graphics Style
- Minimalist design using colored rectangles and shapes
- Clear visual distinction between player and platforms
- Performance-optimized for smooth gameplay
- **Ready for future upgrades** with high-quality sprites and textures

## Technology Stack

- **Framework**: LibGDX 1.12.1 (Java-based game development framework)
- **Language**: Java
- **Platform**: Android (with desktop testing support)
- **Build Tool**: Gradle
- **Minimum Android**: API 21 (Android 5.0 Lollipop)
- **Target Android**: API 33 (Android 13)

## Quick Start

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- Android SDK (API 21+)
- Android Studio (recommended) or any Java IDE
- Gradle 7.4+ (included via wrapper)

### Running on Desktop (for testing)
```bash
cd c:\Projects\chainedTogether2D
gradlew desktop:run
```

### Building for Android
```bash
cd c:\Projects\chainedTogether2D
gradlew android:assembleDebug
```

The APK will be generated at: `android/build/outputs/apk/debug/android-debug.apk`

### Installing on Android Device
```bash
# Connect your Android device via USB or start an emulator
gradlew android:installDebug
```

For detailed setup instructions, see [SETUP.md](docs/SETUP.md).

## Project Structure

```
ChainedClimber2D/
├── android/              # Android-specific code and resources
├── core/                 # Platform-independent game logic
│   └── src/
│       ├── screens/      # Game screens (Menu, Game)
│       ├── entities/     # Game entities (Player, Platform)
│       ├── systems/      # Game systems (Input, Physics)
│       └── utils/        # Utilities and constants
├── desktop/              # Desktop launcher (for testing)
├── docs/                 # Documentation
│   ├── SETUP.md
│   ├── ARCHITECTURE.md
│   └── GAME_DESIGN.md
├── build.gradle          # Root build configuration
└── settings.gradle       # Project settings
```

## Documentation

- **[SETUP.md](docs/SETUP.md)** - Complete development environment setup guide
- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Technical architecture and design
- **[GAME_DESIGN.md](docs/GAME_DESIGN.md)** - Game mechanics and design decisions

## Controls

### Desktop (Testing)
- **A** or **Left Arrow** - Move left
- **D** or **Right Arrow** - Move right
- **W** or **Space** - Jump

### Android (Touch)
- **Left Button** - Move left
- **Right Button** - Move right
- **Jump Button** - Jump

## Future Roadmap

### Version 2: Chain Mechanics
- Implement chain physics between players
- Add coordination challenges requiring synchronized movement
- Chain tension and breaking mechanics

### Version 3: Multiplayer
- Online multiplayer support
- Player synchronization
- Room/lobby system
- Real-time player coordination

### Version 4: Enhanced Graphics
- High-quality character sprites
- Animated player movements
- Detailed platform textures
- Particle effects
- Background parallax scrolling

### Version 5: Advanced Features
- Multiple levels with increasing difficulty
- Checkpoints and respawn system
- Power-ups and collectibles
- Leaderboards and achievements

## Contributing

This is currently a solo development project. Feedback and suggestions are welcome!

## License

© 2025 ChainedClimber2D. All rights reserved.

## Contact

For questions or feedback, please create an issue in the project repository.

---

**Made with LibGDX** 🎮
