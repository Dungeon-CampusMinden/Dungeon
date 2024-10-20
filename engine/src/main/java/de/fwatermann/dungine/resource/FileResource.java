package de.fwatermann.dungine.resource;

import de.fwatermann.dungine.utils.annotations.NotNull;
import de.fwatermann.dungine.utils.annotations.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import org.lwjgl.BufferUtils;

/**
 * The `FileResource` class represents a resource that is loaded from a file. It provides methods to
 * read the file's contents into a byte buffer and to resolve relative paths.
 */
public class FileResource extends Resource {

  private final Path path;
  private ByteBuffer buffer;
  private long size = -1;

  /**
   * Constructs a new `FileResource` with the specified file path.
   *
   * @param path the path to the file
   */
  protected FileResource(@NotNull Path path) {
    this.path = path;
  }

  /**
   * Reads the file's contents into a byte buffer. This method is called internally when the buffer
   * is accessed for the first time.
   */
  private void read() {
    try {
      byte[] bytes = Files.readAllBytes(this.path);
      this.buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
      this.buffer.put(bytes);
      this.buffer.flip();
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file: " + this.path, e);
    }
  }

  /**
   * Resolves a relative path against the file's path.
   *
   * @param path the relative path to resolve
   * @return the resolved resource, or null if the resource could not be loaded
   */
  @Override
  @Nullable
  public Resource resolveRelative(String path) {
    return load(this.path.resolve(path).normalize().toString(), 0x01);
  }

  /**
   * Reads the entire file's contents into a read-only byte buffer.
   *
   * @return a read-only byte buffer containing the file's contents
   */
  @Override
  public ByteBuffer readBytes() {
    if (this.buffer == null) {
      this.read();
    }
    return this.buffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
  }

  /**
   * Reads a portion of the file's contents into a read-only byte buffer.
   *
   * @param offset the offset to start reading from
   * @param count the number of bytes to read
   * @return a read-only byte buffer containing the specified portion of the file's contents
   */
  @Override
  public ByteBuffer readBytes(int offset, int count) {
    if (this.buffer != null) {
      return this.buffer.slice(offset, count).asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }
    ByteBuffer buffer = BufferUtils.createByteBuffer(count);
    try {
      Files.newByteChannel(this.path).position(offset).read(buffer);
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to read part of file: "
              + this.path
              + " [s: "
              + offset
              + " e:"
              + (offset + count)
              + "]",
          e);
    }
    return buffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
  }

  /**
   * Returns the size of the file.
   *
   * @return the size of the file
   * @throws IOException if an I/O error occurs
   */
  @Override
  public long size() throws IOException {
    if (this.size == -1) {
      this.size = Files.size(this.path);
    }
    return this.size;
  }

  /** Deallocates the buffer, allowing it to be garbage collected. */
  @Override
  public void deallocate() {
    this.buffer = null;
  }

  /** Disposes of the resource, deallocating the buffer. */
  @Override
  public void dispose() {
    this.buffer = null;
  }

  /**
   * Returns a string representation of the `FileResource`.
   *
   * @return a string representation of the `FileResource`
   */
  @Override
  public String toString() {
    if (this.buffer != null) {
      return String.format("FileResource[path=%s, bytes: %d]", this.path, this.buffer.capacity());
    } else {
      return String.format("FileResource[path=%s, bytes: n/a]", this.path);
    }
  }

  /**
   * Checks if this `FileResource` is equal to another object.
   *
   * @param other the object to compare to
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }
    FileResource that = (FileResource) other;
    return this.path.equals(that.path);
  }

  /**
   * Returns the hash code of this `FileResource`.
   *
   * @return the hash code of this `FileResource`
   */
  @Override
  public int hashCode() {
    return this.path.hashCode();
  }
}
