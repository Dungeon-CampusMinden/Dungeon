package contrib.editor.level.mode.deco;

/**
 * Enum representing the different edit modes for a decorative collider.
 *
 * <p>Each mode corresponds to a specific operation that can be performed on the collider, such as
 * changing the decoration or modifying its dimension and position offsets.
 *
 * <p>The enum also provides methods to retrieve a display name for each mode, navigate to the next
 * mode cyclically, and determine if the mode involves copying the collider to the clipboard.
 */
enum DecoColliderEditMode {
  /** Change the decoration of the collider. */
  CHANGE_DECO("Change deco"),
  /** Modify the X offset of the collider. */
  MODIFY_OFFSET_X("Modify offset X"),
  /** Modify the Y offset of the collider. */
  MODIFY_OFFSET_Y("Modify offset Y"),
  /** Modify the width of the collider. */
  MODIFY_WIDTH("Modify width"),
  /** Modify the height of the collider. */
  MODIFY_HEIGHT("Modify height");

  private final String displayName;

  DecoColliderEditMode(String displayName) {
    this.displayName = displayName;
  }

  String displayName() {
    return displayName;
  }

  DecoColliderEditMode next() {
    return values()[(ordinal() + 1) % values().length];
  }

  boolean copiesColliderToClipboard() {
    return this != CHANGE_DECO;
  }
}
