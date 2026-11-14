package testbase;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
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
     * Instance variable for the Configuration, a copy of the thread's value taken
     * when this object was created. There is deliberately no driver field: a test
     * class instance is shared by all the parallel data provider rows of a method,
     * so the driver must always be read from the thread-local (see getDriver()).
     */
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
        AllureWrapper.step("Initialize TestBase", () -> {
            if (getConfiguration() == null) {
                configuration = new Configuration();
                configurationThreadLocal.set(configuration);
                AllureWrapper.step("Initialized new Configuration object.", ScreenshotOption.SKIP);
            } else {
                configuration = getConfiguration();
                AllureWrapper.step("Reusing existing Configuration object.", ScreenshotOption.SKIP);
            }
            AllureWrapper.step("Reusing existing RemoteWebDriver object.", ScreenshotOption.SKIP);
        });
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
        return AllureWrapper.step("Launch " + browser + " browser.", () -> {
        try {
            RemoteWebDriver webDriver = null;
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
                    if (configuration.browserHeadlessMode) {
                        chromeOptions.addArguments("--headless=new",
                                "--window-size=" + configuration.browserWindowSizeWidth + ","
                                        + configuration.browserWindowSizeHeight);
                    }
                    webDriver = new ChromeDriver(chromeOptions);
                    driverThreadLocal.set(webDriver);
                    // Lets AllureWrapper find this driver for screenshots.
                    AllureWrapper.setTestBase(this);
                    AllureWrapper.step("Launched Chrome browser successfully.", ScreenshotOption.SKIP);
                    break;
                case "firefox":
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (configuration.browserHeadlessMode) {
                        firefoxOptions.addArguments("-headless", "--width=" + configuration.browserWindowSizeWidth,
                                "--height=" + configuration.browserWindowSizeHeight);
                    }
                    webDriver = new FirefoxDriver(firefoxOptions);
                    driverThreadLocal.set(webDriver);
                    AllureWrapper.setTestBase(this);
                    AllureWrapper.step("Launched Firefox browser successfully.", ScreenshotOption.SKIP);
                    break;
                case "edge":
                    EdgeOptions edgeOptions = new EdgeOptions();
                    if (configuration.browserHeadlessMode) {
                        edgeOptions.addArguments("--headless=new",
                                "--window-size=" + configuration.browserWindowSizeWidth + ","
                                        + configuration.browserWindowSizeHeight);
                    }
                    webDriver = new EdgeDriver(edgeOptions);
                    driverThreadLocal.set(webDriver);
                    AllureWrapper.setTestBase(this);
                    AllureWrapper.step("Launched Edge browser successfully.", ScreenshotOption.SKIP);
                    break;
                case "safari":
                    if (configuration.browserHeadlessMode) {
                        AllureWrapper.step("Safari does not support headless mode; launching with a visible window.",
                                ScreenshotOption.SKIP);
                    }
                    webDriver = new SafariDriver();
                    driverThreadLocal.set(webDriver);
                    AllureWrapper.setTestBase(this);
                    AllureWrapper.step("Launched Safari browser successfully.", ScreenshotOption.SKIP);
                    break;
                default:
                    String errorMessage = "The specified browser is not supported: " + browser
                            + ". Supported browsers are: Chrome, Firefox, Edge, Safari.";
                    AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
                    Assert.fail(errorMessage);
                    break;
            }
            webDriver.get(configuration.url);
            AllureWrapper.step("Loading URL for application under test: " + configuration.url + ".", ScreenshotOption.SKIP);
            // The timeouts are applied after the first page load, so the initial load
            // is not covered by the page load timeout.
            webDriver.manage().timeouts().pageLoadTimeout(configuration.timeoutsDurationsPageLoadTimeout);
            AllureWrapper
                    .step("Page load timeout set to: " + configuration.timeoutsDurationsPageLoadTimeout.getSeconds()
                            + " seconds.", ScreenshotOption.SKIP);
            webDriver.manage().timeouts().implicitlyWait(configuration.timeoutsDurationsImplicitWait);
            AllureWrapper
                    .step("Implicit wait duration set to: " + configuration.timeoutsDurationsImplicitWait.getSeconds()
                            + " seconds.", ScreenshotOption.SKIP);
            return webDriver;
        } catch (Exception e) {
            String errorMessage = "Failed to launch browser: " + browser + ".";
            AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
            Assert.fail(errorMessage, e);
            return null;
        }
        });
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
            // Always quit the driver registered for this thread; the test instance is
            // shared between parallel rows and must not hold a driver itself.
            RemoteWebDriver webDriver = getDriver();
            driverThreadLocal.remove();
            webDriver.quit();
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
            new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                    .until(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(locator),
                            ExpectedConditions.visibilityOfElementLocated(locator),
                            ExpectedConditions.elementToBeClickable(locator)));
            getDriver().findElement(locator).click();
            AllureWrapper.step("Clicked the element.", ScreenshotOption.SKIP);
        });
    }

    /**
     * This method will enter the given input string into the element located by the
     * given locator. It first waits, up to the explicit wait duration, until the
     * element is present, visible and enabled.
     *
     * @param locator    - The By locator of the element into which the input
     *                   string should be entered
     * @param keysToSend - The keys to be entered into the element located by the
     *                   given locator
     */
    public void sendKeys(By locator, CharSequence... keysToSend) {
        AllureWrapper.step("Enter keys " + String.join(", ", keysToSend) + " into element located by: " + locator + ".",
                () -> {
                    new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                            .until(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(locator),
                                    ExpectedConditions.visibilityOfElementLocated(locator),
                                    ExpectedConditions.elementToBeClickable(locator)));
                    getDriver().findElement(locator).sendKeys(keysToSend);
                    AllureWrapper.step("Entered keys into the element.", ScreenshotOption.SKIP);
                });
    }

    /**
     * This method will retrieve and return the text from the element located by the
     * given locator. It first waits, up to the explicit wait duration, until the
     * element is present and visible.
     *
     * @param locator - The By locator of the element from which to retrieve the
     *                text
     * @return String - the text retrieved from the element located by the given
     *         locator
     */
    public String getText(By locator) {
        return AllureWrapper.step("Get visible text from element located by: " + locator + ".", () -> {
            new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                    .until(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(locator),
                            ExpectedConditions.visibilityOfElementLocated(locator)));
            String text = getDriver().findElement(locator).getText();
            AllureWrapper.step("Retrieved text: " + text + ".", ScreenshotOption.SKIP);
            return text;
        });
    }

    /**
     * This method will clear the text of the element located by the given locator.
     * It first waits, up to the explicit wait duration, until the element is
     * present, visible and enabled.
     *
     * @param locator - The By locator of the element to be cleared
     */
    public void clear(By locator) {
        AllureWrapper.step("Clear the element located by: " + locator + ".", () -> {
            new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                    .until(ExpectedConditions.and(ExpectedConditions.presenceOfElementLocated(locator),
                            ExpectedConditions.visibilityOfElementLocated(locator),
                            ExpectedConditions.elementToBeClickable(locator)));
            getDriver().findElement(locator).clear();
            AllureWrapper.step("Cleared the element.", ScreenshotOption.SKIP);
        });
    }

    /**
     * This method will retrieve the value of an attribute (e.g. "value", "href",
     * "class") of the element located by the given locator. It first waits, up to
     * the explicit wait duration, until the element is present.
     *
     * @param locator   - The By locator of the element
     * @param attribute - The name of the attribute or property to read
     * @return String - the value of the attribute, or null if it is not set
     */
    public String getAttribute(By locator, String attribute) {
        return AllureWrapper.step("Get attribute '" + attribute + "' from element located by: " + locator + ".",
                () -> {
                    new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                            .until(ExpectedConditions.presenceOfElementLocated(locator));
                    String value = getDriver().findElement(locator).getDomProperty(attribute);
                    if (value == null) {
                        value = getDriver().findElement(locator).getDomAttribute(attribute);
                    }
                    AllureWrapper.step("Retrieved attribute value: " + value + ".", ScreenshotOption.SKIP);
                    return value;
                });
    }

    /**
     * This method will check whether the element located by the given locator is
     * displayed. It does not wait for the element to appear, and an element that
     * does not exist is reported as not displayed instead of failing the test.
     * Note that the implicit wait still applies while the element is searched for.
     *
     * @param locator - The By locator of the element
     * @return boolean - true if the element exists and is displayed
     */
    public boolean isDisplayed(By locator) {
        return AllureWrapper.step("Check if element located by: " + locator + " is displayed.", () -> {
            List<WebElement> elements = getDriver().findElements(locator);
            boolean displayed = !elements.isEmpty() && elements.get(0).isDisplayed();
            AllureWrapper.step("Element displayed: " + displayed + ".", ScreenshotOption.SKIP);
            return displayed;
        });
    }

    /**
     * This method will check whether the element located by the given locator is
     * enabled. An element that does not exist is reported as not enabled.
     *
     * @param locator - The By locator of the element
     * @return boolean - true if the element exists and is enabled
     */
    public boolean isEnabled(By locator) {
        return AllureWrapper.step("Check if element located by: " + locator + " is enabled.", () -> {
            List<WebElement> elements = getDriver().findElements(locator);
            boolean enabled = !elements.isEmpty() && elements.get(0).isEnabled();
            AllureWrapper.step("Element enabled: " + enabled + ".", ScreenshotOption.SKIP);
            return enabled;
        });
    }

    /**
     * This method will check whether the element located by the given locator
     * (e.g. a checkbox or radio button) is selected. An element that does not exist
     * is reported as not selected.
     *
     * @param locator - The By locator of the element
     * @return boolean - true if the element exists and is selected
     */
    public boolean isSelected(By locator) {
        return AllureWrapper.step("Check if element located by: " + locator + " is selected.", () -> {
            List<WebElement> elements = getDriver().findElements(locator);
            boolean selected = !elements.isEmpty() && elements.get(0).isSelected();
            AllureWrapper.step("Element selected: " + selected + ".", ScreenshotOption.SKIP);
            return selected;
        });
    }

    /**
     * This method will accept (click OK on) the JavaScript alert, confirm or prompt
     * that is currently open. It waits, up to the explicit wait duration, for the
     * alert to appear.
     */
    public void acceptAlert() {
        AllureWrapper.step("Accept the alert.", () -> {
            new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                    .until(ExpectedConditions.alertIsPresent());
            getDriver().switchTo().alert().accept();
            AllureWrapper.step("Accepted the alert.", ScreenshotOption.SKIP);
        });
    }

    /**
     * This method will dismiss (click Cancel on) the JavaScript confirm or prompt
     * that is currently open. It waits, up to the explicit wait duration, for the
     * alert to appear.
     */
    public void dismissAlert() {
        AllureWrapper.step("Dismiss the alert.", () -> {
            new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                    .until(ExpectedConditions.alertIsPresent());
            getDriver().switchTo().alert().dismiss();
            AllureWrapper.step("Dismissed the alert.", ScreenshotOption.SKIP);
        });
    }

    /**
     * This method will return the message of the JavaScript alert that is currently
     * open, leaving it open. It waits, up to the explicit wait duration, for the
     * alert to appear.
     *
     * @return String - the text of the alert
     */
    public String getAlertText() {
        return AllureWrapper.step("Get text from the alert.", () -> {
            new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                    .until(ExpectedConditions.alertIsPresent());
            String text = getDriver().switchTo().alert().getText();
            AllureWrapper.step("Retrieved alert text: " + text + ".", ScreenshotOption.SKIP);
            return text;
        });
    }

    /**
     * This method will type the given text into the JavaScript prompt that is
     * currently open. It does not close the prompt; follow it with
     * {@link #acceptAlert()} or {@link #dismissAlert()}.
     *
     * @param keysToSend - The text to type into the prompt
     */
    public void sendKeysToAlert(String keysToSend) {
        AllureWrapper.step("Enter keys " + keysToSend + " into the alert.", () -> {
            new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                    .until(ExpectedConditions.alertIsPresent());
            getDriver().switchTo().alert().sendKeys(keysToSend);
            AllureWrapper.step("Entered keys into the alert.", ScreenshotOption.SKIP);
        });
    }

    /**
     * This method will upload a file by giving it to the file input element located
     * by the given locator. The file is looked up in the folder
     * src/test/resources/test-data/Upload Files.
     *
     * Three strategies are tried in order until one works:
     * <ol>
     * <li>sendKeys of the file path to the input (works for plain inputs, hidden
     * ones included).</li>
     * <li>Make the input visible with JavaScript, then sendKeys again (for inputs
     * hidden by CSS that Selenium refuses to interact with).</li>
     * <li>Build a File in the browser from the file's content and assign it to the
     * input with a DataTransfer, firing the input and change events (for inputs
     * that sendKeys cannot reach, such as custom upload widgets).</li>
     * </ol>
     * Submitting the form or clicking the upload button remains the caller's job.
     *
     * @param locator  - The By locator of the file input element
     * @param fileName - The name of the file inside the upload folder
     */
    public void uploadFile(By locator, String fileName) {
        AllureWrapper.step("Upload file " + fileName + " using element located by: " + locator + ".", () -> {
            Path filePath = Paths.get("src/test/resources/test-data/Upload Files", fileName).toAbsolutePath();
            if (!Files.isRegularFile(filePath)) {
                String errorMessage = "File to upload does not exist: " + filePath + ".";
                AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
                Assert.fail(errorMessage);
            }
            new WebDriverWait(getDriver(), configuration.timeoutsDurationsExplicitWait)
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            WebElement input = getDriver().findElement(locator);
            JavascriptExecutor js = getDriver();

            try {
                input.sendKeys(filePath.toString());
                AllureWrapper.step("Uploaded the file using sendKeys.", ScreenshotOption.SKIP);
                return;
            } catch (InvalidElementStateException _) {
                AllureWrapper.step("sendKeys could not upload the file, making the input visible and retrying.",
                        ScreenshotOption.SKIP);
            }

            try {
                js.executeScript("var e = arguments[0];"
                        + "e.style.display = 'block'; e.style.visibility = 'visible'; e.style.opacity = '1';"
                        + "e.style.height = '1px'; e.style.width = '1px'; e.style.position = 'fixed';"
                        + "e.removeAttribute('hidden'); e.disabled = false;", input);
                input.sendKeys(filePath.toString());
                AllureWrapper.step("Uploaded the file using sendKeys after making the input visible.", ScreenshotOption.SKIP);
                return;
            } catch (InvalidElementStateException _) {
                AllureWrapper.step("sendKeys still failed, assigning the file with JavaScript.",
                        ScreenshotOption.SKIP);
            }

            String content = Base64.getEncoder().encodeToString(Files.readAllBytes(filePath));
            String mimeType = Files.probeContentType(filePath);
            js.executeScript("var input = arguments[0], name = arguments[1], type = arguments[2];"
                    + "var bytes = Uint8Array.from(atob(arguments[3]), function(c) { return c.charCodeAt(0); });"
                    + "var dt = new DataTransfer();"
                    + "dt.items.add(new File([bytes], name, { type: type }));"
                    + "input.files = dt.files;"
                    + "input.dispatchEvent(new Event('input', { bubbles: true }));"
                    + "input.dispatchEvent(new Event('change', { bubbles: true }));", input,
                    filePath.getFileName().toString(), mimeType == null ? "application/octet-stream" : mimeType,
                    content);
            AllureWrapper.step("Uploaded the file using JavaScript.", ScreenshotOption.SKIP);
        });
    }

}
