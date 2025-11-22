@echo off
echo ========================================
echo Running MinimalLauncher with verbose logging
echo ========================================
echo.

cd /d "%~dp0"

echo Compiling MinimalLauncher.java...
javac -d desktop\build\classes\java\main desktop\src\com\chainedclimber\MinimalLauncher.java

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo Running MinimalLauncher...
echo ========================================
echo.

java -cp desktop\build\classes\java\main com.chainedclimber.MinimalLauncher

echo.
echo ========================================
echo MinimalLauncher completed.
echo ========================================
pause
