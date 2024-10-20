package de.fwatermann.dungine.graphics.texture;

import org.lwjgl.opengl.GL33;

/**
 * Enum representing the magnification filter options for textures in OpenGL.
 * Provides two options: NEAREST and LINEAR.
 */
public enum TextureMagFilter {

  /**
   * Nearest neighbor filtering.
   * This filter selects the nearest pixel's value.
   */
  NEAREST(GL33.GL_NEAREST),

  /**
   * Linear filtering.
   * This filter selects the weighted average of the four nearest pixels.
   */
  LINEAR(GL33.GL_LINEAR);

  final int glValue;

  /**
   * Constructs a TextureMagFilter with the specified OpenGL value.
   *
   * @param glValue the OpenGL value representing the filter
   */
  TextureMagFilter(int glValue) {
    this.glValue = glValue;
  }

}
