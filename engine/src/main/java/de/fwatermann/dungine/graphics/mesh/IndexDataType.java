package de.fwatermann.dungine.graphics.mesh;

import org.lwjgl.opengl.GL33;

/**
 * Enum representing the index data types used in OpenGL.
 * Each index data type has an associated OpenGL type and size in bytes.
 */
public enum IndexDataType {

  /** Unsigned byte index data type. */
  UNSIGNED_BYTE(GL33.GL_UNSIGNED_BYTE, 1),

  /** Unsigned short index data type. */
  UNSIGNED_SHORT(GL33.GL_UNSIGNED_SHORT, 2),

  /** Unsigned integer index data type. */
  UNSIGNED_INT(GL33.GL_UNSIGNED_INT, 4);

  final int glType;
  final int bytes;

  /**
   * Constructs an IndexDataType with the specified OpenGL type and size in bytes.
   *
   * @param glType the OpenGL type representing the index data type
   * @param bytes the size of the index data type in bytes
   */
  IndexDataType(int glType, int bytes) {
    this.glType = glType;
    this.bytes = bytes;
  }

}
