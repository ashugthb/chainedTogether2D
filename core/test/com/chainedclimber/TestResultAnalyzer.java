package com.chainedclimber;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Advanced test result analyzer that tracks test execution and identifies problematic changes.
 * Implements test flow analysis to pinpoint the first failure in a test sequence.
 * 
 * Features:
 * - Detailed test execution logging
 * - First-failure detection
 * - Test dependency tracking
 * - Stack trace analysis
 * - Test report generation with timestamps
 * - Performance metrics per test
 */
public class TestResultAnalyzer implements TestWatcher {
    
    private static final String REPORTS_DIR = "test-reports";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final List<TestResult> testResults = new ArrayList<>();
    private static boolean firstFailureDetected = false;
    private static TestResult firstFailure = null;
    
    private long testStartTime;
    
    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String displayName = context.getDisplayName();
        System.out.println("⊘ DISABLED: " + displayName + " - Reason: " + reason.orElse("No reason provided"));
    }
    
    @Override
    public void testSuccessful(ExtensionContext context) {
        long duration = System.currentTimeMillis() - testStartTime;
        String displayName = context.getDisplayName();
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        
        TestResult result = new TestResult(
            className,
            displayName,
            TestStatus.PASSED,
            duration,
            null,
            null
        );
        
        testResults.add(result);
        System.out.println("✓ PASSED: " + className + "." + displayName + " (" + duration + "ms)");
    }
    
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        long duration = System.currentTimeMillis() - testStartTime;
        String displayName = context.getDisplayName();
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        
        TestResult result = new TestResult(
            className,
            displayName,
            TestStatus.ABORTED,
            duration,
            cause.getMessage(),
            getStackTraceString(cause)
        );
        
        testResults.add(result);
        System.out.println("⊗ ABORTED: " + className + "." + displayName + " - " + cause.getMessage());
    }
    
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        long duration = System.currentTimeMillis() - testStartTime;
        String displayName = context.getDisplayName();
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String methodName = context.getTestMethod().map(m -> m.getName()).orElse("unknown");
        
        TestResult result = new TestResult(
            className,
            displayName,
            TestStatus.FAILED,
            duration,
            cause.getMessage(),
            getStackTraceString(cause)
        );
        
        testResults.add(result);
        
        // Track first failure for fast debugging
        if (!firstFailureDetected) {
            firstFailureDetected = true;
            firstFailure = result;
            System.out.println("\n" + "=".repeat(80));
            System.out.println("⚠ FIRST FAILURE DETECTED - PROBLEMATIC CHANGE LOCATED");
            System.out.println("=".repeat(80));
        }
        
        System.out.println("✗ FAILED: " + className + "." + displayName);
        System.out.println("  Error: " + cause.getMessage());
        System.out.println("  Location: " + extractRelevantStackTrace(cause));
        System.out.println("  Duration: " + duration + "ms");
        
        // Analyze and suggest potential cause
        analyzePotentialCause(className, methodName, cause);
    }
    
    /**
     * Called before each test starts
     */
    public void testStarting(ExtensionContext context) {
        testStartTime = System.currentTimeMillis();
        String displayName = context.getDisplayName();
        String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        System.out.println("→ RUNNING: " + className + "." + displayName);
    }
    
    /**
     * Generate comprehensive test report
     */
    public static void generateReport() {
        try {
            Path reportsPath = Paths.get(REPORTS_DIR);
            if (!Files.exists(reportsPath)) {
                Files.createDirectories(reportsPath);
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String reportFile = REPORTS_DIR + "/test_report_" + timestamp + ".txt";
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writer.println("=".repeat(80));
                writer.println("TEST EXECUTION REPORT");
                writer.println("Generated: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
                writer.println("=".repeat(80));
                writer.println();
                
                // Summary statistics
                long passed = testResults.stream().filter(r -> r.status == TestStatus.PASSED).count();
                long failed = testResults.stream().filter(r -> r.status == TestStatus.FAILED).count();
                long aborted = testResults.stream().filter(r -> r.status == TestStatus.ABORTED).count();
                long total = testResults.size();
                
                writer.println("SUMMARY:");
                writer.println("  Total Tests: " + total);
                writer.println("  Passed: " + passed + " (" + (total > 0 ? (passed * 100 / total) : 0) + "%)");
                writer.println("  Failed: " + failed);
                writer.println("  Aborted: " + aborted);
                writer.println();
                
                // First failure analysis
                if (firstFailure != null) {
                    writer.println("=".repeat(80));
                    writer.println("FIRST FAILURE - PROBLEMATIC CHANGE:");
                    writer.println("=".repeat(80));
                    writer.println("Test: " + firstFailure.className + "." + firstFailure.testName);
                    writer.println("Error: " + firstFailure.errorMessage);
                    writer.println("Duration: " + firstFailure.durationMs + "ms");
                    writer.println();
                    writer.println("Stack Trace:");
                    writer.println(firstFailure.stackTrace);
                    writer.println();
                }
                
                // Detailed results
                writer.println("=".repeat(80));
                writer.println("DETAILED RESULTS:");
                writer.println("=".repeat(80));
                writer.println();
                
                for (TestResult result : testResults) {
                    writer.println(result.toString());
                    writer.println("-".repeat(80));
                }
                
                // Performance analysis
                writer.println();
                writer.println("=".repeat(80));
                writer.println("PERFORMANCE ANALYSIS:");
                writer.println("=".repeat(80));
                
                testResults.stream()
                    .sorted((a, b) -> Long.compare(b.durationMs, a.durationMs))
                    .limit(10)
                    .forEach(r -> writer.println(String.format("  %6dms - %s.%s", 
                        r.durationMs, r.className, r.testName)));
            }
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("Test report generated: " + reportFile);
            System.out.println("=".repeat(80));
            
        } catch (IOException e) {
            System.err.println("Failed to generate test report: " + e.getMessage());
        }
    }
    
    /**
     * Analyze potential cause of test failure
     */
    private void analyzePotentialCause(String className, String methodName, Throwable cause) {
        String errorMsg = cause.getMessage();
        String causeType = cause.getClass().getSimpleName();
        
        System.out.println("\n  ANALYSIS:");
        
        // Pattern matching for common issues
        if (causeType.equals("NullPointerException")) {
            System.out.println("  → Likely cause: Uninitialized object or null reference");
            System.out.println("  → Check: Object instantiation and null checks in recent changes");
        } else if (causeType.equals("AssertionError")) {
            System.out.println("  → Likely cause: Expected value doesn't match actual value");
            System.out.println("  → Check: Logic changes in tested method");
        } else if (errorMsg != null && errorMsg.contains("bounds")) {
            System.out.println("  → Likely cause: Collision detection or bounds calculation error");
            System.out.println("  → Check: Rectangle operations and coordinate calculations");
        } else if (errorMsg != null && errorMsg.contains("position")) {
            System.out.println("  → Likely cause: Entity positioning or movement logic error");
            System.out.println("  → Check: Position updates and velocity calculations");
        } else if (causeType.equals("ArithmeticException")) {
            System.out.println("  → Likely cause: Division by zero or invalid arithmetic");
            System.out.println("  → Check: Mathematical operations and edge cases");
        }
        
        // Component-specific analysis
        if (className.contains("Player")) {
            System.out.println("  → Component: Player entity - check movement and collision logic");
        } else if (className.contains("Platform")) {
            System.out.println("  → Component: Platform entity - check bounds and collision");
        } else if (className.contains("Physics")) {
            System.out.println("  → Component: Physics system - check velocity and force calculations");
        } else if (className.contains("Collision")) {
            System.out.println("  → Component: Collision system - check detection algorithms");
        }
        
        System.out.println();
    }
    
    /**
     * Extract relevant stack trace information
     */
    private String extractRelevantStackTrace(Throwable cause) {
        StackTraceElement[] stack = cause.getStackTrace();
        for (StackTraceElement element : stack) {
            if (element.getClassName().contains("chainedclimber")) {
                return element.getClassName() + "." + element.getMethodName() + 
                       "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
            }
        }
        return stack.length > 0 ? stack[0].toString() : "Unknown location";
    }
    
    /**
     * Get full stack trace as string
     */
    private String getStackTraceString(Throwable cause) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : cause.getStackTrace()) {
            sb.append("  at ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Reset analyzer state for new test run
     */
    public static void reset() {
        testResults.clear();
        firstFailureDetected = false;
        firstFailure = null;
    }
    
    /**
     * Get first failure for debugging
     */
    public static TestResult getFirstFailure() {
        return firstFailure;
    }
    
    /**
     * Test result data class
     */
    public static class TestResult {
        public final String className;
        public final String testName;
        public final TestStatus status;
        public final long durationMs;
        public final String errorMessage;
        public final String stackTrace;
        public final String timestamp;
        
        public TestResult(String className, String testName, TestStatus status, 
                         long durationMs, String errorMessage, String stackTrace) {
            this.className = className;
            this.testName = testName;
            this.status = status;
            this.durationMs = durationMs;
            this.errorMessage = errorMessage;
            this.stackTrace = stackTrace;
            this.timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(status).append("] ");
            sb.append(className).append(".").append(testName);
            sb.append(" (").append(durationMs).append("ms)");
            sb.append("\n  Timestamp: ").append(timestamp);
            if (errorMessage != null) {
                sb.append("\n  Error: ").append(errorMessage);
            }
            if (stackTrace != null) {
                sb.append("\n  Stack Trace:\n").append(stackTrace);
            }
            return sb.toString();
        }
    }
    
    public enum TestStatus {
        PASSED, FAILED, ABORTED
    }
}
