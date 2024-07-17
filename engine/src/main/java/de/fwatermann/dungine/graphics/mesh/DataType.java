package de.fwatermann.dungine.graphics.mesh;

import org.lwjgl.opengl.GL33;

public enum DataType {
  BYTE(GL33.GL_BYTE, 1),
  UNSIGNED_BYTE(GL33.GL_UNSIGNED_BYTE, 1),
  SHORT(GL33.GL_SHORT, 2),
  UNSIGNED_SHORT(GL33.GL_UNSIGNED_SHORT, 2),
  INT(GL33.GL_INT, 4),
  UNSIGNED_INT(GL33.GL_UNSIGNED_INT, 4),
  FLOAT(GL33.GL_FLOAT, 4),
  DOUBLE(GL33.GL_DOUBLE, 8);

  final int glType;
  final int bytes;

  DataType(int glType, int bytes) {
    this.glType = glType;
    this.bytes = bytes;
  }

  public boolean isInteger() {
    return this == BYTE
        || this == UNSIGNED_BYTE
        || this == SHORT
        || this == UNSIGNED_SHORT
        || this == INT
        || this == UNSIGNED_INT;
  }
}
