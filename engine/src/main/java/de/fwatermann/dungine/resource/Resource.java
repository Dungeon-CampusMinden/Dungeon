package de.fwatermann.dungine.resource;

import de.fwatermann.dungine.utils.Disposable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class Resource implements Disposable {

  private static final Map<String, Resource> cache = new HashMap<>();

  /**
   * Loads a resource from the specified path. If the resource is not found on the file system, it
   * will be loaded from the classpath.
   *
   * @param path the path to the resource
   * @return the resource
   */
  public static Resource load(String path) {
    return cache.computeIfAbsent(
        path,
        k -> {
          FileSystem fs = FileSystems.getDefault();
          Path p = fs.getPath(k);
          if (Files.exists(p)) {
            return new FileResource(p);
          } else {
            return new ClasspathResource(k);
          }
        });
  }

  /**
   * Reads the resource as a byte buffer. If the bytes are no longer required the
   * resource should be deallocated using the {@link #deallocate()} method. This method
   * can be used to reallocate the resource if needed.
   *
   * @return the resource as a byte buffer
   * @throws IOException if an I/O error occurs
   */
  public abstract ByteBuffer readBytes() throws IOException;

  /**
   * Deallocates the resource, freeing any resources. This method should be called when the resource
   * is no longer needed. Or only used as reference.
   */
  public abstract void deallocate();

  /**
   * Returns the size of the resource in bytes.
   * @return the size of the resource in bytes
   * @throws IOException if an I/O error occurs
   */
  public abstract long size() throws IOException;
}
