# ChainedClimber2D - Advanced Testing & Debugging System

## Overview

This project implements a comprehensive, production-grade testing and debugging system designed for fast iteration and problem identification. The system follows industry best practices from professional game development studios.

## 🎯 Key Features

### 1. **Multi-Tier Test Architecture**
- **Smoke Tests**: Fast critical system verification (~5 seconds)
- **Unit Tests**: Detailed component and entity testing
- **Integration Tests**: Full system flow testing
- **Parametrized Tests**: Data-driven test scenarios

### 2. **First-Failure Detection**
- Automatically identifies the FIRST test that fails
- Pinpoints the exact problematic change
- Provides stack trace analysis
- Suggests potential causes based on error patterns

### 3. **Detailed Test Reports**
- HTML reports with visual representation
- Text reports with timestamps and performance metrics
- Test execution flow analysis
- Performance profiling per test

### 4. **Runtime Debug Mode**
- Toggle on/off with F3 key
- Frame-by-frame stepping (F4 to toggle, F5 to step)
- Real-time value monitoring
- Collision box visualization
- Velocity vector display
- Performance metrics overlay

### 5. **Smart Analysis**
- Pattern matching for common issues (NullPointerException, bounds errors, etc.)
- Component-specific error analysis
- Relevant stack trace extraction
- Suggested fix locations

## 🚀 Quick Start

### Running Tests

#### Option 1: Run All Tests
```bash
.\run-tests.bat
```

#### Option 2: Run Specific Test Types
```bash
.\run-tests.bat smoke    # Only smoke tests (fast verification)
.\run-tests.bat unit     # Only unit tests
.\run-tests.bat Player   # Run tests matching "Player"
```

#### Option 3: Gradle Commands
```bash
.\gradlew.bat test                          # Run all tests
.\gradlew.bat test --tests "*SmokeTests*"   # Run smoke tests
.\gradlew.bat test --tests "*PlayerTest*"   # Run player tests
```

### Enabling Debug Mode

#### In-Game Controls:
- **F3**: Toggle debug overlay ON/OFF
- **F4**: Toggle step mode (frame-by-frame)
- **F5**: Advance one frame (in step mode)

#### Programmatically:
```java
// In GameScreen or any class
DebugMode.setEnabled(true);
DebugMode.setLevel(DebugMode.DebugLevel.DEBUG);

// Log values
DebugMode.log("Player", "Position", player.getPosition());
DebugMode.log("Physics", "Velocity", velocity, DebugMode.DebugLevel.INFO);

// Visual debugging
DebugMode.drawBounds(shapeRenderer, entity.getBounds());
DebugMode.drawVelocity(shapeRenderer, position, velocity, 0.1f);
```

## 📊 Test Structure

```
core/test/com/chainedclimber/
├── GdxTestRunner.java              # LibGDX headless test runner
├── TestResultAnalyzer.java         # Advanced failure analysis
├── MasterTestSuite.java           # Main test suite orchestrator
├── smoke/
│   └── SmokeTests.java            # Critical system verification
└── entities/
    ├── PlayerTest.java            # Player entity unit tests
    └── MovingPlatformTest.java    # Moving platform tests
```

## 🧪 Test Categories

### Smoke Tests (@Order 1-10)
Fast tests that verify critical systems are operational:
- LibGDX initialization
- Constants loading
- Entity creation (Player, Platform, MovingPlatform)
- Level matrix generation
- Spatial hash system
- Physics constants
- Block type system

**Purpose**: Catch major breaking changes instantly before running full suite.

### Unit Tests (Player)
Comprehensive testing of Player entity:
- **Initialization**: Position, bounds, velocity, grounded state
- **Movement**: Left, right, jump, stop
- **Physics**: Gravity, velocity updates, fall speed clamping
- **Collision**: Landing on platforms, hitting from all sides
- **State Management**: Grounded state, bounce, reset position
- **Edge Cases**: Negative positions, large coordinates, rapid changes
- **Interpolation**: Previous position tracking

### Unit Tests (MovingPlatform)
Testing moving platform behavior:
- Movement and velocity
- Boundary reversal
- Delta tracking for player carry
- Interpolation support

## 📝 Test Reports

### Automated Report Generation
After each test run, detailed reports are generated in `test-reports/`:

```
test-reports/
└── test_report_20251123_143052.txt
```

### Report Contents:
1. **Summary Statistics**: Total, passed, failed, percentage
2. **First Failure Analysis**: Detailed first failure with stack trace
3. **Detailed Results**: All test results with timestamps
4. **Performance Analysis**: Slowest tests ranked

### HTML Reports:
Gradle also generates visual HTML reports:
```
core/build/reports/tests/test/index.html
```

## 🔍 Debugging Features

### Real-Time Monitoring
When debug mode is enabled (F3):
- Player position and velocity
- Grounded state
- FPS and performance metrics
- Collision counts
- Recent debug messages with levels (ERROR, WARNING, INFO, DEBUG, VERBOSE)

