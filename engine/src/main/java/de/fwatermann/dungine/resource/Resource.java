package de.fwatermann.dungine.resource;

import de.fwatermann.dungine.utils.Disposable;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Resource implements Disposable {

  private static final Logger LOGGER = LogManager.getLogger(Resource.class);

  private static final ReferenceQueue<Resource> refQueue = new ReferenceQueue<>();
  private static final Map<String, SoftReference<Resource>> cache = new HashMap<>();

  /**
   * Loads a resource from the specified path. If the resource is not found on the file system, it
   * will be loaded from the classpath.
   *
   * @param path the path to the resource
   * @return the resource
   */
  public static Resource load(String path) {
    SoftReference<Resource> ref = cache.get(path);
    if (ref != null && ref.get() != null) {
      return ref.get();
    }
    Resource res = null;
    FileSystem fs = FileSystems.getDefault();
    Path p = fs.getPath(path);
    if (Files.exists(p)) {
      res = new FileResource(p);
    } else {
      res = new ClasspathResource(path);
    }
    ref = new SoftReference<>(res, refQueue);
    cache.put(path, ref);
    return res;
  }

  public static void collectGarbage() {
    Reference<? extends Resource> ref;
    while ((ref = refQueue.poll()) != null) {
      Resource res = ref.get();
      if (res != null) {
        res.dispose();
      }
    }
    cache.entrySet().removeIf(e -> {
      if (e.getValue().get() == null) {
        LOGGER.trace("Collected unused resource: {}", e.getKey());
        return true;
      }
      return false;
    });
  }

  /**
   * Reads the resource as a byte buffer. If the bytes are no longer required the resource should be
   * deallocated using the {@link #deallocate()} method. This method can be used to reallocate the
   * resource if needed.
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
   *
   * @return the size of the resource in bytes
   * @throws IOException if an I/O error occurs
   */
  public abstract long size() throws IOException;

  /**
   * Returns a string representation of the resource.
   *
   * @return the string representation of the resource
   */
  public abstract String toString();

  /**
   * Check if the resource is equal to another object.
   *
   * @param other the object to compare
   * @return true if the resource is equal to the other object
   */
  public abstract boolean equals(Object other);

  /**
   * Returns the hash code of the resource.
   *
   * @return the hash code of the resource
   */
  public abstract int hashCode();
}
