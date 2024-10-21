package de.fwatermann.dungine.graphics.mesh;

import org.lwjgl.opengl.GL33;

/**
 * Enum representing the primitive types used in OpenGL. Each primitive type has an associated
 * OpenGL type.
 */
public enum PrimitiveType {

  /** Points primitive type. */
  POINTS(GL33.GL_POINTS),

  /** Line strip primitive type. */
  LINE_STRIP(GL33.GL_LINE_STRIP),

  /** Line loop primitive type. */
  LINE_LOOP(GL33.GL_LINE_LOOP),

  /** Lines primitive type. */
  LINES(GL33.GL_LINES),

  /** Line strip adjacency primitive type. */
  LINE_STRIP_ADJACENCY(GL33.GL_LINE_STRIP_ADJACENCY),

  /** Lines adjacency primitive type. */
  LINES_ADJACENCY(GL33.GL_LINES_ADJACENCY),

  /** Triangle strip primitive type. */
  TRIANGLE_STRIP(GL33.GL_TRIANGLE_STRIP),

  /** Triangle fan primitive type. */
  TRIANGLE_FAN(GL33.GL_TRIANGLE_FAN),

  /** Triangles primitive type. */
  TRIANGLES(GL33.GL_TRIANGLES),

  /** Triangle strip adjacency primitive type. */
  TRIANGLES_STRIP_ADJACENCY(GL33.GL_TRIANGLE_STRIP_ADJACENCY),

  /** Triangles adjacency primitive type. */
  TRIANGLES_ADJACENCY(GL33.GL_TRIANGLES_ADJACENCY);

  final int glType;

  /**
   * Constructs a PrimitiveType with the specified OpenGL type.
   *
   * @param glType the OpenGL type representing the primitive type
   */
  PrimitiveType(int glType) {
    this.glType = glType;
  }
}
