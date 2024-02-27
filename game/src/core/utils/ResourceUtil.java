package core.utils;

import core.utils.logging.CustomLogLevel;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;

public final class ResourceUtil {
  private static final Logger LOGGER = Logger.getLogger(ResourceUtil.class.getSimpleName());

  /**
   * Open the resource given as parameter as Path.
   *
   * <p>Use a new FileSystem if inside JAR, or just a Path otherwise.
   *
   * <p>Throws an exception if no path could be found for the requested resource. Not intended for
   * use in streams.
   *
   * @param resource in assets folder, can be a specific file or a folder
   * @return The actual Path object for the resource.
   * @throws URISyntaxException if URI for resource is malformed
   * @throws IOException if the attempt to open a file system for the JAR is not successful
   */
  public static Path pathOf(String resource) throws URISyntaxException, IOException {
    Path pathToResource;

    /*
     1. get URI of resource relative to current process (thread)

     we can't use a given class here - the resource would be resolved relative to this class, i.e.
     relative to a given (sub-) project:
     - e.g. using Crafting.class.getResource(resource).toURI() would resolve relative to
     subproject "dungeon"
     - e.g. using DrawComponent.class.getResource(resource).toURI() would resolve relative to
     subproject "game"
    */
    URI uri = Thread.currentThread().getContextClassLoader().getResource(resource).toURI();

    /*
      2. get Path to resource
      - in local file system just use the URI
      - in JAR we need to create a new FileSystem on top of the URI
    */
    if (uri.getScheme().equals("jar")) {
      FileSystem fs;
      try {
        fs = FileSystems.getFileSystem(uri);
      } catch (FileSystemNotFoundException fsnfe) {
        fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
      }
      pathToResource = fs.getPath(resource); // absolute to/in JAR, relative to resources root
    } else {
      pathToResource = Paths.get(uri); // absolute to local file system
    }

    return pathToResource;
  }

  /**
   * Open an InputStream on Path p
   *
   * <p>Cases: (a) path is absolute: use the local file system, (b) path is relative: resolve in
   * resources of current process
   *
   * <p>Intended for use in streams - returns an Optional.empty() in the event of an error, e.g. if
   * no InputStream could be created for the Path.
   *
   * @param p Path to open
   * @return new Optional<InputStream> for Path p
   */
  public static Optional<InputStream> newInputStream(Path p) {
    InputStream stream = null;

    if (p.isAbsolute()) {
      // looks like we are operating from local file system (path is absolute)
      try {
        stream = Files.newInputStream(p);
      } catch (IOException e) {
        LOGGER.log(CustomLogLevel.ERROR, "Error opening path '" + p + "' as InputStream");
      }
    } else {
      // path is relative, so try to load it via resources folder of the current process
      stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(p.toString());
    }

    return Optional.ofNullable(stream);
  }
}
