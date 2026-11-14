package utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.testng.Assert;

/**
 * Typed view of src/test/resources/configuration.properties.
 *
 * The file is read once for the whole JVM. Each {@code new Configuration()}
 * copies the values into its own fields, so a test can change one instance
 * (e.g. a longer explicit wait) without affecting any other. Any missing or
 * invalid value fails the test immediately with a descriptive message.
 */
public class Configuration {

    /**
     * Properties loaded from the configuration.properties file. The file is read
     * only once, when the class is first used, and every Configuration object
     * reads its values from this in-memory copy.
     */
    private static final Properties properties = new Properties();

    /** Base URL of the application under test. */
    public String url;

    /** Selenium implicit wait, applied to every element lookup. */
    public Duration timeoutsDurationsImplicitWait;
    /** Maximum time to wait for a page to finish loading. */
    public Duration timeoutsDurationsPageLoadTimeout;
    /** Maximum time {@code WebDriverWait} waits for an element condition, e.g. clickable. */
    public Duration timeoutsDurationsExplicitWait;

    /** Which browsers the data provider runs each test on. */
    public boolean browserBrowserExecutionChrome;
    public boolean browserBrowserExecutionFirefox;
    public boolean browserBrowserExecutionEdge;
    public boolean browserBrowserExecutionSafari;
    public boolean browserHeadlessMode;
    public int browserWindowSizeWidth;
    public int browserWindowSizeHeight;

    /** How many times {@link RetryAnalyzer} re-runs a failed test. */
    public int retryLogicMaxRetries;

    /** Highest step level written to the report (see configuration.properties for the levels). */
    public int reportLogLevel;

    /**
     * Loads the configuration.properties file into the static properties object.
     * This runs once, the first time the Configuration class is used. The path is
     * relative to the project root, which is the working directory for Maven.
     */
    static {
        try (FileInputStream inputStream = new FileInputStream("./src/test/resources/configuration.properties")) {
            properties.load(inputStream);
            AllureWrapper.step("Loaded configuration.properties file.", ScreenshotOption.SKIP);
        } catch (FileNotFoundException fileNotFoundException) {
            String errorMessage = "configuration.properties file could not be opened because the file does not exist, is a directory rather than a regular file, or cannot be opened for reading (e.g. missing permissions or the file is locked).";
            AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
            Assert.fail(errorMessage, fileNotFoundException);
        } catch (IOException ioException) {
            String errorMessage = "An error occurred when reading from or closing the input stream for configuration.properties file.";
            AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
            Assert.fail(errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            String errorMessage = "An error occurred due to the configuration.properties file containing a malformed Unicode escape sequence (a '\\u' not followed by four hexadecimal digits).";
            AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
            Assert.fail(errorMessage, illegalArgumentException);
        }
    }

    /**
     * Constructor for the Configuration class. It initializes the instance
     * variables with the corresponding values from the properties already loaded
     * from the configuration.properties file.
     */
    public Configuration() {
        try {
            url = properties.getProperty("url");

            // Wait durations are written in whole seconds in the file.
            timeoutsDurationsImplicitWait = Duration
                    .ofSeconds(Long.parseLong(properties.getProperty("timeouts.durations.implicit-wait")));
            timeoutsDurationsPageLoadTimeout = Duration
                    .ofSeconds(Long.parseLong(properties.getProperty("timeouts.durations.page-load-timeout")));
            timeoutsDurationsExplicitWait = Duration
                    .ofSeconds(Long.parseLong(properties.getProperty("timeouts.durations.explicit-wait")));

            // parseBoolean never throws: a missing or misspelled value silently becomes false.
            browserBrowserExecutionChrome = Boolean
                    .parseBoolean(properties.getProperty("browser.browser-execution.chrome"));
            browserBrowserExecutionFirefox = Boolean
                    .parseBoolean(properties.getProperty("browser.browser-execution.firefox"));
            browserBrowserExecutionEdge = Boolean
                    .parseBoolean(properties.getProperty("browser.browser-execution.edge"));
            browserBrowserExecutionSafari = Boolean
                    .parseBoolean(properties.getProperty("browser.browser-execution.safari"));
            browserHeadlessMode = Boolean.parseBoolean(properties.getProperty("browser.headless-mode"));

            browserWindowSizeWidth = Integer.parseInt(properties.getProperty("browser.window-size.width"));
            browserWindowSizeHeight = Integer.parseInt(properties.getProperty("browser.window-size.height"));

            retryLogicMaxRetries = Integer.parseInt(properties.getProperty("retry-logic.max-retries"));

            reportLogLevel = Integer.parseInt(properties.getProperty("report.log-level"));
        } catch (IllegalArgumentException illegalArgumentException) {
            // NumberFormatException is a subclass of IllegalArgumentException.
            String errorMessage = "An error occurred while reading a value from configuration.properties file. A numeric property is either missing or misspelled, empty, not a whole number (e.g. contains letters, spaces or decimals), or too large for its type.";
            AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
            Assert.fail(errorMessage, illegalArgumentException);
        } catch (NullPointerException nullPointerException) {
            String errorMessage = "An error occurred as a value read from configuration.properties file is null. A property is missing or misspelled in the file and its value was used where null is not allowed.";
            AllureWrapper.step(errorMessage, ScreenshotOption.SKIP);
            Assert.fail(errorMessage, nullPointerException);
        }
    }
}
