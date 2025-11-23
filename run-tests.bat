@echo off
REM Advanced Test Runner for ChainedClimber2D
REM Runs tests with detailed reporting and failure analysis

echo ================================================================================
echo ChainedClimber2D - Advanced Test Suite Runner
echo ================================================================================
echo.

REM Check for optional test type parameter
set TEST_TYPE=%1
if "%TEST_TYPE%"=="" set TEST_TYPE=all

echo Test Type: %TEST_TYPE%
echo.

REM Clean previous test results
echo Cleaning previous test results...
if exist core\build\test-results rmdir /s /q core\build\test-results
if exist core\build\reports\tests rmdir /s /q core\build\reports\tests
if exist test-reports rmdir /s /q test-reports
mkdir test-reports

echo.
echo ================================================================================
echo Running Tests...
echo ================================================================================
echo.

REM Run appropriate test suite based on type
if "%TEST_TYPE%"=="smoke" (
    echo Running Smoke Tests only...
    call gradlew.bat :core:test --tests "com.chainedclimber.smoke.*" --info
) else if "%TEST_TYPE%"=="unit" (
    echo Running Unit Tests only...
    call gradlew.bat :core:test --tests "com.chainedclimber.entities.*" --info
) else if "%TEST_TYPE%"=="all" (
    echo Running Full Test Suite...
    call gradlew.bat :core:test --info
) else (
    echo Running specific test: %TEST_TYPE%
    call gradlew.bat :core:test --tests "*%TEST_TYPE%*" --info
)

set TEST_EXIT_CODE=%ERRORLEVEL%

echo.
echo ================================================================================
echo Test Execution Complete
echo ================================================================================
echo Exit Code: %TEST_EXIT_CODE%
echo.

REM Open test report if tests ran
if exist core\build\reports\tests\test\index.html (
    echo Opening test report...
    start core\build\reports\tests\test\index.html
)

REM Check for test-reports directory
if exist test-reports (
    echo.
    echo Test reports generated in: test-reports\
    for %%f in (test-reports\*.txt) do (
        echo   - %%f
    )
)

echo.
if %TEST_EXIT_CODE% EQU 0 (
    echo [SUCCESS] All tests passed!
    echo ================================================================================
) else (
    echo [FAILURE] Some tests failed!
    echo ================================================================================
    echo.
    echo Check the test report for details on failures.
    echo First failure indicates the problematic change.
)

echo.
pause
exit /b %TEST_EXIT_CODE%
