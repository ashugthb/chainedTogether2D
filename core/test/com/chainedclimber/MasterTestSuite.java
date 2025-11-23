package com.chainedclimber;

/**
 * Master test suite documentation.
 * 
 * To run all tests: gradle test
 * To run smoke tests: gradle test --tests "*.smoke.*"
 * To run unit tests: gradle test --tests "*.entities.*"
 * 
 * Tests run in order:
 * 1. Smoke Tests (critical system verification)
 * 2. Unit Tests (entity and component tests)
 * 
 * This ensures that basic functionality is verified before running
 * more complex tests, allowing for fast failure detection.
 */
public class MasterTestSuite {
    // This class serves as documentation for the test structure
    // JUnit5 will automatically discover and run all test classes
}
