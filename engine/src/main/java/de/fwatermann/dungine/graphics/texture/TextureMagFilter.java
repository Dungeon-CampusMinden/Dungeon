package de.fwatermann.dungine.graphics.texture;

import org.lwjgl.opengl.GL33;

public enum TextureMagFilter {

  NEAREST(GL33.GL_NEAREST),
  LINEAR(GL33.GL_LINEAR);

  final int glValue;

  TextureMagFilter(int glValue) {
    this.glValue = glValue;
  }

}
