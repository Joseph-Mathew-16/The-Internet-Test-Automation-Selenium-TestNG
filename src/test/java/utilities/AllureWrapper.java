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
import testbase.TestBase;

/**
 * Thin wrapper over the Allure API. Records steps and screenshots in the Allure
 * results and writes every step to the TestNG reporter / console.
 *
 * Plain steps (no runnable) get a screenshot attached unless
 * {@link ScreenshotOption#SKIP} is passed. Steps that take a runnable do not
 * attach a screenshot themselves; the steps inside the runnable do. When a
 * runnable throws, a screenshot of the failure is attached before the
 * exception is rethrown.
 *
 * The TestBase (and through it the driver) is registered per thread through
 * {@link #setTestBase(TestBase)}, which TestBase calls when the browser is
 * launched. If a screenshot cannot be taken, a step reporting that is written
 * instead and test execution continues.
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
     * TestBase used to reach the driver for screenshots. One per thread so tests
     * running in parallel never take screenshots of each other's browsers.
     */
    private static final ThreadLocal<TestBase> testBaseThreadLocal = new ThreadLocal<>();

    /**
     * Registers the TestBase for the current thread.
     *
     * @param testBase the TestBase whose driver is used for screenshots
     */
    public static void setTestBase(TestBase testBase) {
        testBaseThreadLocal.set(testBase);
    }

    /**
     * Removes the TestBase registered for the current thread. Called when the
     * browser is quit so the thread does not keep a stale reference.
     */
    public static void clearTestBase() {
        testBaseThreadLocal.remove();
    }

    /**
     * Takes a screenshot and attaches it to the Allure results unless
     * {@code screenshotOption} is {@link ScreenshotOption#SKIP}.
     *
     * Never throws: if the screenshot cannot be taken (for example the browser
     * is not launched yet or has already closed), a BROKEN step is written
     * instead.
     *
     * @param screenshotOption whether a screenshot should be attached
     */
    private static void attachScreenshot(ScreenshotOption screenshotOption) {
        if (screenshotOption == ScreenshotOption.SKIP) {
            return;
        }
        try {
            TestBase testBase = testBaseThreadLocal.get();
            WebDriver driver = testBase.getDriver();
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.attachment("Screenshot", "image/png", new ByteArrayInputStream(png),
                    AttachmentOptions.empty());
        } catch (Throwable t) {
            try {
                // SKIP is passed so a failing screenshot cannot trigger another screenshot attempt.
                step("Screenshot could not be taken" + t.getMessage(), Status.BROKEN, ScreenshotOption.SKIP);
            } catch (Throwable _) {
                // Reporting problems must never stop test execution.
            }
        }
    }

    /**
     * Records a passed step with a screenshot.
     *
     * @param name the step description
     */
    public static void step(String name) {
        step(name, ScreenshotOption.ATTACH);
    }

    /**
     * Records a passed step.
     *
     * @param name             the step description
     * @param screenshotOption whether a screenshot is attached to the step
     */
    public static void step(String name, ScreenshotOption screenshotOption) {
        logToConsole(name);
        // The screenshot is taken inside the step so Allure shows it under this step.
        Allure.step(name, () -> {
            attachScreenshot(screenshotOption);
        });
    }

    /**
     * Records a step with the given status and a screenshot.
     *
     * @param name   the step description
     * @param status the Allure status of the step (e.g. FAILED, BROKEN)
     */
    public static void step(String name, Status status) {
        step(name, status, ScreenshotOption.ATTACH);
    }

    /**
     * Records a step with the given status.
     *
     * @param name             the step description
     * @param status           the Allure status of the step (e.g. FAILED, BROKEN)
     * @param screenshotOption whether a screenshot is attached
     */
    public static void step(String name, Status status, ScreenshotOption screenshotOption) {
        logToConsole(name);
        Allure.step(name, status);
        // Allure.step(name, status) creates a step with no body, so the screenshot is
        // attached to whichever step or test is currently open.
        attachScreenshot(screenshotOption);
    }

    /**
     * Runs a block of code as a step. A screenshot is always attached after the
     * block finishes, whether it passes or throws. If the block throws, the
     * exception is rethrown so the step is marked failed.
     *
     * @param name     the step description
     * @param runnable the code to run inside the step
     */
    public static void step(String name, Allure.ThrowableRunnableVoid runnable) {
        logToConsole(name);
        Allure.step(name, () -> {
            try {
                runnable.run();
            } finally {
                attachScreenshot(ScreenshotOption.ATTACH);
            }
        });
    }

    /**
     * Runs a block of code that returns a value as a step. A screenshot is
     * always attached after the block finishes, whether it passes or throws. If
     * the block throws, the exception is rethrown.
     *
     * @param name     the step description
     * @param runnable the code to run inside the step
     * @param <T>      the type of the returned value
     * @return the value returned by the runnable
     */
    public static <T> T step(String name, Allure.ThrowableRunnable<T> runnable) {
        logToConsole(name);
        return Allure.step(name, () -> {
            try {
                return runnable.run();
            } finally {
                attachScreenshot(ScreenshotOption.ATTACH);
            }
        });
    }

    /**
     * Runs a block of code as a step with a name decided inside the block. The
     * step context lets the block rename the step or add parameters. This
     * overload delegates straight to Allure, so it has no failure screenshot and
     * is not written to the TestNG reporter.
     *
     * @param runnable the code to run, receiving the step context
     */
    public static void step(Allure.ThrowableContextRunnableVoid<Allure.StepContext> runnable) {
        Allure.step(runnable);
    }

    /**
     * Runs a block of code as a step and gives it the step context (to add
     * parameters or rename the step). A screenshot is always attached after the
     * block finishes, whether it passes or throws. If the block throws, the
     * exception is rethrown.
     *
     * @param name     the step description
     * @param runnable the code to run, receiving the step context
     */
    public static void step(String name, Allure.ThrowableContextRunnableVoid<Allure.StepContext> runnable) {
        logToConsole(name);
        Allure.step(name, context -> {
            try {
                runnable.run(context);
            } finally {
                attachScreenshot(ScreenshotOption.ATTACH);
            }
        });
    }

    /**
     * Runs a block of code that returns a value as a step and gives it the step
     * context. A screenshot is always attached after the block finishes, whether
     * it passes or throws. If the block throws, the exception is rethrown.
     *
     * @param name     the step description
     * @param runnable the code to run, receiving the step context
     * @return the value returned by the runnable
     */
    public static Object step(String name, Allure.ThrowableContextRunnable<Object, Allure.StepContext> runnable) {
        logToConsole(name);
        // The cast selects the context-aware Allure.step overload; the lambda alone is ambiguous.
        return Allure.step(name, (Allure.ThrowableContextRunnable<Object, Allure.StepContext>) context -> {
            try {
                return runnable.run(context);
            } finally {
                attachScreenshot(ScreenshotOption.ATTACH);
            }
        });
    }

    /**
     * Runs a block of code that returns a value as a step with a name decided
     * inside the block. This overload delegates straight to Allure, so it has no
     * failure screenshot and is not written to the TestNG reporter.
     *
     * @param runnable the code to run, receiving the step context
     * @param <T>      the type of the returned value
     * @return the value returned by the runnable
     */
    public static <T> T step(Allure.ThrowableContextRunnable<T, Allure.StepContext> runnable) {
        return Allure.step(runnable);
    }

}
