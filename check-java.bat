@echo off
echo ============================================
echo Checking Java Installation
echo ============================================
echo.

echo Current JAVA_HOME:
echo %JAVA_HOME%
echo.

echo Expected JAVA_HOME:
echo C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\
echo.

echo Testing Java version:
java -version
echo.

echo ============================================
echo If JAVA_HOME is incorrect, follow these steps:
echo 1. Close this terminal completely
echo 2. Close VS Code completely
echo 3. Reopen VS Code
echo 4. Open a new terminal
echo 5. Run this script again
echo ============================================
pause
