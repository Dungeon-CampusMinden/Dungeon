package wizard.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Materializes the project embedded in a player JAR into one disposable runtime. */
final class EmbeddedProjectMaterializer {
  private static final String RESOURCE_PREFIX = "wizard/embedded-project/";
  private static final String FILE_INDEX = RESOURCE_PREFIX + "files.list";

  private EmbeddedProjectMaterializer() {}

  static Path materialize(final Path runtimeDirectory) {
    Path project = runtimeDirectory.toAbsolutePath().normalize().resolve("embedded-project");
    try {
      Files.createDirectory(project);
      try (var index =
          new BufferedReader(new InputStreamReader(resource(FILE_INDEX), StandardCharsets.UTF_8))) {
        String entry;
        while ((entry = index.readLine()) != null) {
          copyResource(project, entry);
        }
      }
      return project;
    } catch (IOException exception) {
      throw new IllegalStateException("Cannot materialize embedded project", exception);
    }
  }

  private static void copyResource(final Path project, final String entry) throws IOException {
    Path destination = project.resolve(entry).normalize();
    if (!destination.startsWith(project) || destination.equals(project)) {
      throw new IllegalStateException("Embedded project path escapes its destination: " + entry);
    }
    Files.createDirectories(destination.getParent());
    try (InputStream input = resource(RESOURCE_PREFIX + entry)) {
      Files.copy(input, destination);
    }
  }

  private static InputStream resource(final String name) {
    InputStream input =
        EmbeddedProjectMaterializer.class.getClassLoader().getResourceAsStream(name);
    if (input == null) {
      throw new IllegalStateException("Missing embedded project resource: " + name);
    }
    return input;
  }
}
