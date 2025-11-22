# Java Compatibility Issue - Java 25

## The Problem

You currently have **Java 25** installed, which is too new for the current Gradle and LibGDX ecosystem. While I've upgraded the project to use the latest Gradle 8.5, Java 25 has restricted API access that prevents Gradle from starting properly.

## The Solution: Install Java 17 LTS

**Java 17** is the recommended Long-Term Support (LTS) version that works perfectly with LibGDX and Gradle 8.5.

### Step 1: Download Java 17 LTS

**Visit:** https://adoptium.net/

1. Select **Version: 17 - LTS**
2. Select **Operating System: Windows**
3. Select **Architecture: x64**
4. Click **Download .msi**

### Step 2: Install Java 17

1. Run the downloaded `.msi` installer
2. Complete the installation wizard
3. Note the installation path (usually `C:\Program Files\Eclipse Adoptium\jdk-17.x.x\`)

### Step 3: Update JAVA_HOME

**Windows 10/11:**

1. Press `Windows + X` → **System**
2. Click **Advanced system settings**
3. Click **Environment Variables**
4. Under **System variables**, find `JAVA_HOME` and click **Edit**
5. Change it to: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x\`  
   (use your actual JDK 17 installation path)
6. Click **OK** on all dialogs

### Step 4: Verify Java 17

Open a **new** Command Prompt:

```bash
java -version
```

**Expected output:**
```
openjdk version "17.0.x" 2023-xx-xx LTS
```

### Step 5: Clean and Run

```bash
cd c:\Projects\chainedTogether2D

# Stop any running Gradle daemons
.\gradlew.bat --stop

# Run the game
.\gradlew.bat desktop:run
```

## Why Java 17?

- ✅ **LTS (Long Term Support)** - Stable, supported until 2029
- ✅ **Perfect for LibGDX** - Recommended by the LibGDX community
- ✅ **Gradle Compatible** - Fully supported by Gradle 8.x
- ✅ **Android Compatible** - Works with Android development
- ✅ **Widely Used** - Industry standard for game development

## Alternative: Keep Java 25 for Other Projects

You can keep both Java versions installed:

1. Install Java 17 alongside Java 25
2. Set `JAVA_HOME` to Java 17 for game development
3. Use Java 25 for other projects by switching `JAVA_HOME` when needed

## After Installing Java 17

Once you have Java 17 installed and JAVA_HOME updated:

1. **Restart VS Code**
2. Run `.\gradlew.bat desktop:run`
3. **Your game will launch!** 🎮

## Summary

**Current Issue:** Java 25 is too new for LibGDX/Gradle ecosystem  
**Solution:** Install Java 17 LTS  
**Download:** https://adoptium.net/ (select version 17)  
**After:** Update JAVA_HOME → Restart VS Code → Run game!

---

**Need Help?** See [SETUP.md](file:///c:/Projects/chainedTogether2D/docs/SETUP.md) for detailed environment setup instructions.
