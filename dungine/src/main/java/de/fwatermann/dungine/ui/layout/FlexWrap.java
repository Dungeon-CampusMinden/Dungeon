package de.fwatermann.dungine.ui.layout;

/** Enum representing the wrapping behavior of flexible items within a container. */
public enum FlexWrap {
  /** Items are laid out in a single line (no wrapping). */
  NO_WRAP,

  /** Items are laid out in multiple lines, from top to bottom. */
  WRAP,

  /** Items are laid out in multiple lines, from bottom to top. */
  WRAP_REVERSE
}