### Visual Debugging
- **Green boxes**: Entity bounds
- **Yellow arrows**: Velocity vectors
- **Red circles**: Collision points
- **Overlay text**: Real-time values

### Step Mode
Perfect for analyzing specific frames:
1. Press F4 to enable step mode
2. Game pauses but continues rendering
3. Press F5 to advance one frame
4. Observe state changes frame-by-frame

### Debug Logging Levels
```java
DebugMode.setLevel(DebugMode.DebugLevel.VERBOSE);  // All messages
DebugMode.setLevel(DebugMode.DebugLevel.DEBUG);    // Debug and above
DebugMode.setLevel(DebugMode.DebugLevel.INFO);     // Info and above
DebugMode.setLevel(DebugMode.DebugLevel.WARNING);  // Warnings and errors
DebugMode.setLevel(DebugMode.DebugLevel.ERROR);    // Errors only
```

## 🎓 Best Practices

### Writing New Tests

#### 1. Use Descriptive Names
```java
@Test
@DisplayName("Player should stop at world boundary when moving left")
void testPlayerStopsAtLeftBoundary() {
    // Test implementation
}
```

#### 2. Follow AAA Pattern
```java
@Test
void testPlayerJump() {
    // Arrange
    player.setGrounded(true);
    
    // Act
    player.jump();
    
    // Assert
    assertThat(player.getVelocity().y).isPositive();
}
```

#### 3. Use AssertJ for Fluent Assertions
```java
assertThat(player.getPosition().x)
    .isCloseTo(expectedX, within(EPSILON))
    .isGreaterThan(0);
```

#### 4. Test Edge Cases
```java
@ParameterizedTest
@ValueSource(floats = {-100, 0, 640, 10000})
void testPlayerCreationAtVariousPositions(float x) {
    Player testPlayer = new Player(x, 100);
    assertThat(testPlayer.getPosition().x).isCloseTo(x, within(0.001f));
}
```

### Debugging Workflow

1. **Make a change** to game code
2. **Run smoke tests** first (`.\run-tests.bat smoke`)
3. If smoke tests pass, **run full suite** (`.\run-tests.bat`)
4. On failure, check **first failure** in console output
5. Open **HTML report** for detailed stack trace
6. **Enable debug mode** in-game (F3)
7. **Use step mode** (F4+F5) to observe exact frame of issue
8. **Fix the issue**
9. **Re-run tests** to confirm fix

## 🔧 Configuration

### JUnit Platform Configuration
Edit `build.gradle`:
```gradle
test {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
        showStandardStreams = false  // Set to true for verbose output
    }
}
```

### Test Dependencies
- JUnit Jupiter 5.10.0 (API, Engine, Params)
- Mockito 5.5.0 (Core, JUnit Jupiter)
- AssertJ 3.24.2 (Fluent assertions)
- LibGDX Headless Backend (Test execution)

## 📈 Performance Considerations

### Test Execution Speed
- **Smoke Tests**: < 5 seconds
- **Unit Tests**: < 30 seconds
- **Full Suite**: < 2 minutes

### Optimization Tips
1. Use `@BeforeAll` for expensive setup shared across tests
2. Dispose resources in `@AfterEach` to prevent memory leaks
3. Use headless backend for tests (no GPU needed)
4. Run smoke tests before committing code

## 🐛 Common Issues & Solutions

### Issue: "LibGDX not initialized"
**Solution**: Ensure test class uses `@ExtendWith(GdxTestRunner.class)`

### Issue: "Tests pass locally but fail in CI"
**Solution**: Check for timing dependencies; use fixed timestep

### Issue: "NullPointerException in tests"
**Solution**: Check test order; resources might not be initialized

### Issue: "Debug mode not showing"
**Solution**: Ensure DebugMode.setEnabled(true) is called before rendering

## 📚 Additional Resources

### Test Patterns Used
- **Arrange-Act-Assert (AAA)**: Standard test structure
- **Test Fixtures**: Shared test setup in @BeforeEach
- **Parametrized Tests**: Data-driven testing with @ParameterizedTest
- **Test Suites**: Organized test execution with @Suite
- **Test Watchers**: Advanced failure analysis with TestResultAnalyzer

### Assertion Libraries
- **AssertJ**: Fluent assertions for better readability
- **JUnit Assertions**: Standard assertions
- **Hamcrest**: Matcher-based assertions

## 🎯 Future Enhancements

- [ ] Integration tests for full game flow
- [ ] Performance regression tests
- [ ] Visual regression tests (screenshot comparison)
- [ ] Automated test generation from game sessions
- [ ] Code coverage reporting
- [ ] Mutation testing

## 📞 Support

For issues or questions about the testing system:
1. Check test reports in `test-reports/` directory
2. Enable debug mode and inspect runtime values
3. Review stack traces for problematic code locations
4. Use step mode to isolate exact frame of failure

---

**Remember**: Tests are your safety net. Write them before adding features, run them after changes, and trust them to catch regressions early!
