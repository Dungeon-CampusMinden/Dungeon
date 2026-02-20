package core.utils;

/** Enum representing different cursor types and their associated image resources. */
public enum Cursors {

  /** The default cursor. */
  DEFAULT("cursors/pointer_a.png", 9, 6),

  /** A cursor indicating a link or external resource. */
  EXTERNAL("cursors/cursor_alias.png", 1, 1),

  /** A cursor indicating an action or interaction is possible. */
  INTERACT("cursors/pointer_l.png", 8, 7),

  /** A cursor indicating an action to close something. */
  CROSS("cursors/cross_small.png", 16, 16),

  /** A cursor indicating a text input field. */
  TEXT("cursors/bracket_a_vertical.png", 16, 16),

  /** A cursor indicating a forbidden action or unavailable option. */
  DISABLED("cursors/cursor_disabled.png", 1, 1),
  ;

  private final String path;
  private final int hotspotX;
  private final int hotspotY;

  Cursors(String path, int hotspotX, int hotspotY) {
    this.path = path;
    this.hotspotX = hotspotX;
    this.hotspotY = hotspotY;
  }

  /**
   * Get the file path of the cursor image resource.
   *
   * @return the file path of the cursor image resource
   */
  public String path() {
    return path;
  }

  /**
   * Get the x-coordinate of the cursor's hotspot.
   *
   * @return the x-coordinate of the cursor's hotspot
   */
  public int hotspotX() {
    return hotspotX;
  }

  /**
   * Get the y-coordinate of the cursor's hotspot.
   *
   * @return the y-coordinate of the cursor's hotspot
   */
  public int hotspotY() {
    return hotspotY;
  }
}
