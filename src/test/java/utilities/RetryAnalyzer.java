package utilities;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import testbase.TestBase;

/**
 * Re-runs a failed test up to the number of times set by
 * {@code retry-logic.max-retries} in configuration.properties. TestNG creates
 * one instance per test method, so the retry count is tracked per method.
 * Attached to all tests by {@link AnnotationTransformer}.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    /** Number of retries already used by this test. */
    private int retryCount = 0;
    private final int maxRetryCount;

    /**
     * Reads the maximum number of retries from the configuration.
     */
    public RetryAnalyzer() {
        Configuration configuration = TestBase.configurationThreadLocal.get();
        if (configuration == null) {
            // TestBase has not created a Configuration on this thread yet, so load one.
            configuration = new Configuration();
        }
        this.maxRetryCount = configuration.retryLogicMaxRetries;
    }

    /**
     * Called by TestNG after a test fails.
     *
     * @param result the result of the failed test
     * @return true to run the test again, false once the retries are used up
     */
    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            return true;
        }
        return false;
    }

}
