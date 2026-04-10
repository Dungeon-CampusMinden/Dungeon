package testingUtils;

import org.junit.platform.launcher.TestExecutionListener;

/**
 * Minimal test listener used by the JUnit service loader in the test assets.
 *
 * <p>The service entry already exists in the repository. Providing this no-op implementation keeps
 * the launcher bootstrap working for targeted JVM tests that do not require libGDX setup.
 */
public final class LibgdxTestStartupListener implements TestExecutionListener {}
