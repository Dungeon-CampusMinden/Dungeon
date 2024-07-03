package de.fwatermann.dungine.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ClasspathResource extends Resource {

  private final String path;
  private ByteBuffer buffer;

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
   * Get the size of the resource. If the resource is not read yet, the size is unknown and -1 is
   * returned.
   */
  @Override
  public long size() throws IOException {
    return this.buffer != null ? this.buffer.capacity() : -1;
  }

  @Override
  public ByteBuffer readBytes() throws IOException {
    if (this.buffer == null) {
      this.read();
    }
    return this.buffer.asReadOnlyBuffer();
  }

  @Override
  public void dispose() {
    this.buffer = null;
  }
}
