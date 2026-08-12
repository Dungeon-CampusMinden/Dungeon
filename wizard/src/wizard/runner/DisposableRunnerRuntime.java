package wizard.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Function;

/** Owns the exact lifecycle of one disposable Runner working directory. */
final class DisposableRunnerRuntime {
  private static final String RUNTIME_PREFIX = "wizard-runner-runtime-";
  private static final String LOG_DIRECTORY_PROPERTY = "BASELOGDIR";
  private static final String REFLECTION_DIRECTORY_PROPERTY = "BASEREFLECTIONDIR";

  private DisposableRunnerRuntime() {}

  static <T> T run(final Function<Path, T> operation) {
    Path runtime = create();
    String previousLogDirectory = System.getProperty(LOG_DIRECTORY_PROPERTY);
    String previousReflectionDirectory = System.getProperty(REFLECTION_DIRECTORY_PROPERTY);
    System.setProperty(LOG_DIRECTORY_PROPERTY, runtime + File.separator);
    System.setProperty(REFLECTION_DIRECTORY_PROPERTY, runtime.resolve("reflection").toString());
    try {
      return operation.apply(runtime);
    } finally {
      restoreProperty(LOG_DIRECTORY_PROPERTY, previousLogDirectory);
      restoreProperty(REFLECTION_DIRECTORY_PROPERTY, previousReflectionDirectory);
      delete(runtime);
    }
  }

  private static void restoreProperty(final String name, final String value) {
    if (value == null) {
      System.clearProperty(name);
    } else {
      System.setProperty(name, value);
    }
  }

  private static Path create() {
    try {
      return Files.createTempDirectory(RUNTIME_PREFIX).toRealPath();
    } catch (IOException | SecurityException exception) {
      throw new IllegalStateException(
          "Cannot create disposable Runner runtime directory", exception);
    }
  }

  private static void delete(final Path runtime) {
    try (var paths = Files.walk(runtime)) {
      for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(path);
      }
    } catch (IOException | SecurityException exception) {
      throw new IllegalStateException(
          "Cannot delete disposable Runner runtime directory " + runtime, exception);
    }
  }
}
