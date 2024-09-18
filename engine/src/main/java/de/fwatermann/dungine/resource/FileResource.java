package de.fwatermann.dungine.resource;

import de.fwatermann.dungine.utils.annotations.NotNull;
import de.fwatermann.dungine.utils.annotations.Nullable;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResource extends Resource {

  private final Path path;
  private ByteBuffer buffer;
  private long size = -1;

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
  @Nullable
  public Resource resolveRelative(String path) {
    return load(this.path.resolve(path).normalize().toString(), 0x01);
  }

  @Override
  public ByteBuffer readBytes() {
    if (this.buffer == null) {
      this.read();
    }
    return this.buffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
  }

  @Override
  public ByteBuffer readBytes(int offset, int count) {
    if(this.buffer != null) {
      return this.buffer.slice(offset, count).asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }
    ByteBuffer buffer = BufferUtils.createByteBuffer(count);
    try {
      Files.newByteChannel(this.path).position(offset).read(buffer);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read part of file: " + this.path + " [s: " + offset + " e:" + (offset + count) + "]", e);
    }
    return buffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
  }

  @Override
  public long size() throws IOException {
    if(this.size == -1) {
      this.size = Files.size(this.path);
    }
    return this.size;
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
