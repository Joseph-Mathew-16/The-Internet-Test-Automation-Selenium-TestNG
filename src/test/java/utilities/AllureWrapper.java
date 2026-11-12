package utilities;

import java.io.ByteArrayInputStream;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
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
 * attach a screenshot themselves; the steps inside the runnable do. The
 * TestBase (and through it the driver and Configuration) is registered per
 * thread through {@link #setTestBase(TestBase)}, done by TestBase when the
 * browser is launched. If a screenshot cannot be taken, a step reporting that
 * is written instead and test execution continues.
 */
public class AllureWrapper {

    /**
     * TestBase used to reach the driver and configuration, one per thread.
     */
    private static final ThreadLocal<TestBase> testBaseThreadLocal = new ThreadLocal<>();

    /**
     * Registers the TestBase for the current thread.
     */
    public static void setTestBase(TestBase testBase) {
        testBaseThreadLocal.set(testBase);
    }

    /**
     * Removes the TestBase registered for the current thread.
     */
    public static void clearTestBase() {
        testBaseThreadLocal.remove();
    }

    /**
     * Takes a screenshot and attaches it to the Allure results unless
     * {@code screenshotOption} is {@link ScreenshotOption#SKIP}. Never throws: if the screenshot cannot be
     * taken, a BROKEN step is written instead.
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
                step("Screenshot could not be taken" + t.getMessage(), Status.BROKEN, ScreenshotOption.SKIP);
            } catch (Throwable ignored) {
                // Reporting problems must never stop test execution.
            }
        }
    }

    public static void step(String name) {
        step(name, ScreenshotOption.ATTACH);
    }

    public static void step(String name, ScreenshotOption screenshotOption) {
        Reporter.log(name, true);
        Allure.step(name, () -> {
            attachScreenshot(screenshotOption);
        });
    }

    public static void step(String name, Status status) {
        step(name, status, ScreenshotOption.ATTACH);
    }

    public static void step(String name, Status status, ScreenshotOption screenshotOption) {
        Reporter.log(name, true);
        Allure.step(name, status);
        attachScreenshot(screenshotOption);
    }

    public static void step(String name, Allure.ThrowableRunnableVoid runnable) {
        Reporter.log(name, true);
        Allure.step(name, runnable);
    }

    public static <T> T step(String name, Allure.ThrowableRunnable<T> runnable) {
        Reporter.log(name, true);
        return Allure.step(name, runnable);
    }

    public static void step(Allure.ThrowableContextRunnableVoid<Allure.StepContext> runnable) {
        Allure.step(runnable);
    }

    public static void step(String name, Allure.ThrowableContextRunnableVoid<Allure.StepContext> runnable) {
        Reporter.log(name, true);
        Allure.step(name, runnable);
    }

    public static Object step(String name, Allure.ThrowableContextRunnable<Object, Allure.StepContext> runnable) {
        Reporter.log(name, true);
        return Allure.step(name, runnable);
    }

    public static <T> T step(Allure.ThrowableContextRunnable<T, Allure.StepContext> runnable) {
        return Allure.step(runnable);
    }

}
