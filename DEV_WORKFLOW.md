# Development Workflow - Live Reload Setup

## 🚀 Quick Start - Live Development Mode

### Option 1: PowerShell (Recommended)
```powershell
# Terminal 1 - Start auto-compiler (watches for changes)
.\watch.ps1

# Terminal 2 - Run the game
.\gradlew desktop:run
```

### Option 2: Batch Script
```cmd
# Double-click or run:
watch-and-build.bat

# Then in another terminal:
gradlew desktop:run
```

### Option 3: Single Command (Continuous Build)
```cmd
# Watches and rebuilds automatically
gradlew -t classes --console=plain
```

---

## 📝 How It Works

1. **Start the file watcher** in one terminal
   - It monitors all `.java` files
   - Auto-compiles when you save
   - Shows build status

2. **Run the game** in another terminal
   - Use `gradlew desktop:run`
   - Game runs normally

3. **Edit and save** your code
   - Changes auto-compile in Terminal 1
   - **Restart the game** (Ctrl+C then run again) to see changes

---

## ⚡ Development Tips

### Fast Iteration Workflow
```
1. Edit code in your IDE
2. Save (Ctrl+S)
3. Watch Terminal 1 - shows "BUILD SUCCESSFUL"
4. Restart game in Terminal 2
5. Test your changes
```

### Incremental Builds
- Only changed files recompile
- Much faster than full rebuild
- Typically takes 1-3 seconds

### If Build Fails
- Check Terminal 1 for errors
- Fix the syntax error
- Save again - auto-compiles

---

## 🛠️ Available Scripts

| Script | Purpose |
|--------|---------|
| `watch.ps1` | PowerShell file watcher (best for Windows) |
| `watch-and-build.bat` | Batch file watcher |
| `run-dev.bat` | Quick dev mode launcher |
| `dev-live.bat` | Alternative continuous build |

---

## 🎯 Recommended Setup in VS Code

### Terminal 1 (Watch Mode)
```powershell
.\watch.ps1
```

### Terminal 2 (Run Game)
```powershell
.\gradlew desktop:run
```

**Workflow:**
1. Edit code → Save
2. See "BUILD SUCCESSFUL" in Terminal 1
3. Ctrl+C in Terminal 2, then `↑` (up arrow) + Enter to restart
4. Test changes immediately

---

## 📌 Notes

- **No need to run full build** - continuous mode handles it
- **Faster than manual builds** - only rebuilds changed files
- **Keep watcher running** - leave Terminal 1 open while coding
- **Restart game manually** - auto-restart not supported yet (LibGDX limitation)

---

## 🔧 Advanced: Gradle Continuous Build

For power users, use Gradle's built-in continuous build:

```bash
# Auto-rebuild everything
gradlew -t build

# Auto-rebuild just core module
gradlew -t core:classes

# Auto-rebuild with quiet output
gradlew -t classes --quiet
```

---

## 🐛 Troubleshooting

### Watcher not detecting changes?
- Make sure you **saved the file** (Ctrl+S)
- Check Terminal 1 for errors

### Game doesn't show changes?
- You must **restart the game** after recompile
- LibGDX doesn't support hot-reload during runtime

### Build errors?
- Fix syntax errors in your code
- Save again - watcher will retry automatically

---

**Happy Coding! 🎮**
