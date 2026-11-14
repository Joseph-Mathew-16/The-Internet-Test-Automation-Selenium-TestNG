package testbase;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import utilities.AllureWrapper;
import utilities.Configuration;
import utilities.ScreenshotOption;
import utilities.SoftAssert;

/**
 * Core of the framework. Launches and quits browsers, keeps the driver and
 * Configuration per thread so tests can run in parallel, and provides the basic
 * element actions (click, send keys, get text) that page objects build on.
 * Every action is recorded as an Allure step.
 */
public class TestBase {

    /**
     * ThreadLocal variables to store RemoteWebDriver and Configuration instances
     * for each thread. Each parallel test therefore has its own browser, and page
     * objects created on the same thread find it without it being passed around.
     */
    public static ThreadLocal<RemoteWebDriver> driverThreadLocal = new ThreadLocal<>();
    public static ThreadLocal<Configuration> configurationThreadLocal = new ThreadLocal<>();

    /**
     * Instance variables for RemoteWebDriver and Configuration. They are copies of
     * the thread's values taken when this object was created.
     */
    public RemoteWebDriver driver;
    public Configuration configuration;

    /**
     * Constructor for the TestBase class. It initializes the Configuration and
     * RemoteWebDriver instances for the current thread.
     *
     * The first object created on a thread loads the Configuration; later ones,
     * such as page objects, reuse it. The driver is whatever is registered for the
     * thread, so it is null until {@link #launchBrowser(String)} has run.
     */
    public TestBase() {
        if (getConfiguration() == null) {
            configuration = new Configuration();
            configurationThreadLocal.set(configuration);
            AllureWrapper.step("Initialized new Configuration object.", ScreenshotOption.SKIP);
        } else {
            configuration = getConfiguration();
            AllureWrapper.step("Reusing existing Configuration object.", ScreenshotOption.SKIP);
        }
        driver = getDriver();
        AllureWrapper.step("Reusing existing RemoteWebDriver object.", ScreenshotOption.SKIP);
    }

    /**
     * Constructor for the TestBase class. It uses the provided Configuration for
     * this instance only, leaving the Configuration stored for the current thread
     * unchanged. Use it to temporarily override configuration values (e.g. the
     * explicit wait duration) for specific actions.
     *
     * @param configuration - The Configuration to be used by this instance
     */
    public TestBase(Configuration configuration) {
        this.configuration = configuration;
        AllureWrapper.step("Using provided temporary Configuration object.", ScreenshotOption.SKIP);

        driver = getDriver();
        AllureWrapper.step("Reusing existing RemoteWebDriver object.", ScreenshotOption.SKIP);
    }

    /**
     * This method will return the instance of the Configuration for the current
     * thread.
     *
     * @return Configuration - The instance of the Configuration for the current
     *         thread, or null if none has been created yet
     */
    public Configuration getConfiguration() {
        return configurationThreadLocal.get();
    }

    /**
     * This method will launch the browser based on the parameter passed, register
     * it for the current thread and open the application URL. Fails the test if
     * the browser is not supported or cannot be started.
     *
     * @param browser - The name of the browser to be launched (e.g., "chrome",
     *                "firefox", "edge", "safari")
     * @return RemoteWebDriver - The instance of the launched browser driver
     */
    public RemoteWebDriver launchBrowser(String browser) {
        try {
            switch (browser) {
                case "chrome":
                    ChromeOptions chromeOptions = new ChromeOptions();
                    // Turn off Chrome's password manager and leak-detection popups, which
                    // would otherwise cover the page after a login.
                    Map<String, Object> prefs = new HashMap<>();
                    prefs.put("credentials_enable_service", false);
                    prefs.put("profile.password_manager_enabled", false);
                    prefs.put("profile.password_manager_leak_detection", false);
                    chromeOptions.setExperimentalOption("prefs", prefs);
                    chromeOptions
                            .addArguments("--disable-features=PasswordLeakDetection,SafetyCheckPasswordsLeakDetection");
                    driver = new ChromeDriver(chromeOptions);
                    driverThreadLocal.set(driver);
                    // Lets AllureWrapper find this driver for screenshots.
                    AllureWrapper.setTestBase(this);
                    AllureWrapper.step("Launched Chrome browser successfully.");
                    break;
                case "firefox":
                    driver = new FirefoxDriver();
                    driverThreadLocal.set(driver);
                    AllureWrapper.setTestBase(this);
                    AllureWrapper.step("Launched Firefox browser successfully.");
                    break;
                case "edge":
                    driver = new EdgeDriver();
                    driverThreadLocal.set(driver);
                    AllureWrapper.setTestBase(this);
                    AllureWrapper.step("Launched Edge browser successfully.");
                    break;
                case "safari":
                    driver = new SafariDriver();
                    driverThreadLocal.set(driver);
                    AllureWrapper.setTestBase(this);
                    AllureWrapper.step("Launched Safari browser successfully.");
                    break;
                default:
                    String errorMessage = "The specified browser is not supported: " + browser
                            + ". Supported browsers are: Chrome, Firefox, Edge, Safari.";
                    AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
                    Assert.fail(errorMessage);
                    break;
            }
            driver.get(configuration.url);
            AllureWrapper.step("Loading URL for application under test: " + configuration.url + ".");
            // The timeouts are applied after the first page load, so the initial load
            // is not covered by the page load timeout.
            driver.manage().timeouts().pageLoadTimeout(configuration.timeoutsDurationsPageLoadTimeout);
            AllureWrapper
                    .step("Page load timeout set to: " + configuration.timeoutsDurationsPageLoadTimeout.getSeconds()
                            + " seconds.",ScreenshotOption.SKIP);
            driver.manage().timeouts().implicitlyWait(configuration.timeoutsDurationsImplicitWait);
            AllureWrapper
                    .step("Implicit wait duration set to: " + configuration.timeoutsDurationsImplicitWait.getSeconds()
                            + " seconds.",ScreenshotOption.SKIP);
            return driver;
        } catch (Exception e) {
            String errorMessage = "Failed to launch browser: " + browser + ".";
            AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
            Assert.fail(errorMessage, e);
            return null;
        }
    }

