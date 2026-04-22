package contrib.hud.crafting;

/**
 * Enumeration of actions available in the crafting dialog.
 *
 * <p>Each action represents a user-interactive button in the crafting interface, including
 * configuration for the label, callback key, icon, and relative position/size within the dialog.
 */
public enum CraftingDialogAction {
  /** Craft action-button. */
  CRAFT("Craft", CraftingDialogController.CALLBACK_CRAFT, "hud/check.png", 0.812f, 0.05f, 0.15f, 0.15f),
  /** Cancel action-button. */
  CANCEL("Cancel", CraftingDialogController.CALLBACK_CANCEL, "hud/cross.png", 0.036f, 0.05f, 0.15f, 0.15f);

  private final String label;
  private final String callbackKey;
  private final String iconPath;
  private final float relativeX;
  private final float relativeY;
  private final float relativeWidth;
  private final float relativeHeight;

  CraftingDialogAction(
    String label,
    String callbackKey,
    String iconPath,
    float relativeX,
    float relativeY,
    float relativeWidth,
    float relativeHeight) {
    this.label = label;
    this.callbackKey = callbackKey;
    this.iconPath = iconPath;
    this.relativeX = relativeX;
    this.relativeY = relativeY;
    this.relativeWidth = relativeWidth;
    this.relativeHeight = relativeHeight;
  }

  /**
   * Gets the display label for this action.
   *
   * @return the label text
   */
  public String label() {
    return label;
  }

  /**
   * Gets the callback key associated with this action.
   *
   * @return the callback key identifier
   */
  public String callbackKey() {
    return callbackKey;
  }

  /**
   * Gets the icon resource path for this action.
   *
   * @return the icon path
   */
  public String iconPath() {
    return iconPath;
  }

  /**
   * Gets the relative X position of this action's button.
   *
   * @return the relative X position (0.0 to 1.0)
   */
  public float relativeX() {
    return relativeX;
  }

  /**
   * Gets the relative Y position of this action's button.
   *
   * @return the relative Y position (0.0 to 1.0)
   */
  public float relativeY() {
    return relativeY;
  }

  /**
   * Gets the relative width of this action's button.
   *
   * @return the relative width (0.0 to 1.0)
   */
  public float relativeWidth() {
    return relativeWidth;
  }

  /**
   * Gets the relative height of this action's button.
   *
   * @return the relative height (0.0 to 1.0)
   */
  public float relativeHeight() {
    return relativeHeight;
  }
}
