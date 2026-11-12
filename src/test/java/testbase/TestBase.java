package testbase;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import io.qameta.allure.Step;
import utilities.AllureWrapper;
import utilities.Configuration;

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
            AllureWrapper.step("Initialized new Configuration object.");
        } else {
            configuration = getConfiguration();
            AllureWrapper.step("Reusing existing Configuration object.");
        }
        driver = getDriver();
        AllureWrapper.step("Reusing existing RemoteWebDriver object.");
    }

    /**
     * Constructor for the TestBase class. It initializes the Configuration and
     * RemoteWebDriver instances for the current thread. It sets a provided Duration
     * as the explicit wait duration.
     */
    public TestBase(Duration explicitWaitDuration) {
        configuration = new Configuration(this);
        configuration.timeoutsDurationsExplicitWait = explicitWaitDuration;
        AllureWrapper.step("Initialized new Configuration object. Explicit wait duration set as "
                + explicitWaitDuration.toString() + ".");

        driver = getDriver();
        AllureWrapper.step("Reusing existing RemoteWebDriver object.");
    }

    /**
     * This method will return the instance of the Configuration for the current
     * thread.
     *
     * @return Configuration - The instance of the Configuration for the current
     *         thread
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
    @Step("Launch browser.")
    public RemoteWebDriver launchBrowser(String browser) {
        try {
            switch (browser) {
                case "chrome":
                    driver = new ChromeDriver();
                    driverThreadLocal.set(driver);
                    AllureWrapper.step("Launched Chrome browser successfully.");
                    break;
                case "firefox":
                    driver = new FirefoxDriver();
                    driverThreadLocal.set(driver);
                    AllureWrapper.step("Launched Firefox browser successfully.");
                    break;
                case "edge":
                    driver = new EdgeDriver();
                    driverThreadLocal.set(driver);
                    AllureWrapper.step("Launched Edge browser successfully.");
                    break;
                case "safari":
                    driver = new SafariDriver();
                    driverThreadLocal.set(driver);
                    AllureWrapper.step("Launched Safari browser successfully.");
                    break;
                default:
                    String errorMessage = "The specified browser is not supported: " + browser
                            + ". Supported browsers are: Chrome, Firefox, Edge, Safari.";
                    AllureWrapper.step(errorMessage);
                    Assert.fail(errorMessage);
                    break;
            }
            driver.get(configuration.url);
            AllureWrapper.step("Loading URL for application under test: " + configuration.url + ".");
            driver.manage().timeouts().pageLoadTimeout(configuration.timeoutsDurationsPageLoadTimeout);
            AllureWrapper.step("Page load timeout set to: " + configuration.timeoutsDurationsPageLoadTimeout.getSeconds()
                    + " seconds.");
            driver.manage().timeouts().implicitlyWait(configuration.timeoutsDurationsImplicitWait);
            AllureWrapper.step("Implicit wait duration set to: " + configuration.timeoutsDurationsImplicitWait.getSeconds()
                    + " seconds.");
            return driver;
        } catch (Exception e) {
            String errorMessage = "Failed to launch browser: " + browser + ".";
            AllureWrapper.step(errorMessage);
            Assert.fail(errorMessage, e);
            return null;
        }
    }

    /**
     * This method will return the instance of the browser driver for the current
     * thread.
     *
     * @return RemoteWebDriver - The instance of the browser driver for the current
     *         thread
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
            AllureWrapper.step("Browser quit successfully.");
        } catch (Exception e) {
            String errorMessage = "Failed to quit browser.";
            AllureWrapper.step(errorMessage);
            Assert.fail(errorMessage, e);
        }
    }

    /**
     * This method will perform a click action on the element located by the given
     * locator.
     *
     * @param locator - The By locator of the element to be clicked
     */
    @Step("Click an element.")
    public void click(By locator) {
        AllureWrapper.step("Click an element located by: " + locator + ".");
        new WebDriverWait(driver, configuration.timeoutsDurationsExplicitWait)
                .until(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(locator),
                        ExpectedConditions.visibilityOfElementLocated(locator),
                        ExpectedConditions.elementToBeClickable(locator)));
        driver.findElement(locator).click();
    }

    /**
     * This method will enter the given input string into the element located by the
     * given locator.
     *
     * @param locator    - The By locator of the element into which the input
     *                   string should be entered
     * @param keysToSend - The keys to be entered into the element located by the
     *                   given locator
     */
    @Step("Enter keys into an element.")
    public void sendKeys(By locator, CharSequence... keysToSend) {
        AllureWrapper.step("Enter keys " + String.join(", ", keysToSend));
        driver.findElement(locator).sendKeys(keysToSend);
    }

    /**
     * This method will retrieve and return the text from the element located by the
     * given locator.
     *
     * @param locator - The By locator of the element from which to retrieve the
     *                text
     * @return String - the text retrieved from the element located by the given
     *         locator
     */
    @Step("Get visible text of an element.")
    public String getText(By locator) {
        AllureWrapper.step("Getting visible text from element located by: " + locator + ".");
        String text = driver.findElement(locator).getText();
        AllureWrapper.step("Retrieved text: " + text + ".");
        return text;

    }

    /**
     * This method will assert that the actual string contains the expected
     * values.
     *
     * @param actual - Actual string to be checked for containing the
     *               expected substring
     * @param values - Expected values that should be contained in the
     *               actual string
     */
    @Step("Assert that string contains some values")
    public void assertThatStringContains(String actual, CharSequence... values) {
        AllureWrapper.step("Asserting that actual string: " + actual + " contains expected values: " + String.join(", ", values) + ".");
        assertThat(actual).contains(values);
    }

}
