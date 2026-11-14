package utilities;

import org.assertj.core.api.SoftAssertions;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * Gives every test method invocation (including data provider rows and
 * retries) its own soft assertions, and verifies them once the test method
 * has finished.
 *
 * If soft assertions failed, the test result is marked as FAILURE with all
 * collected errors, so TestNG, Allure and the RetryAnalyzer treat it like a
 * normal assertion failure. If the test had already failed with a hard error,
 * the soft assertion errors are added to that error as suppressed exceptions
 * so nothing is lost.
 */
public class SoftAssertListener implements IInvokedMethodListener {

    /**
     * Starts a clean set of soft assertions before each test method.
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        // Configuration methods (@BeforeMethod, @AfterMethod, ...) are ignored.
        if (method.isTestMethod()) {
            SoftAssert.reset();
        }
    }

    /**
     * Verifies the soft assertions after each test method and fails the test if
     * any of them failed.
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod()) {
            return;
        }
        SoftAssertions softAssertions = SoftAssert.remove();
        if (softAssertions == null || softAssertions.wasSuccess()) {
            return;
        }
        try {
            // Throws one AssertionError that lists every collected failure.
            softAssertions.assertAll();
        } catch (AssertionError softAssertionError) {
            Throwable existing = testResult.getThrowable();
            if (testResult.getStatus() == ITestResult.FAILURE && existing != null) {
                // The test already failed on its own; keep that error as the main one.
                existing.addSuppressed(softAssertionError);
            } else {
                // The test body passed, so the soft assertion failures turn it into a failure.
                testResult.setStatus(ITestResult.FAILURE);
                testResult.setThrowable(softAssertionError);
            }
        }
    }
}
