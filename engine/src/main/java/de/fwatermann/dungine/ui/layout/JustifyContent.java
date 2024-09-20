package de.fwatermann.dungine.ui.layout;

/**
 * Enum representing the justification of content within a container.
 */
public enum JustifyContent {
  /**
   * Items are packed at the start of the container.
   */
  FLEX_START,

  /**
   * Items are packed at the end of the container.
   */
  FLEX_END,

  /**
   * Items are centered in the container.
   */
  CENTER,

  /**
   * Items are evenly distributed with equal space between them.
   */
  SPACE_BETWEEN,

  /**
   * Items are evenly distributed with equal space around them.
   */
  SPACE_AROUND,

  /**
   * Items are evenly distributed with equal space around them, but with more space at the edges.
   */
  SPACE_EVENLY;
}
