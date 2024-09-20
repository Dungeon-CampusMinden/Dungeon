package de.fwatermann.dungine.ui.layout;

/**
 * Enum representing the alignment of a single item on the cross axis (not the direction axis) of a
 * line in a container.
 *
 * <p>This is used to override the align-items property of the parent container.
 */
public enum AlignSelf {
  /** Default alignment. The item will follow the container's alignment settings. */
  AUTO,

  /** The item is packed at the start of the line. */
  FLEX_START,

  /** The item is packed at the end of the line. */
  FLEX_END,

  /** The item is centered in the line. */
  CENTER,

  /** The item is stretched to fill the line. */
  STRETCH;
}
