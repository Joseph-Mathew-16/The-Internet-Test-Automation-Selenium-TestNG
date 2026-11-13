package utilities;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import testbase.TestBase;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private final int maxRetryCount;

    public RetryAnalyzer() {
        Configuration configuration = TestBase.configurationThreadLocal.get();
        if (configuration == null) {
            configuration = new Configuration(); // loads configuration.properties
        }
        this.maxRetryCount = configuration.retryLogicMaxRetries;
    }

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            return true;
        }
        return false;
    }

}