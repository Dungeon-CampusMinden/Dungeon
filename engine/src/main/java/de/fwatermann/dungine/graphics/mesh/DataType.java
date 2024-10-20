package de.fwatermann.dungine.graphics.mesh;

import org.lwjgl.opengl.GL33;

/**
 * Enum representing the data types used in OpenGL.
 * Each data type has an associated OpenGL type and size in bytes.
 */
public enum DataType {
  /** Signed byte data type. */
  BYTE(GL33.GL_BYTE, 1),

  /** Unsigned byte data type. */
  UNSIGNED_BYTE(GL33.GL_UNSIGNED_BYTE, 1),

  /** Signed short data type. */
  SHORT(GL33.GL_SHORT, 2),

  /** Unsigned short data type. */
  UNSIGNED_SHORT(GL33.GL_UNSIGNED_SHORT, 2),

  /** Signed integer data type. */
  INT(GL33.GL_INT, 4),

  /** Unsigned integer data type. */
  UNSIGNED_INT(GL33.GL_UNSIGNED_INT, 4),

  /** Floating point data type. */
  FLOAT(GL33.GL_FLOAT, 4),

  /** Double precision floating point data type. */
  DOUBLE(GL33.GL_DOUBLE, 8);

  final int glType;
  final int bytes;

  /**
   * Constructs a DataType with the specified OpenGL type and size in bytes.
   *
   * @param glType the OpenGL type representing the data type
   * @param bytes the size of the data type in bytes
   */
  DataType(int glType, int bytes) {
    this.glType = glType;
    this.bytes = bytes;
  }

  /**
   * Checks if the data type is an integer type.
   *
   * @return true if the data type is an integer type, false otherwise
   */
  public boolean isInteger() {
    return this == BYTE
        || this == UNSIGNED_BYTE
        || this == SHORT
        || this == UNSIGNED_SHORT
        || this == INT
        || this == UNSIGNED_INT;
  }
}
