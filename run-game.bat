@echo off
echo ============================================
echo Running ChainedClimber2D Game
echo ============================================
echo.

echo Current Java Version:
java -version
echo.

echo Starting the game...
echo (This may take a minute on first run)
echo.

gradlew.bat desktop:run

echo.
echo ============================================
echo If the game didn't start, check the error above
echo ============================================
pause