    /**
     * This method will return the instance of the browser driver for the current
     * thread.
     *
     * @return RemoteWebDriver - The instance of the browser driver for the current
     *         thread, or null if no browser has been launched
     */
    public RemoteWebDriver getDriver() {
        return driverThreadLocal.get();
    }

    /**
     * This method will return the soft assertions for the current test. A failed
     * soft assertion is reported but does not stop the test; all failures are
     * verified automatically when the test method ends (see
     * utilities.SoftAssertListener).
     *
     * @return SoftAssertions - The soft assertions for the current test
     */
    public SoftAssertions softly() {
        return SoftAssert.get();
    }

    /**
     * This method will quit the browser and remove the driver instance from the
     * ThreadLocal variable.
     */
    public void quitBrowser() {
        try {
            // This object may have been created before the browser was launched, so
            // fall back to the driver registered for the thread.
            if (driver == null) {
                driver = getDriver();
            }
            driver.quit();
            driverThreadLocal.remove();
            AllureWrapper.clearTestBase();
            AllureWrapper.step("Browser quit successfully.", ScreenshotOption.SKIP);
        } catch (Exception e) {
            String errorMessage = "Failed to quit browser.";
            AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
            Assert.fail(errorMessage, e);
        }
    }

    /**
     * This method will perform a click action on the element located by the given
     * locator. It first waits, up to the explicit wait duration, until the element
     * is present, visible and clickable.
     *
     * @param locator - The By locator of the element to be clicked
     */
    public void click(By locator) {
        AllureWrapper.step("Click an element located by: " + locator + ".", () -> {
            new WebDriverWait(driver, configuration.timeoutsDurationsExplicitWait)
                    .until(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(locator),
                            ExpectedConditions.visibilityOfElementLocated(locator),
                            ExpectedConditions.elementToBeClickable(locator)));
            driver.findElement(locator).click();
            AllureWrapper.step("Clicked the element.");
        });
    }

    /**
     * This method will enter the given input string into the element located by the
     * given locator. It relies on the implicit wait to find the element.
     *
     * @param locator    - The By locator of the element into which the input
     *                   string should be entered
     * @param keysToSend - The keys to be entered into the element located by the
     *                   given locator
     */
    public void sendKeys(By locator, CharSequence... keysToSend) {
        AllureWrapper.step("Enter keys " + String.join(", ", keysToSend) + " into element located by: " + locator + ".",
                () -> {
                    // TODO: Implement explicit wait.
                    driver.findElement(locator).sendKeys(keysToSend);
                    AllureWrapper.step("Entered keys into the element.");
                });
    }

    /**
     * This method will retrieve and return the text from the element located by the
     * given locator. It relies on the implicit wait to find the element.
     *
     * @param locator - The By locator of the element from which to retrieve the
     *                text
     * @return String - the text retrieved from the element located by the given
     *         locator
     */
    public String getText(By locator) {
        return AllureWrapper.step("Get visible text from element located by: " + locator + ".", () -> {
            // TODO: Implement explicit wait.
            String text = driver.findElement(locator).getText();
            AllureWrapper.step("Retrieved text: " + text + ".", ScreenshotOption.SKIP);
            return text;
        });
    }

}
