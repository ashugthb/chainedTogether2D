@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo.
echo ========================================
echo   ChainedClimber2D - LIVE RELOAD
echo ========================================
echo.
echo This watches your code and auto-rebuilds!
echo Just save any .java file and it recompiles.
echo.
echo Starting...
echo.

REM Start continuous compilation in background
start /MIN "Auto-Compile" cmd /c "gradlew.bat -t classes --quiet"

echo Waiting for initial build...
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo BUILD WATCHER IS RUNNING
echo ========================================
echo.
echo Now run the game with:
echo   gradlew desktop:run
echo.
echo Or in a new terminal:
echo   .\gradlew desktop:run
echo.
echo Every time you save a .java file:
echo   - Auto-compiles in background
echo   - Restart the game to see changes
echo.
echo Press any key to STOP the auto-compiler
echo ========================================
echo.

pause >nul

echo Stopping auto-compiler...
taskkill /FI "WindowTitle eq Auto-Compile*" /T /F >nul 2>&1

echo.
echo Auto-compiler stopped.
pause
