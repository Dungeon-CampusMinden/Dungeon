package core.utils.components.path;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;

/**
 * Datatype that is used for all path apis in the dungeon framework.
 *
 * @see core.components.DrawComponent
 */
public interface IPath {
  default String convertPath(String path) {
    URI uri =
        URI.create(getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm())
            .normalize();
    boolean isInJar = uri.getScheme().equals("jar");
    if (isInJar) {
      // inside JAR
      try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
        path = fileSystem.getPath(path).toString();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return path;
  }

  String pathString();

  int priority();
}
