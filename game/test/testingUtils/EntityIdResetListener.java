package testingUtils;

import engine.utils.EntityIdProvider;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * Resets the {@link EntityIdProvider} after each test, so every test starts with an empty ID
 * registry.
 *
 * <p>The reset runs after a test instead of before it, because JUnit creates the test instance
 * before a test starts and entities in instance fields must keep their registered IDs.
 */
public class EntityIdResetListener implements TestExecutionListener {
  @Override
  public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
    if (testIdentifier.isTest()) {
      EntityIdProvider.reset();
    }
  }
}
