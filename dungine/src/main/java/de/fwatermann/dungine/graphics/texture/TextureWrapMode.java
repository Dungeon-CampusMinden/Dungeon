package de.fwatermann.dungine.graphics.texture;

import org.lwjgl.opengl.GL33;

/**
 * Enum representing the texture wrap modes used in OpenGL. Each wrap mode has an associated OpenGL
 * constant.
 */
public enum TextureWrapMode {

  /** Clamp the texture coordinates to the edge of the texture. */
  CLAMP_TO_EDGE(GL33.GL_CLAMP_TO_EDGE),

  /** Clamp the texture coordinates to the border of the texture. */
  CLAMP_TO_BORDER(GL33.GL_CLAMP_TO_BORDER),

  /** Repeat the texture coordinates, mirroring them at every integer boundary. */
  MIRRORED_REPEAT(GL33.GL_MIRRORED_REPEAT),

  /** Repeat the texture coordinates. */
  REPEAT(GL33.GL_REPEAT);

  final int glValue;

  /**
   * Constructs a TextureWrapMode with the specified OpenGL constant.
   *
   * @param glValue the OpenGL constant representing the wrap mode
   */
  TextureWrapMode(int glValue) {
    this.glValue = glValue;
  }
}
