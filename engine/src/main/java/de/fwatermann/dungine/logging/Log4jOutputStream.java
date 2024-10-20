package de.fwatermann.dungine.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.Level;

/**
 * The `Log4jOutputStream` class extends `OutputStream` to redirect output to Log4j. It buffers the
 * output and logs it at the specified log level.
 */
public class Log4jOutputStream extends OutputStream {

  /** The log level to use for logging messages. */
  private final Level level;

  /**
   * Constructs a new `Log4jOutputStream` with the specified log level.
   *
   * @param logLevel the log level to use for logging messages
   */
  public Log4jOutputStream(Level logLevel) {
    super();
    this.level = logLevel;
  }

  /**
   * Gets the caller class from the stack trace.
   *
   * @return the caller class, or null if not found
   */
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

  /** The buffer to hold the output data. */
  ByteBuffer buffer = ByteBuffer.allocate(1024);

  /**
   * Writes a byte to the output stream.
   *
   * @param b the byte to write
   * @throws IOException if an I/O error occurs
   */
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

  /**
   * Flushes the output stream and logs the buffered data.
   *
   * @throws IOException if an I/O error occurs
   */
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

  /**
   * Writes a portion of an array of bytes to the output stream.
   *
   * @param b the data
   * @param off the start offset in the data
   * @param len the number of bytes to write
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    super.write(b, off, len);
  }

  /**
   * Writes an array of bytes to the output stream.
   *
   * @param b the data to write
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void write(byte[] b) throws IOException {
    super.write(b);
  }

  /**
   * Closes the output stream and flushes any buffered data.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    this.flush();
    super.close();
  }
}
