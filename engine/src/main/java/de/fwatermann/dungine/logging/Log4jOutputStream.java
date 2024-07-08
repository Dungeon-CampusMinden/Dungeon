package de.fwatermann.dungine.logging;

import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Log4jOutputStream extends OutputStream {

  private Level level;

  public Log4jOutputStream(Level logLevel) {
    super();
    this.level = logLevel;
  }

  private Class<?> getCaller() {
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    StackTraceElement caller = null;
    for (int i = 0; i < stack.length; i++) {
      if (!stack[i].getClassName().toLowerCase().startsWith("java")
          && !stack[i].getClassName().toLowerCase().startsWith("sun")
          && !stack[i].getClassName().equalsIgnoreCase(this.getClass().getName())) {
        caller = stack[i];
        break;
      }
    }
    if (caller == null) {
      return null;
    }
    try {
      return Class.forName(caller.getClassName());
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  ByteBuffer buffer = ByteBuffer.allocate(1024);

  @Override
  public void write(int b) throws IOException {
    if (b == 0) return;
    if (this.buffer.position() == this.buffer.capacity() - 1) {
      ByteBuffer newBuffer = ByteBuffer.allocate(this.buffer.capacity() + 1024);
      this.buffer.flip();
      newBuffer.put(this.buffer);
      this.buffer = newBuffer;
    }
    if (b == '\n') {
      this.flush();
      return;
    }
    this.buffer.put((byte) b);
  }

  @Override
  public void flush() throws IOException {
    if (this.buffer.position() == 0) return;
    this.buffer.flip();
    byte[] bytes = new byte[this.buffer.remaining()];
    this.buffer.get(bytes);
    this.buffer.clear();
    String message = new String(bytes);
    Class<?> caller = this.getCaller();
    if (caller != null) {
      org.apache.logging.log4j.LogManager.getLogger(caller).log(this.level, message);
    } else {
      org.apache.logging.log4j.LogManager.getLogger("Default").log(this.level, message);
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    super.write(b, off, len);
  }

  @Override
  public void write(byte[] b) throws IOException {
    super.write(b);
  }

  @Override
  public void close() throws IOException {
    this.flush();
    super.close();
  }
}
