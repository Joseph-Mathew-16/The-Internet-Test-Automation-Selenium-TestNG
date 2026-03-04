package testbase;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import utilities.ChainTestReporter.AttachScreenshot;
import utilities.ChainTestReporter.LogToStandardOut;
import utilities.Configuration;
import utilities.TimeUtilities;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBase {

    /**
     * ThreadLocal variables to store RemoteWebDriver and Configuration instances
     * for each thread
     */
    public static ThreadLocal<RemoteWebDriver> driverThreadLocal = new ThreadLocal<>();
    public static ThreadLocal<Configuration> configurationThreadLocal = new ThreadLocal<>();
    /**
     * Instance variables for RemoteWebDriver and Configuration
     */
    public RemoteWebDriver driver;
    public Configuration configuration;

    /**
     * Constructor for the TestBase class. It initializes the Configuration and
     * RemoteWebDriver instances for the current thread.
     */
    public TestBase() {
        if (getConfiguration() == null) {
            configuration = new Configuration(this);
            configurationThreadLocal.set(configuration);
            reportLog("Initialized new Configuration object.", 6, LogToStandardOut.YES, AttachScreenshot.NO);
        } else {
            configuration = getConfiguration();
            reportLog("Reusing existing Configuration object.", 6, LogToStandardOut.YES, AttachScreenshot.NO);
        }
        driver = getDriver();
        reportLog("Reusing existing RemoteWebDriver object.", 6, LogToStandardOut.YES, AttachScreenshot.NO);
    }

    /**
     * Constructor for the TestBase class. It initializes the Configuration and
     * RemoteWebDriver instances for the current thread. It sets a provided Duration as the explicit wait duration.
     */
    public TestBase(Duration explicitWaitDuration) {
            configuration = new Configuration(this);
            configuration.timeoutsDurationsExplicitWait = explicitWaitDuration;
            reportLog("Initialized new Configuration object. Explicit wait duration set as " + explicitWaitDuration.toString() + ".", 6, LogToStandardOut.YES, AttachScreenshot.NO);

        driver = getDriver();
        reportLog("Reusing existing RemoteWebDriver object.", 6, LogToStandardOut.YES, AttachScreenshot.NO);
    }

    /**
     * This method will return the instance of the Configuration for the current
     * thread.
     *
     * @return Configuration - The instance of the Configuration for the current
     * thread
     */
    public Configuration getConfiguration() {
        return configurationThreadLocal.get();
    }

    /**
     * This method will launch the browser based on the parameter passed
     *
     * @param browser - The name of the browser to be launched (e.g., "chrome",
     *                "firefox", "edge")
     * @return RemoteWebDriver - The instance of the launched browser driver
     */
    public RemoteWebDriver launchBrowser(String browser) {
        try {
            switch (browser) {
                case "chrome":
                    driver = new ChromeDriver();
                    driverThreadLocal.set(driver);
                    reportLog("Launched Chrome browser successfully.", 4, LogToStandardOut.YES, AttachScreenshot.NO);
                    break;
                case "firefox":
                    driver = new FirefoxDriver();
                    driverThreadLocal.set(driver);
                    reportLog("Launched Firefox browser successfully.", 4, LogToStandardOut.YES, AttachScreenshot.NO);
                    break;
                case "edge":
                    driver = new EdgeDriver();
                    driverThreadLocal.set(driver);
                    reportLog("Launched Edge browser successfully.", 4, LogToStandardOut.YES, AttachScreenshot.NO);
                    break;
                case "safari":
                    driver = new SafariDriver();
                    driverThreadLocal.set(driver);
                    reportLog("Launched Safari browser successfully.", 4, LogToStandardOut.YES, AttachScreenshot.NO);
                    break;
                default:
                    String errorMessage = "The specified browser is not supported: " + browser + ". Supported browsers are: Chrome, Firefox, Edge, Safari.";
                    reportLog(errorMessage, 3, LogToStandardOut.YES, AttachScreenshot.NO);
                    Assert.fail(errorMessage);
                    break;
            }
            driver.get(configuration.url);
            reportLog("Loading URL for application under test: " + configuration.url + ".", 4, LogToStandardOut.YES, AttachScreenshot.NO);
            driver.manage().timeouts().pageLoadTimeout(configuration.timeoutsDurationsPageLoadTimeout);
            reportLog("Page load timeout set to: " + configuration.timeoutsDurationsPageLoadTimeout.getSeconds() + " seconds.", 6, LogToStandardOut.YES, AttachScreenshot.NO);
            driver.manage().timeouts().implicitlyWait(configuration.timeoutsDurationsImplicitWait);
            reportLog("Implicit wait duration set to: " + configuration.timeoutsDurationsImplicitWait.getSeconds() + " seconds.", 6, LogToStandardOut.YES, AttachScreenshot.NO);
            return driver;
        } catch (Exception e) {
            String errorMessage = "Failed to launch browser: " + browser + ".";
            reportLog(errorMessage, 1, LogToStandardOut.YES, AttachScreenshot.NO);
            Assert.fail(errorMessage, e);
            return null;
        }
    }

    /**
     * This method will return the instance of the browser driver for the current
     * thread.
     *
     * @return RemoteWebDriver - The instance of the browser driver for the current
     * thread
     */
    public RemoteWebDriver getDriver() {
        return driverThreadLocal.get();
    }

    /**
     * This method will quit the browser and remove the driver instance from the
     * ThreadLocal variable.
     */
    public void quitBrowser() {
        try {
            if (driver == null) {
                driver = getDriver();
            }
            driver.quit();
            driverThreadLocal.remove();
            reportLog("Browser quit successfully.", 4, LogToStandardOut.YES, AttachScreenshot.NO);
        } catch (Exception e) {
            String errorMessage = "Failed to quit browser.";
            reportLog(errorMessage, 3, LogToStandardOut.YES, AttachScreenshot.NO);
            Assert.fail(errorMessage, e);
        }
    }

    /**
     * This method will log a message to the TestNG and ChainTest reports.
     *
     * @param logMessage       - The message to be logged
     * @param logMessageLevel  - The level of the log message
     * @param logToStandardOut - Whether to log the message to standard
     *                         output/console (YES or NO)
     * @param attachScreenshot - Whether to attach a screenshot to the log (YES or
     *                         NO)
     */
    public void reportLog(String logMessage, int logMessageLevel, LogToStandardOut logToStandardOut, AttachScreenshot attachScreenshot) {
        // Attempt to fetch the method name of the currently executing test case and prepend it to the log message. Will ignore any error that occurs while fetching the method name and will not prepend the method name to the log message if any error occurs (e.g., if the test is not run from a test suite .xml file).
        try {
            ITestResult result = Reporter.getCurrentTestResult();
            String testCaseName = result.getMethod().getMethodName();

            logMessage = "[" + testCaseName + "] " + logMessage;
        } catch (Exception _) {
            new utilities.ChainTestReporter().reportLog("Error when fetching method name for log. Ignore if not run from test suite .xml file.", 6, configuration.reportLogLevel, logToStandardOut, this, attachScreenshot);
        }

        String currentTime = new TimeUtilities().getCurrentTimeforReportLogs();
        logMessage = "[" + currentTime + "] " + logMessage;

        new utilities.ChainTestReporter().reportLog(logMessage, logMessageLevel, configuration.reportLogLevel, logToStandardOut, this, attachScreenshot);
    }

    /**
     * This method will perform a click action on the element located by the given
     * locator.
     *
     * @param locator - The By locator of the element to be clicked
     */
    public void click(By locator) {
        try {
            new WebDriverWait(driver, configuration.timeoutsDurationsExplicitWait).until(ExpectedConditions.and(ExpectedConditions.
                    presenceOfElementLocated(locator), ExpectedConditions.visibilityOfElementLocated(locator), ExpectedConditions.elementToBeClickable(locator)));
            driver.findElement(locator).click();
            reportLog("Clicked on element located by: " + locator + ".", 4, LogToStandardOut.YES, AttachScreenshot.YES);
        } catch (Exception e) {
            String errorMessage = "Error occurred while clicking on element located by: " + locator + ".";
            reportLog(errorMessage, 2, LogToStandardOut.YES, AttachScreenshot.YES);
            Assert.fail(errorMessage, e);
        }
    }

    /**
     * This method will enter the given input string into the element located by the
     * given locator.
     *
     * @param locator     - The By locator of the element into which the input
     *                    string should be entered
     * @param inputString - The string to be entered into the element located by the
     *                    given locator
     */
    public void enterString(By locator, CharSequence inputString) {
        try {
            driver.findElement(locator).sendKeys(inputString);
            reportLog("Entered string: '" + inputString + "' into element located by: " + locator + ".", 4, LogToStandardOut.YES, AttachScreenshot.YES);
        } catch (Exception e) {
            String errorMessage = "Error occurred while entering string: '" + inputString + "' into element located by: " + locator.toString() + ".";
            reportLog(errorMessage, 2, LogToStandardOut.YES, AttachScreenshot.YES);
            Assert.fail(errorMessage, e);
        }
    }

    /**
     * This method will retrieve and return the text from the element located by the
     * given locator.
     *
     * @param locator - The By locator of the element from which to retrieve the
     *                text
     * @return String - the text retrieved from the element located by the given
     * locator
     */
    public String getText(By locator) {
        try {
            String text = driver.findElement(locator).getText();
            reportLog("Retrieved text: '" + text + "' from element located by: " + locator + ".", 4, LogToStandardOut.YES, AttachScreenshot.YES);
            return text;
        } catch (Exception e) {
            String errorMessage = "Error occurred while retrieving text from element located by: " + locator + ".";
            reportLog(errorMessage, 2, LogToStandardOut.YES, AttachScreenshot.YES);
            Assert.fail(errorMessage, e);
            return null;
        }
    }

    /**
     * This method will assert that the actual string contains the expected
     * substring.
     *
     * @param actualString      - Actual string to be checked for containing the
     *                          expected substring
     * @param expectedSubstring - Expected substring that should be contained in the
     *                          actual string
     */
    public void assertThatStringContains(String actualString, String expectedSubstring) {
        try {
            assertThat(actualString).contains(expectedSubstring);
            reportLog("Assertion passed: Expected string: '" + actualString + "' contains substring: '" + expectedSubstring + "'.", 4, LogToStandardOut.YES, AttachScreenshot.NO);
        } catch (AssertionError assertionError) {
            String errorMessage = "Assertion failed: Expected string: '" + actualString + "' does not contain substring: '" + expectedSubstring + "'.";
            reportLog(errorMessage, 2, LogToStandardOut.YES, AttachScreenshot.NO);
            Assert.fail(errorMessage, assertionError);
        }
    }

}
