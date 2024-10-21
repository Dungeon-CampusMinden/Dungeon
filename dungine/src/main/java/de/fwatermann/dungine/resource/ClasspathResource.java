package de.fwatermann.dungine.resource;

import de.fwatermann.dungine.utils.annotations.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;

/**
 * The `ClasspathResource` class represents a resource that is loaded from the classpath. It
 * provides methods to read the resource as a byte buffer, resolve relative paths, and get the size
 * of the resource.
 *
 * <p>This class extends the `Resource` class and overrides its methods to provide specific
 * functionality for classpath resources.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * ClasspathResource resource = new ClasspathResource("/path/to/resource");
 * ByteBuffer buffer = resource.readBytes();
 * }</pre>
 */
public class ClasspathResource extends Resource {

  private static final Logger LOGGER = LogManager.getLogger(ClasspathResource.class);

  private final String path;
  private ByteBuffer buffer;
  private long size = -1;

  /**
   * Constructs a new ClasspathResource instance with the specified path.
   *
   * @param path the path to the resource
   */
  protected ClasspathResource(String path) {
    this.path = path;
  }

  private void read() {
    try {
      InputStream is = ClasspathResource.class.getResourceAsStream(this.path);
      if (is == null) {
        throw new RuntimeException("Resource not found: " + this.path);
      }
      byte[] bytes = is.readAllBytes();
      this.buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
      this.buffer.put(bytes);
      this.buffer.flip();

      is.close();
    } catch (IOException e) {
      throw new RuntimeException("Failed to read resource: " + this.path, e);
    }
  }

  /**
   * Resolve a relative path to a resource. The path is relative to the current resource.
   *
   * @param path the relative path
   * @return the resource or null if the resource does not exist
   */
  @Override
  @Nullable
  public Resource resolveRelative(String path) {
    LOGGER.debug("Resolving relative path: {} ({})", path, this.path);
    if (path.startsWith("/")) {
      return load(path, 0x02);
    } else {
      String nPath = Path.of(this.path).getParent().resolve(path).normalize().toString();
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
        nPath = nPath.replaceAll("\\\\", "/");
      }
      return load(nPath, 0x02);
    }
  }

  /**
   * Get the size of the resource. If the resource is not read yet, the size is unknown and -1 is
   * returned.
   */
  @Override
  public long size() throws IOException {
    if (this.size == -1) {
      this.size = ClassLoader.getSystemResource(this.path).openConnection().getContentLengthLong();
    }
    return this.size;
  }

  @Override
  public ByteBuffer readBytes() throws IOException {
    if (this.buffer == null) {
      this.read();
    }
    return this.buffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
  }

  @Override
  public ByteBuffer readBytes(int offset, int count) {
    if (this.buffer != null) {
      return this.buffer.slice(offset, count).asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }
    try {
      InputStream is = ClasspathResource.class.getResourceAsStream(this.path);
      if (is == null) throw new RuntimeException("Resource not found: " + this.path);
      is.skipNBytes(offset);

      byte[] bytes = is.readNBytes(count);
      ByteBuffer buffer = BufferUtils.createByteBuffer(count);
      buffer.put(bytes);
      buffer.flip();

      is.close();
      return buffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    } catch (IOException ex) {
      throw new RuntimeException(
          "Failed to read part of resource: "
              + this.path
              + " [s: "
              + offset
              + " e:"
              + (offset + count)
              + "]",
          ex);
    }
  }

  @Override
  public void deallocate() {
    this.buffer = null;
  }

  @Override
  public void dispose() {
    this.buffer = null;
  }

  @Override
  public String toString() {
    if (this.buffer != null) {
      return String.format(
          "ClasspathResource[path=%s, bytes: %d]", this.path, this.buffer.capacity());
    } else {
      return String.format("ClasspathResource[path=%s, bytes: n/a]", this.path);
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }
    ClasspathResource that = (ClasspathResource) other;
    return this.path.equals(that.path);
  }

  @Override
  public int hashCode() {
    return this.path.hashCode();
  }
}
