package utilities;

import org.assertj.core.api.SoftAssertions;

import io.qameta.allure.model.Status;

/**
 * Holds one AssertJ {@link SoftAssertions} instance per test method invocation
 * and per thread.
 *
 * A failed soft assertion is recorded, reported as a FAILED Allure step (with a
 * screenshot), and the test keeps running. {@link SoftAssertListener} creates a
 * new instance before every test method and calls {@link SoftAssertions#assertAll()}
 * after it, so collected failures always fail the test without the test having
 * to call assertAll() itself.
 *
 * Usage in a test: {@code softly().assertThat(actual).isEqualTo(expected);}
 */
public final class SoftAssert {

    /** The soft assertions of the test currently running on each thread. */
    private static final ThreadLocal<SoftAssertions> softAssertionsThreadLocal = new ThreadLocal<>();

    /** Utility class, not meant to be instantiated. */
    private SoftAssert() {
    }

    /**
     * Returns the SoftAssertions for the current thread, creating it if the
     * listener has not done so (e.g. when called outside a test method).
     *
     * @return the soft assertions for the current thread
     */
    public static SoftAssertions get() {
        SoftAssertions softAssertions = softAssertionsThreadLocal.get();
        if (softAssertions == null) {
            softAssertions = create();
            softAssertionsThreadLocal.set(softAssertions);
        }
        return softAssertions;
    }

    /**
     * Starts a fresh set of soft assertions for the current thread, discarding
     * any left over from a previous invocation. Called by
     * {@link SoftAssertListener} before each test method.
     */
    static void reset() {
        softAssertionsThreadLocal.set(create());
    }

    /**
     * Removes and returns the SoftAssertions for the current thread. Called by
     * {@link SoftAssertListener} after each test method so nothing leaks into
     * the next test that runs on the same thread.
     *
     * @return the removed soft assertions, or null if none was created
     */
    static SoftAssertions remove() {
        SoftAssertions softAssertions = softAssertionsThreadLocal.get();
        softAssertionsThreadLocal.remove();
        return softAssertions;
    }

    /**
     * Creates a SoftAssertions that reports each failed assertion as a FAILED
     * Allure step the moment it is collected.
     */
    private static SoftAssertions create() {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.setAfterAssertionErrorCollected(error -> AllureWrapper
                .step("Soft assertion failed: " + error.getMessage(), Status.FAILED));
        return softAssertions;
    }
}
