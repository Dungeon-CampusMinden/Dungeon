package de.fwatermann.dungine.utils;

import java.nio.*;
import org.lwjgl.opengl.GL30;

public class GLUtils {

  /** Checks for OpenGL errors and throws a RuntimeException if an error occurred. */
  public static void checkGLError() {
    int error = GL30.glGetError();
    if (error != GL30.GL_NO_ERROR) {
      throw new RuntimeException("OpenGL error: " + error);
    }
  }

  /**
   * Checks if the OpenGL version is at least the specified version.
   *
   * @param minMajor the minimum major version
   * @param minMinor the minimum minor version
   * @return true if the OpenGL version is at least the specified version, false otherwise
   */
  public static boolean checkVersion(int minMajor, int minMinor) {
    int major = GL30.glGetInteger(GL30.GL_MAJOR_VERSION);
    int minor = GL30.glGetInteger(GL30.GL_MINOR_VERSION);
    return major > minMajor || (major == minMajor && minor >= minMinor);
  }

  /**
   * Checks if the provided FloatBuffer is direct and in native byte order.
   * Direct buffers are memory areas outside the normal garbage-collected heap.
   * Native byte order refers to the byte order used by the underlying hardware,
   * which can be either big-endian or little-endian.
   *
   * @param buffer The FloatBuffer to check.
   * @throws IllegalArgumentException if the buffer is not direct or not in native byte order.
   */
  public static void checkBuffer(FloatBuffer buffer) {
    if(buffer == null) return;
    if(!buffer.isDirect()) {
      throw new IllegalArgumentException("Buffer must be direct!");
    }
    if(buffer.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("Buffer must be in native byte order!");
    }
  }

  /**
   * Checks if the provided IntBuffer is direct and in native byte order.
   *
   * @param buffer The IntBuffer to check.
   * @throws IllegalArgumentException if the buffer is not direct or not in native byte order.
   */
  public static void checkBuffer(IntBuffer buffer) {
    if(buffer == null) return;
    if(!buffer.isDirect()) {
      throw new IllegalArgumentException("Buffer must be direct!");
    }
    if(buffer.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("Buffer must be in native byte order!");
    }
  }

  /**
   * Checks if the provided ShortBuffer is direct and in native byte order.
   *
   * @param buffer The ShortBuffer to check.
   * @throws IllegalArgumentException if the buffer is not direct or not in native byte order.
   */
  public static void checkBuffer(ShortBuffer buffer) {
    if(buffer == null) return;
    if(!buffer.isDirect()) {
      throw new IllegalArgumentException("Buffer must be direct!");
    }
    if(buffer.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("Buffer must be in native byte order!");
    }
  }

  /**
   * Checks if the provided ByteBuffer is direct and in native byte order.
   *
   * @param buffer The ByteBuffer to check.
   * @throws IllegalArgumentException if the buffer is not direct or not in native byte order.
   */
  public static void checkBuffer(ByteBuffer buffer) {
    if(buffer == null) return;
    if(!buffer.isDirect()) {
      throw new IllegalArgumentException("Buffer must be direct!");
    }
    if(buffer.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("Buffer must be in native byte order!");
    }
  }

  /**
   * Checks if the provided LongBuffer is direct and in native byte order.
   *
   * @param buffer The LongBuffer to check.
   * @throws IllegalArgumentException if the buffer is not direct or not in native byte order.
   */
  public static void checkBuffer(LongBuffer buffer) {
    if(buffer == null) return;
    if(!buffer.isDirect()) {
      throw new IllegalArgumentException("Buffer must be direct!");
    }
    if(buffer.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("Buffer must be in native byte order!");
    }
  }

  /**
   * Checks if the provided DoubleBuffer is direct and in native byte order.
   *
   * @param buffer The DoubleBuffer to check.
   * @throws IllegalArgumentException if the buffer is not direct or not in native byte order.
   */
  public static void checkBuffer(DoubleBuffer buffer) {
    if(buffer == null) return;
    if(!buffer.isDirect()) {
      throw new IllegalArgumentException("Buffer must be direct!");
    }
    if(buffer.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("Buffer must be in native byte order!");
    }
  }

  /**
   * Checks if the provided CharBuffer is direct and in native byte order.
   *
   * @param buffer The CharBuffer to check.
   * @throws IllegalArgumentException if the buffer is not direct or not in native byte order.
   */
  public static void checkBuffer(CharBuffer buffer) {
    if(buffer == null) return;
    if(!buffer.isDirect()) {
      throw new IllegalArgumentException("Buffer must be direct!");
    }
    if(buffer.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException("Buffer must be in native byte order!");
    }
  }


}