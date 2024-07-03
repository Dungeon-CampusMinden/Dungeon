package de.fwatermann.dungine.graphics;

import org.lwjgl.opengl.GL33;

/** An enumeration of OpenGL usage hints for buffers. */
public enum GLUsageHint {

  /**
   * The buffer will be used for rendering static objects. Meaning that the buffer will not be
   * updated frequently.
   *
   * <p><code>DRAW_*</code> usage hints will indicate that the buffer will only be written to from
   * the application.
   */
  DRAW_STATIC,

  /**
   * Theb buffer will be used for rendering dynamic objects. Meaning that the buffer will be updated
   * frequently.
   *
   * <p><code>DRAW_*</code> usage hints will indicate that the buffer will only be written to from
   * the application.
   */
  DRAW_DYNAMIC,

  /**
   * The buffer will be used for rendering a stream of data. Meaning that the buffer will be updated
   * every frame.
   *
   * <p><code>DRAW_*</code> usage hints will indicate that the buffer will only be written to from
   * the application.
   */
  DRAW_STREAM,

  /**
   * The buffer will be used for reading static data. Meaning that the buffer will not be updated
   * frequently.
   *
   * <p><code>READ_*</code> usage hints will indicate that the buffer will only be read from by the
   * application.
   */
  READ_STATIC,

  /**
   * The buffer will be used for reading dynamic data. Meaning that the buffer will be updated
   * frequently.
   *
   * <p><code>READ_*</code> usage hints will indicate that the buffer will only be read from by the
   * application.
   */
  READ_DYNAMIC,

  /**
   * The buffer will be used for reading a stream of data. Meaning that the buffer will be updated
   * every frame.
   *
   * <p><code>READ_*</code> usage hints will indicate that the buffer will only be read from by the
   * application.
   */
  READ_STREAM,

  /**
   * The buffer will be used for reading and writing static data. Meaning that the buffer will not
   * be updated frequently.
   *
   * <p><code>COPY_*</code> usage hints will indicate that the buffer will neither be read from and
   * written to by the application.
   */
  COPY_STATIC,

  /**
   * The buffer will be used for reading and writing dynamic data. Meaning that the buffer will be
   * updated frequently.
   *
   * <p><code>COPY_*</code> usage hints will indicate that the buffer will neither be read from and
   * written to by the application.
   */
  COPY_DYNAMIC,

  /**
   * The buffer will be used for reading and writing a stream of data. Meaning that the buffer will
   * be updated every frame.
   *
   * <p><code>COPY_*</code> usage hints will indicate that the buffer will neither be read from and
   * written to by the application.
   */
  COPY_STREAM;

  /**
   * Returns the OpenGL constant for this usage hint.
   *
   * @return the OpenGL constant for this usage hint
   */
  public int getGLConstant() {
    switch (this) {
      case DRAW_STATIC:
        return GL33.GL_STATIC_DRAW;
      case DRAW_DYNAMIC:
        return GL33.GL_DYNAMIC_DRAW;
      case DRAW_STREAM:
        return GL33.GL_STREAM_DRAW;
      case READ_STATIC:
        return GL33.GL_STATIC_READ;
      case READ_DYNAMIC:
        return GL33.GL_DYNAMIC_READ;
      case READ_STREAM:
        return GL33.GL_STREAM_READ;
      case COPY_STATIC:
        return GL33.GL_STATIC_COPY;
      case COPY_DYNAMIC:
        return GL33.GL_DYNAMIC_COPY;
      case COPY_STREAM:
        return GL33.GL_STREAM_COPY;
      default:
        return -1;
    }
  }
}
