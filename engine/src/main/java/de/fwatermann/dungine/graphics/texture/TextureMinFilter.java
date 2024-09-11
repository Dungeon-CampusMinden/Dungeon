package de.fwatermann.dungine.graphics.texture;

import org.lwjgl.opengl.GL33;

public enum TextureMinFilter {

  NEAREST(GL33.GL_NEAREST),
  LINEAR(GL33.GL_LINEAR),
  NEAREST_MIPMAP_NEAREST(GL33.GL_NEAREST_MIPMAP_NEAREST),
  LINEAR_MIPMAP_NEAREST(GL33.GL_LINEAR_MIPMAP_NEAREST),
  NEAREST_MIPMAP_LINEAR(GL33.GL_NEAREST_MIPMAP_LINEAR),
  LINEAR_MIPMAP_LINEAR(GL33.GL_LINEAR_MIPMAP_LINEAR);

  final int glValue;

  TextureMinFilter(int glValue) {
    this.glValue = glValue;
  }

}
