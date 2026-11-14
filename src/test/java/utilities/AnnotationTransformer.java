package utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * TestNG listener that attaches {@link RetryAnalyzer} to every test method at
 * runtime, so individual tests do not need {@code retryAnalyzer = ...} in their
 * {@code @Test} annotation. Registered in regression-test-suite.xml.
 */
public class AnnotationTransformer implements IAnnotationTransformer {

    /**
     * Called by TestNG once for every {@code @Test} annotation before it runs.
     */
    @Override
    public void transform(ITestAnnotation annotation,
            Class testClass,
            Constructor testConstructor,
            Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
