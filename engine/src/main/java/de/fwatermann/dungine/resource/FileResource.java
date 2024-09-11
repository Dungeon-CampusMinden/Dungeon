package de.fwatermann.dungine.resource;

import de.fwatermann.dungine.utils.annotations.NotNull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResource extends Resource {

  private final Path path;
  private ByteBuffer buffer;

  protected FileResource(@NotNull Path path) {
    this.path = path;
  }

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

  @Override
  public ByteBuffer readBytes() {
    if (this.buffer == null) {
      this.read();
    }
    return this.buffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
  }

  @Override
  public long size() throws IOException {
    return this.buffer != null ? this.buffer.capacity() : Files.size(this.path);
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
    if(this.buffer != null) {
      return String.format("FileResource[path=%s, bytes: %d]", this.path, this.buffer.capacity());
    } else {
      return String.format("FileResource[path=%s, bytes: n/a]", this.path);
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
    FileResource that = (FileResource) other;
    return this.path.equals(that.path);
  }

  @Override
  public int hashCode() {
    return this.path.hashCode();
  }
}
