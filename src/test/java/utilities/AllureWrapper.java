package utilities;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;

import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import io.qameta.allure.model.Status;

/**
 * Thin wrapper over the Allure API. Records steps and screenshots in the Allure
 * results and writes every step to the TestNG reporter / console.
 *
 * Steps and screenshots are separate: no step attaches a screenshot by itself.
 * Call {@link #attachScreenshot(WebDriver)} with the browser to capture where
 * one is wanted. It attaches to the step that is open at that moment, so to
 * attach a screenshot to a step that wraps a block, call it inside the block,
 * in a {@code finally} so it also runs when the block throws:
 *
 * <pre>
 * AllureWrapper.step("Click ...", () -&gt; {
 *     try {
 *         // action
 *     } finally {
 *         AllureWrapper.attachScreenshot(getDriver());
 *     }
 * });
 * </pre>
 *
 * Calling it after the step has returned or thrown attaches the screenshot to
 * the parent step or the test instead, because Allure has already closed the
 * step by then. If a screenshot cannot be taken, a step reporting that is
 * written instead and test execution continues.
 */
public class AllureWrapper {

    /** 24-hour timestamp prefix for console / reporter output. */
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Writes a step to the TestNG reporter and console as
     * "[yyyy-MM-dd HH:mm:ss] [test case name] step". The test name comes from the
     * TestNG result of the current thread; outside a running test it is omitted.
     */
    private static void logToConsole(String message) {
        ITestResult result = Reporter.getCurrentTestResult();
        String testName = result == null ? "" : "[" + result.getName() + "] ";
        Reporter.log("[" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + "] " + testName + message, true);
    }

    /**
     * Takes a screenshot of the given browser and attaches it to the step that is
     * currently open (or to the test when no step is open).
     *
     * Never throws: if the screenshot cannot be taken (for example the driver is
     * null because the browser is not launched yet, or the browser has already
     * closed), a BROKEN step is written instead. This matters because it is
     * called from {@code finally} blocks, where an exception would replace the
     * original failure.
     *
     * @param driver the browser to take the screenshot of
     */
    public static void attachScreenshot(WebDriver driver) {
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.attachment("Screenshot", "image/png", new ByteArrayInputStream(png),
                    AttachmentOptions.empty());
        } catch (Throwable t) {
            try {
                step("Screenshot could not be taken: " + t.getMessage(), Status.BROKEN);
            } catch (Throwable _) {
                // Reporting problems must never stop test execution.
            }
        }
    }

    /**
     * Records a passed step.
     *
     * @param name the step description
     */
    public static void step(String name) {
        logToConsole(name);
        Allure.step(name);
    }

    /**
     * Records a step with the given status.
     *
     * @param name   the step description
     * @param status the Allure status of the step (e.g. FAILED, BROKEN)
     */
    public static void step(String name, Status status) {
        logToConsole(name);
        Allure.step(name, status);
    }

    /**
     * Runs a block of code as a step. If the block throws, the step is marked
     * failed and the exception is rethrown.
     *
     * @param name     the step description
     * @param runnable the code to run inside the step
     */
    public static void step(String name, Allure.ThrowableRunnableVoid runnable) {
        logToConsole(name);
        Allure.step(name, runnable);
    }

    /**
     * Runs a block of code that returns a value as a step. If the block throws,
     * the step is marked failed and the exception is rethrown.
     *
     * @param name     the step description
     * @param runnable the code to run inside the step
     * @param <T>      the type of the returned value
     * @return the value returned by the runnable
     */
    public static <T> T step(String name, Allure.ThrowableRunnable<T> runnable) {
        logToConsole(name);
        return Allure.step(name, runnable);
    }

    /**
     * Runs a block of code as a step with a name decided inside the block. The
     * step context lets the block rename the step or add parameters. This
     * overload delegates straight to Allure, so it is not written to the TestNG
     * reporter.
     *
     * @param runnable the code to run, receiving the step context
     */
    public static void step(Allure.ThrowableContextRunnableVoid<Allure.StepContext> runnable) {
        Allure.step(runnable);
    }

    /**
     * Runs a block of code as a step and gives it the step context (to add
     * parameters or rename the step). If the block throws, the step is marked
     * failed and the exception is rethrown.
     *
     * @param name     the step description
     * @param runnable the code to run, receiving the step context
     */
    public static void step(String name, Allure.ThrowableContextRunnableVoid<Allure.StepContext> runnable) {
        logToConsole(name);
        Allure.step(name, runnable);
    }

    /**
     * Runs a block of code that returns a value as a step and gives it the step
     * context. If the block throws, the step is marked failed and the exception
     * is rethrown.
     *
     * @param name     the step description
     * @param runnable the code to run, receiving the step context
     * @return the value returned by the runnable
     */
    public static Object step(String name, Allure.ThrowableContextRunnable<Object, Allure.StepContext> runnable) {
        logToConsole(name);
        return Allure.step(name, runnable);
    }

    /**
     * Runs a block of code that returns a value as a step with a name decided
     * inside the block. This overload delegates straight to Allure, so it is not
     * written to the TestNG reporter.
     *
     * @param runnable the code to run, receiving the step context
     * @param <T>      the type of the returned value
     * @return the value returned by the runnable
     */
    public static <T> T step(Allure.ThrowableContextRunnable<T, Allure.StepContext> runnable) {
        return Allure.step(runnable);
    }

}
