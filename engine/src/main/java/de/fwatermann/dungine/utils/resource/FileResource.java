package de.fwatermann.dungine.utils.resource;

import de.fwatermann.dungine.utils.annotations.NotNull;
import java.io.IOException;
import java.nio.ByteBuffer;
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
      this.buffer = ByteBuffer.allocateDirect(bytes.length);
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
    return this.buffer.asReadOnlyBuffer();
  }

  @Override
  public long size() throws IOException {
    return this.buffer != null ? this.buffer.capacity() : Files.size(this.path);
  }

  @Override
  public void dispose() {
    this.buffer = null;
  }
}
