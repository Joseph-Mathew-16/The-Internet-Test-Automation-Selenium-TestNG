package utilities;

/**
 * Decides whether {@link AllureWrapper} attaches a screenshot to a step.
 */
public enum ScreenshotOption {

    /** Attach a screenshot to the step. */
    ATTACH,

    /**
     * Do not attach a screenshot to the step. Use it for framework steps where
     * there is nothing useful to see, or where no browser is open yet.
     */
    SKIP
}
