# Development Environment Setup Guide

This guide will walk you through setting up your development environment to build, run, and develop **ChainedClimber2D**.

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 11 or Higher**
   - Download from: https://adoptium.net/ (Recommended: Eclipse Temurin)
   - Verify installation:
     ```bash
     java -version
     ```
   - Should show version 11 or higher

2. **Android SDK**
   - Option A: Install via Android Studio (recommended)
   - Option B: Install Command Line Tools only
   - Minimum API Level: 21 (Android 5.0)
   - Recommended API Level: 33 (Android 13)

3. **Android Studio** (Recommended)
   - Download from: https://developer.android.com/studio
   - Includes Android SDK, emulator, and excellent IDE support
   - Version: Arctic Fox or newer

4. **Gradle** (Included via wrapper)
   - The project includes Gradle Wrapper
   - No manual installation needed
   - Version: 7.4+

### Optional Software

- **Git** - For version control
- **IntelliJ IDEA** - Alternative IDE (also supports Android)
- **VS Code** - Lightweight editor with Java extensions

## Step-by-Step Setup

### 1. Install Java JDK

**Windows:**
```bash
# Download installer from https://adoptium.net/
# Run the installer and follow the wizard
# Add JAVA_HOME to environment variables
```

**Verify Installation:**
```bash
java -version
javac -version
```

### 2. Install Android Studio

1. Download Android Studio from https://developer.android.com/studio
2. Run the installer
3. During first launch, complete the Setup Wizard:
   - Install Android SDK
   - Install Android SDK Platform (API 33 recommended)
   - Install Android SDK Build-Tools
   - Install Android Emulator

### 3. Configure Android SDK

**Set ANDROID_HOME Environment Variable:**

**Windows:**
```bash
# Add to System Environment Variables:
ANDROID_HOME=C:\Users\<YourUsername>\AppData\Local\Android\Sdk
```

**Add to PATH:**
```bash
# Add these to your PATH:
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\tools
%ANDROID_HOME%\tools\bin
```

**Verify:**
```bash
adb version
```

### 4. Clone/Open the Project

```bash
cd c:\Projects
# If using Git:
# git clone <repository-url> chainedTogether2D

# Open in Android Studio:
# File -> Open -> Select c:\Projects\chainedTogether2D
```

### 5. Sync Gradle

Android Studio should automatically detect the Gradle project and prompt you to sync.

**Manual Sync:**
- Click "File" → "Sync Project with Gradle Files"
- Or click the "Sync" icon in the toolbar

**Command Line:**
```bash
cd c:\Projects\chainedTogether2D
gradlew tasks
```

This will download all dependencies (may take a few minutes on first run).

## Building the Project

### Desktop Build (Fastest - For Testing)

**Command Line:**
```bash
cd c:\Projects\chainedTogether2D
gradlew desktop:run
```

**Android Studio:**
1. Open "Run" menu
2. Select "Edit Configurations..."
3. Click "+" → "Application"
4. Set:
   - Name: Desktop
   - Module: desktop
   - Main class: `com.chainedclimber.DesktopLauncher`
5. Click "OK" and run

### Android Build

**Debug APK:**
```bash
gradlew android:assembleDebug
```

Output: `android/build/outputs/apk/debug/android-debug.apk`

**Release APK:**
```bash
gradlew android:assembleRelease
```

Output: `android/build/outputs/apk/release/android-release-unsigned.apk`

## Running the Game

### On Desktop

```bash
gradlew desktop:run
```

Controls:
- **A** / **Left Arrow**: Move left
- **D** / **Right Arrow**: Move right
- **W** / **Space**: Jump

### On Android Emulator

1. **Create an Emulator (First Time):**
   - Open Android Studio
   - Tools → AVD Manager
   - Click "Create Virtual Device"
   - Select a device (e.g., Pixel 5)
   - Select system image (API 33 recommended)
   - Finish

2. **Start the Emulator:**
   - Click the "Play" button next to your emulator in AVD Manager

3. **Install the Game:**
   ```bash
   gradlew android:installDebug
   ```

4. **Launch the App:**
   - Find "ChainedClimber2D" in the app drawer
   - Tap to launch

### On Physical Android Device

1. **Enable Developer Options:**
   - Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"

2. **Connect Device:**
   - Connect via USB cable
   - Accept the debugging prompt on your device

