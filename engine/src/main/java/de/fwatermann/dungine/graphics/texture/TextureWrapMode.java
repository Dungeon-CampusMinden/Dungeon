package de.fwatermann.dungine.graphics.texture;

import org.lwjgl.opengl.GL33;

public enum TextureWrapMode {
  CLAMP_TO_EDGE(GL33.GL_CLAMP_TO_EDGE),
  CLAMP_TO_BORDER(GL33.GL_CLAMP_TO_BORDER),
  MIRRORED_REPEAT(GL33.GL_MIRRORED_REPEAT),
  REPEAT(GL33.GL_REPEAT);

  final int glValue;

  TextureWrapMode(int glValue) {
    this.glValue = glValue;
  }
}
