package de.fwatermann.dungine.resource;

import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.annotations.Nullable;
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

/**
 * The `Resource` class represents an abstract resource that can be loaded from the file system or classpath.
 * It provides methods to load, read, and manage resources, including caching and garbage collection.
 */
public abstract class Resource implements Disposable {

  /** Logger for the `Resource` class. */
  private static final Logger LOGGER = LogManager.getLogger(Resource.class);

  /** Reference queue for garbage collection of resources. */
  private static final ReferenceQueue<Resource> refQueue = new ReferenceQueue<>();

  /** Cache for loaded resources. */
  private static final Map<String, SoftReference<Resource>> cache = new HashMap<>();

  /**
   * Constructs a new `Resource`.
   */
  protected Resource() {}

  /**
   * Loads a resource from the specified path. If the resource is not found on the file system, it
   * will be loaded from the classpath.
   *
   * @param path the path to the resource
   * @return the resource
   */
  public static Resource load(String path) {
    return load(path, 0x00);
  }

  /**
   * Loads a resource from the specified path. If the resource is not found on the file system, it
   * will be loaded from the classpath.
   *
   * <p>If the forceType is 0x01, the resource will be loaded from the file system. If the forceType
   * is 0x02, the resource will be loaded from the classpath. If the forceType is 0x00, the resource
   * will be loaded from the file system if it exists, otherwise from the classpath.
   *
   * @param path the path to the resource
   * @param forceType the type of resource to load
   * @return the resource
   */
  protected static Resource load(String path, int forceType) {
    SoftReference<Resource> ref = cache.get(path);
    if (ref != null && ref.get() != null) {
      return ref.get();
    }
    Resource res = null;
    FileSystem fs = FileSystems.getDefault();
    Path p = fs.getPath(path);

    if (forceType == 0x00) {
      if (Files.exists(p)) {
        res = new FileResource(p);
      } else if (Resource.class.getResourceAsStream(path) != null) {
        res = new ClasspathResource(path);
      } else {
        LOGGER.warn("Resource not found: {}", path);
        return null;
      }
    } else {
      if (forceType == 0x01) { // force FileResource
        if (Files.exists(p)) {
          res = new FileResource(p);
        } else {
          return null;
        }
      } else if (forceType == 0x02) { // force ClasspathResource
        if (Resource.class.getResourceAsStream(path) != null) {
          res = new ClasspathResource(path);
        } else {
          return null;
        }
      } else {
        throw new IllegalArgumentException("Invalid forceType: " + forceType);
      }
    }

    ref = new SoftReference<>(res, refQueue);
    cache.put(path, ref);
    return res;
  }

  /**
   * Collects garbage by disposing of unused resources and removing them from the cache.
   */
  public static void collectGarbage() {
    Reference<? extends Resource> ref;
    while ((ref = refQueue.poll()) != null) {
      Resource res = ref.get();
      if (res != null) {
        res.dispose();
      }
    }
    cache
        .entrySet()
        .removeIf(
            e -> {
              if (e.getValue().get() == null) {
                LOGGER.trace("Collected unused resource: {}", e.getKey());
                return true;
              }
              return false;
            });
  }

  /**
   * Resolves a relative path to a resource.
   *
   * @param path the relative path
   * @return the resource
   */
  @Nullable
  public abstract Resource resolveRelative(String path);

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
   * Reads part of the resource as byte buffer. The result of this reading will not be cached!
   * @param offset the offset in bytes
   * @param count the number of bytes to read
   * @return the resource as a byte buffer
   */
  public abstract ByteBuffer readBytes(int offset, int count);

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
