package wizard.runner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Runner build identity. */
public final class RunnerVersion {
  private static final String RESOURCE = "/wizard/runner-version.properties";

  private final String runnerVersion;

  private RunnerVersion(final String runnerVersion) {
    this.runnerVersion = runnerVersion;
  }

  /**
   * Loads the Gradle-provided build version.
   *
   * @return runner build identity
   * @throws IllegalStateException if the packaged resource is missing or invalid
   */
  public static RunnerVersion load() {
    try (InputStream input = RunnerVersion.class.getResourceAsStream(RESOURCE)) {
      if (input == null) {
        throw new IllegalStateException("Missing runner version resource: " + RESOURCE);
      }
      Properties properties = new UniqueProperties();
      properties.load(input);
      if (properties.size() != 1) {
        throw new IllegalStateException("Runner version resource must contain exactly one key");
      }
      String value = properties.getProperty("runnerVersion");
      if (value == null || value.isBlank()) {
        throw new IllegalStateException("Runner version resource has no runnerVersion");
      }
      return new RunnerVersion(value);
    } catch (IOException | IllegalArgumentException exception) {
      throw new IllegalStateException("Cannot read runner version resource", exception);
    }
  }

  private static final class UniqueProperties extends Properties {
    @Override
    public synchronized Object put(final Object key, final Object value) {
      if (containsKey(key)) {
        throw new IllegalArgumentException("Duplicate runner version property: " + key);
      }
      return super.put(key, value);
    }
  }

  /**
   * Returns the Gradle-provided build version.
   *
   * @return runner build version
   */
  public String runnerVersion() {
    return runnerVersion;
  }
}
