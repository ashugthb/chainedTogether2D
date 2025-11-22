@echo off
REM ChainedClimber2D - Quick Development Runner
REM This script watches for file changes and auto-recompiles

cd /d "%~dp0"

echo.
echo ================================================
echo   ChainedClimber2D - LIVE DEVELOPMENT MODE
echo ================================================
echo.
echo This will:
echo  1. Watch your .java files for changes
echo  2. Auto-compile when you save
echo  3. Restart the game automatically
echo.
echo Just edit and save - changes apply instantly!
echo.
echo Press Ctrl+C to stop
echo ================================================
echo.

REM Use continuous build mode - watches files and rebuilds
gradlew.bat desktop:run -t --continuous

pause
