package de.fwatermann.dungine.ui.layout;

/** Enum representing the alignment on the cross axis (not the direction axis) of a container. */
public enum AlignContent {

  /** Default alignment. Items are packed in their default position. */
  NORMAL,

  /** Items are packed at the start of the container. */
  FLEX_START,

  /** Items are packed at the end of the container. */
  FLEX_END,

  /** Items are centered in the container. */
  CENTER,

  /** Items are stretched to fill the container. */
  STRETCH,

  /** Items are evenly distributed with equal space between them. */
  SPACE_BETWEEN,

  /** Items are evenly distributed with equal space around them. */
  SPACE_EVENLY,

  /**
   * Items are evenly distributed with equal space around them, but with more space at the edges.
   */
  SPACE_AROUND
}
