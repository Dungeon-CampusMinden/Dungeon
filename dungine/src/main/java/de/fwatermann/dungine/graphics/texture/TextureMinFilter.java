package de.fwatermann.dungine.graphics.texture;

import org.lwjgl.opengl.GL33;

/**
 * Enum representing the texture minification filters used in OpenGL. Each filter type has an
 * associated OpenGL constant.
 */
public enum TextureMinFilter {

  /** Nearest neighbor filtering. */
  NEAREST(GL33.GL_NEAREST),

  /** Linear filtering. */
  LINEAR(GL33.GL_LINEAR),

  /** Nearest neighbor filtering with nearest mipmap selection. */
  NEAREST_MIPMAP_NEAREST(GL33.GL_NEAREST_MIPMAP_NEAREST),

  /** Linear filtering with nearest mipmap selection. */
  LINEAR_MIPMAP_NEAREST(GL33.GL_LINEAR_MIPMAP_NEAREST),

  /** Nearest neighbor filtering with linear mipmap interpolation. */
  NEAREST_MIPMAP_LINEAR(GL33.GL_NEAREST_MIPMAP_LINEAR),

  /** Linear filtering with linear mipmap interpolation. */
  LINEAR_MIPMAP_LINEAR(GL33.GL_LINEAR_MIPMAP_LINEAR);

  final int glValue;

  /**
   * Constructs a TextureMinFilter with the specified OpenGL constant.
   *
   * @param glValue the OpenGL constant representing the filter type
   */
  TextureMinFilter(int glValue) {
    this.glValue = glValue;
  }
}
