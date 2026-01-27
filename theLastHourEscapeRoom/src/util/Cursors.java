package util;

public enum Cursors {
  DEFAULT("cursors/pointer_a.png", 9, 6),
  EXTERNAL("cursors/cursor_alias.png", 1, 1),
  INTERACT("cursors/pointer_l.png", 8, 7),
  CROSS("cursors/cross_small.png", 16, 16),
  TEXT("cursors/bracket_a_vertical.png", 16, 16),
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

  public String path() {
    return path;
  }

  public int hotspotX() {
    return hotspotX;
  }

  public int hotspotY() {
    return hotspotY;
  }
}
