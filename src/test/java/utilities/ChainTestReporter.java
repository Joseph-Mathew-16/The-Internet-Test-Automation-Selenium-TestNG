package utilities;

import com.aventstack.chaintest.plugins.ChainTestListener;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.Reporter;
import testbase.TestBase;

public class ChainTestReporter extends ChainTestListener {

    /**
     * This method will log a message to the TestNG report and optionally to
     * standard output/console. It can also attach a screenshot to the
     * report if specified.
     *
     * @param logMessage       - The message to be logged
     * @param logLevel         - The level of the log message
     * @param verboseLevel     - The verbose level of the test suite
     * @param logToStandardOut - Whether to log the message to standard
     *                         output/console (YES or NO)
     * @param testBase         - The instance of the TestBase class to access the
     *                         WebDriver instance for taking screenshots
     * @param attachScreenshot - Whether to attach a screenshot to the log (YES or
     *                         NO)
     */
    public void reportLog(String logMessage, int logLevel, int verboseLevel, LogToStandardOut logToStandardOut,
                          TestBase testBase, AttachScreenshot attachScreenshot) {
        boolean verboseLevelFlag = logLevel <= verboseLevel;
        boolean logToStandardOutFlag = logToStandardOut == LogToStandardOut.YES;
        boolean attachScreenshotFlag = attachScreenshot == AttachScreenshot.YES;

        if (verboseLevelFlag) {
            Reporter.log(logMessage, logToStandardOutFlag);
            ChainTestListener.log(logMessage);
            if (attachScreenshotFlag) {
                try {
                    ChainTestListener.embed(takeScreenshot(testBase), "image/png");
                } catch (WebDriverException webDriverException) {
                    String errorMessage = "Failure when taking screenshot.";
                    reportLog(logMessage, 3, verboseLevel, LogToStandardOut.YES, testBase, AttachScreenshot.NO);
                    Assert.fail(errorMessage, webDriverException);
                } catch (Exception e) {
                    String errorMessage = "Error when taking screenshot.";
                    reportLog(logMessage, 3, verboseLevel, LogToStandardOut.YES, testBase, AttachScreenshot.NO);
                    Assert.fail(errorMessage, e);
                }
            }
        }

    }

    /**
     * This method will take a screenshot using the WebDriver instance from the
     * TestBase class and return it as a byte array.
     *
     * @param testBase - The instance of the TestBase class to access the WebDriver
     *                 instance
     * @return - A byte array representing the screenshot taken
     * @throws WebDriverException - If there is an error while taking the screenshot
     */
    public byte[] takeScreenshot(TestBase testBase) throws WebDriverException {
        TakesScreenshot screenshot = testBase.driver;
        return screenshot.getScreenshotAs(OutputType.BYTES);
    }

    public enum LogToStandardOut {
        YES, NO
    }

    public enum AttachScreenshot {
        YES, NO
    }

}
