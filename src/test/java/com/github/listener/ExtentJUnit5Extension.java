package com.github.listener;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExtentJUnit5Extension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {

    private static final ExtentReports extent = new ExtentReports();
    private static final ConcurrentMap<String, ExtentTest> testMap = new ConcurrentHashMap<>();

    static {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter("extent-report.html");
        extent.attachReporter(sparkReporter);
        System.out.println("ExtentSparkReporter initialized and attached.");
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        String testName = context.getDisplayName();
        String testId = context.getUniqueId();

        ExtentTest test = extent.createTest(testName);
        testMap.put(testId, test);

        System.out.println("Test added to map: " + testId + " -> " + testName);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        String testId = context.getUniqueId();
        ExtentTest test = testMap.get(testId);

        if (test != null) {
            if (context.getExecutionException().isPresent()) {
                test.fail(context.getExecutionException().get());
            } else {
                test.pass("Test passed");
            }
        } else {
            System.err.println("Test instance not found for testId: " + testId);
        }

        // Flush after each test to update the report
        extent.flush();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        // We inject the ExtentTest parameter into test methods that request it
        return parameterContext.getParameter().getType().equals(ExtentTest.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        String testId = extensionContext.getUniqueId();
        ExtentTest test = testMap.get(testId);

        if (test == null) {
            throw new IllegalStateException("No ExtentTest instance found for testId: " + testId);
        }
        return test;
    }

    /**
     * Remove or comment out this method to avoid creating a second test instance
     * from within test classes.
     */
    // public static ExtentTest createTest(String testName) {
    //     String testId = testName; 
    //     ExtentTest test = extent.createTest(testName);
    //     testMap.put(testId, test);
    //     return test;
    // }

    @AfterAll
    public static void closeExtentReports() {
        extent.flush();
        System.out.println("ExtentReports closed.");
    }
}