3. **Verify Connection:**
   ```bash
   adb devices
   ```

4. **Install the Game:**
   ```bash
   gradlew android:installDebug
   ```

## Project Structure in IDE

```
ChainedClimber2D/
├── android/              # Android module
│   ├── src/             # Android-specific code
│   ├── res/             # Android resources
│   ├── AndroidManifest.xml
│   └── build.gradle
│
├── core/                # Core game module (platform-independent)
│   ├── src/
│   │   └── com/chainedclimber/
│   │       ├── ChainedClimberGame.java       # Main game class
│   │       ├── screens/                       # Game screens
│   │       │   ├── MainMenuScreen.java
│   │       │   └── GameScreen.java
│   │       ├── entities/                      # Game entities
│   │       │   ├── Player.java
│   │       │   └── Platform.java
│   │       ├── systems/                       # Game systems
│   │       │   └── InputController.java
│   │       └── utils/                         # Utilities
│   │           └── Constants.java
│   └── build.gradle
│
├── desktop/             # Desktop module (for testing)
│   ├── src/
│   │   └── com/chainedclimber/
│   │       └── DesktopLauncher.java
│   └── build.gradle
│
├── docs/                # Documentation
├── gradle/              # Gradle wrapper
├── build.gradle         # Root build file
└── settings.gradle      # Project settings
```

## Common Issues and Solutions

### Issue: "ANDROID_HOME not set"

**Solution:**
```bash
# Windows - add to environment variables:
ANDROID_HOME=C:\Users\<YourUsername>\AppData\Local\Android\Sdk
```

### Issue: "SDK location not found"

**Solution:**
Create `local.properties` in project root:
```properties
sdk.dir=C:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
```

### Issue: Gradle sync fails

**Solution:**
```bash
# Clear Gradle cache
gradlew clean

# Or delete .gradle folder and re-sync
```

### Issue: "Unable to find adb"

**Solution:**
Add Android SDK platform-tools to PATH:
```bash
# Windows:
%ANDROID_HOME%\platform-tools
```

### Issue: Desktop app won't start - "LWJGL not found"

**Solution:**
The desktop module requires native libraries. Gradle should handle this automatically, but if issues persist:
```bash
gradlew desktop:clean desktop:run
```

### Issue: Android emulator is slow

**Solutions:**
- Install Intel HAXM (for Intel CPUs)
- Enable hardware acceleration in BIOS
- Use x86 system image instead of ARM
- Allocate more RAM to emulator (AVD Manager → Edit → Advanced)

## Development Workflow

### Recommended Workflow

1. **Develop on Desktop** (fastest iteration):
   ```bash
   gradlew desktop:run
   ```

2. **Test on Android Emulator** (periodically):
   ```bash
   gradlew android:installDebug
   ```

3. **Test on Real Device** (before release):
   ```bash
   gradlew android:installDebug
   ```

### Hot Reload

LibGDX doesn't support hot reload by default. You'll need to:
- Stop the app
- Make changes
- Rebuild and run

For faster iteration, use desktop mode during development.

### Debugging

**Desktop:**
- Use standard Java debugging in your IDE
- Set breakpoints in core module code

**Android:**
- Use Android Studio's debugger
- View logs: `adb logcat | findstr ChainedClimber`

## Next Steps

Once your environment is set up:

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand the code structure
2. Read [GAME_DESIGN.md](GAME_DESIGN.md) to understand game mechanics
3. Run the game on desktop to verify everything works
4. Start exploring and modifying the code!

## Useful Commands Reference

```bash
# Build and run desktop version
gradlew desktop:run

# Build debug APK
gradlew android:assembleDebug

# Install on connected device/emulator
gradlew android:installDebug

# Clean build
gradlew clean

# View all available tasks
gradlew tasks

# Check connected devices
adb devices

# View Android logs
adb logcat

# Uninstall from device
adb uninstall com.chainedclimber
```

## Resources

- **LibGDX Documentation**: https://libgdx.com/wiki/
- **LibGDX Tutorials**: https://libgdx.com/wiki/start/tutorials
- **Android Developer Guide**: https://developer.android.com/
- **Gradle User Guide**: https://docs.gradle.org/

---

Happy coding! If you encounter any issues not covered here, check the LibGDX community forums or create an issue in the project repository.
