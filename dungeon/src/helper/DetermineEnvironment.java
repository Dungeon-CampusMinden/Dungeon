package helper;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public class DetermineEnvironment {
  private static DetermineEnvironment INST = null;

  public static boolean isStartedInJarFile() {
    return Objects.requireNonNull(
            DetermineEnvironment.class.getResource("DetermineEnvironment.class"))
        .toString()
        .startsWith("jar:");
  }

  public static File getNormalizedFileFromUrl(URL url) {
    return new File(URI.create(url.toExternalForm()).normalize());
  }

  public static DetermineEnvironment getInstance() {
    if (INST == null) {
      INST = new DetermineEnvironment();
    }
    return INST;
  }

  public File getFileToJarFile() {
    return new File(
        URI.create(getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm())
            .normalize());
  }
}
