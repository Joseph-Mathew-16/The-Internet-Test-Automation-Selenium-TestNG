package utilities;

import org.testng.Assert;
import testbase.TestBase;
import utilities.ChainTestReporter.AttachScreenshot;
import utilities.ChainTestReporter.LogToStandardOut;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

public class Configuration {

    private static Properties properties;
    public String url;
    public Duration timeoutsDurationsImplicitWait;
    public Duration timeoutsDurationsPageLoadTimeout;
    public Duration timeoutsDurationsExplicitWait;
    public boolean browserBrowserExecutionChrome;
    public boolean browserBrowserExecutionFirefox;
    public boolean browserBrowserExecutionEdge;
    public boolean browserBrowserExecutionSafari;
    public boolean browserHeadlessMode;
    public int browserWindowSizeWidth;
    public int browserWindowSizeHeight;
    public int retryLogicMaxRetries;
    public int reportLogLevel;

    /**
     * Constructor for the Configuration class. It loads the configuration
     * properties from the configuration.properties file and initializes the static
     * variables with the corresponding values.
     */
    public Configuration(TestBase testBase) {

        properties = new Properties();
        try {
            properties.load(new FileInputStream("./src/test/resources/configuration.properties"));
            url = properties.getProperty("url");

            timeoutsDurationsImplicitWait = Duration
                    .ofSeconds(Long.parseLong(properties.getProperty("timeouts.durations.implicit-wait")));
            timeoutsDurationsPageLoadTimeout = Duration
                    .ofSeconds(Long.parseLong(properties.getProperty("timeouts.durations.page-load-timeout")));
            timeoutsDurationsExplicitWait = Duration
                    .ofSeconds(Long.parseLong(properties.getProperty("timeouts.durations.explicit-wait")));

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
        } catch (FileNotFoundException fileNotFoundException) {
            String errorMessage = "configuration.properties file could not be opened because the file does not exist or due to some other reason.";
            testBase.reportLog(errorMessage, 0, LogToStandardOut.YES, AttachScreenshot.NO);
            Assert.fail(errorMessage, fileNotFoundException);
        } catch (IOException ioException) {
            String errorMessage = "An error occurred when reading from the input stream for configuration.properties files.";
            testBase.reportLog(errorMessage, 0, LogToStandardOut.YES, AttachScreenshot.NO);
            Assert.fail(errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            String errorMessage = "An error occurred due to the input stream for configuration.properties file containing a malformed Unicode escape sequence";
            testBase.reportLog(errorMessage, 0, LogToStandardOut.YES, AttachScreenshot.NO);
            Assert.fail(errorMessage, illegalArgumentException);
        } catch (NullPointerException nullPointerException) {
            String errorMessage = "An error occured as the input stream for configuration.properties file is null.";
            testBase.reportLog(errorMessage, 0, LogToStandardOut.YES, AttachScreenshot.NO);
            Assert.fail(errorMessage, nullPointerException);
        }
    }
}

