package wizard.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

/** Lifecycle tests for disposable Runner runtime cleanup. */
@ResourceLock(Resources.SYSTEM_PROPERTIES)
final class DisposableRunnerRuntimeTest {
  @Test
  void scopesPropertiesAndDeletesTheRuntime() {
    Path workingDirectory = Path.of("").toAbsolutePath().normalize();
    String previousLogDirectory = System.getProperty("BASELOGDIR");
    String previousReflectionDirectory = System.getProperty("BASEREFLECTIONDIR");
    String previousTempDirectory = System.getProperty("java.io.tmpdir");
    AtomicReference<Path> runtime = new AtomicReference<>();

    DisposableRunnerRuntime.run(
        directory -> {
          runtime.set(directory);
          assertEquals(
              directory, Path.of(System.getProperty("BASELOGDIR")).toAbsolutePath().normalize());
          assertEquals(
              directory.resolve("reflection"),
              Path.of(System.getProperty("BASEREFLECTIONDIR")).toAbsolutePath().normalize());
          assertEquals(previousTempDirectory, System.getProperty("java.io.tmpdir"));
          assertEquals(workingDirectory, Path.of("").toAbsolutePath().normalize());
          return null;
        });

    assertEquals(previousLogDirectory, System.getProperty("BASELOGDIR"));
    assertEquals(previousReflectionDirectory, System.getProperty("BASEREFLECTIONDIR"));
    assertEquals(previousTempDirectory, System.getProperty("java.io.tmpdir"));
    assertFalse(Files.exists(runtime.get()));
  }
}
