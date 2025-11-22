# ChainedClimber2D - Quick Start Guide

## 🚀 Get Started in 3 Steps

### 1️⃣ Install Java JDK

**Download**: https://adoptium.net/
- Choose JDK 11 or higher
- Run installer
- **Set JAVA_HOME**:
  - Windows: System Properties → Environment Variables
  - Add: `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.x.x`
  - Add to PATH: `%JAVA_HOME%\bin`

### 2️⃣ Test the Game (Desktop)

```bash
cd c:\Projects\chainedTogether2D
.\gradlew.bat desktop:run
```

**Controls**:
- **A/D** or **Arrows** - Move left/right
- **W/Space** - Jump

### 3️⃣ Build for Android (Optional)

**Install Android Studio**: https://developer.android.com/studio

**Build APK**:
```bash
.\gradlew.bat android:assembleDebug
```

**Location**: `android\build\outputs\apk\debug\android-debug.apk`

## 📖 Full Documentation

- **[README.md](README.md)** - Project overview
- **[docs/SETUP.md](docs/SETUP.md)** - Detailed setup guide  
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Technical architecture
- **[docs/GAME_DESIGN.md](docs/GAME_DESIGN.md)** - Game mechanics

## 🎮 What's Included

✅ Main menu with Play button  
✅ 2D platformer with 8 platforms  
✅ Player movement (left, right, jump)  
✅ Physics (gravity, collision)  
✅ Touch controls for Android  
✅ Keyboard controls for desktop  
✅ Minimal graphics (ready to upgrade)  

## 🔧 Common Issues

**"JAVA_HOME not set"**  
→ Install JDK and set environment variable

**Build fails**  
→ Run `.\gradlew.bat clean`

**Need Android SDK**  
→ Create `local.properties`:
```
sdk.dir=C:\\Users\\<YourName>\\AppData\\Local\\Android\\Sdk
```

## 🚀 Next Steps

1. Run the game on desktop
2. Customize physics in `Constants.java`
3. Add more platforms in `GameScreen.java`
4. Plan multiplayer chain mechanics
5. Upgrade graphics with sprites

**Happy coding!** 🎮
