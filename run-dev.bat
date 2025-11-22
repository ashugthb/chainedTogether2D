@echo off
echo ========================================
echo Starting ChainedClimber2D in DEV MODE
echo ========================================
echo.
echo Features:
echo - Automatic recompilation on code changes
echo - Hot reload when you save files
echo - Just save your .java files and changes apply!
echo.
echo Press Ctrl+C in BOTH windows to stop
echo ========================================
echo.

cd /d "%~dp0"

echo Starting continuous build in a new window...
start "Continuous Build - Auto Compile" cmd /k "gradlew.bat -t classes --console=plain"

timeout /t 3 /nobreak >nul

echo Starting game application...
echo.
echo TIP: Keep this window and the build window open
echo When you save code changes, they'll auto-compile and restart!
echo.

gradlew.bat desktop:run

echo.
echo ========================================
echo Dev mode stopped
echo ========================================
pause
