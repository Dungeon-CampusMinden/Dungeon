package de.fwatermann.dungine.graphics.mesh;

import org.lwjgl.opengl.GL33;

public enum IndexDataType {

  UNSIGNED_BYTE(GL33.GL_UNSIGNED_BYTE, 1),
  UNSIGNED_SHORT(GL33.GL_UNSIGNED_SHORT, 2),
  UNSIGNED_INT(GL33.GL_UNSIGNED_INT, 4);

  final int glType;
  final int bytes;

  IndexDataType(int glType, int bytes) {
    this.glType = glType;
    this.bytes = bytes;
  }

}
