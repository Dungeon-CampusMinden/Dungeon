package contrib.hud.crafting;

/**
 * Shared action metadata for crafting dialogs across UI backends.
 *
 * <p>The action model is backend-neutral: it describes what the dialog should expose,
 * while concrete button widgets remain backend-specific.
 */
public enum CraftingDialogAction {
  CRAFT("Craft", CraftingDialogController.CALLBACK_CRAFT, "hud/check.png", 0.812f, 0.05f, 0.15f, 0.15f),
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

  public String label() {
    return label;
  }

  public String callbackKey() {
    return callbackKey;
  }

  public String iconPath() {
    return iconPath;
  }

  public float relativeX() {
    return relativeX;
  }

  public float relativeY() {
    return relativeY;
  }

  public float relativeWidth() {
    return relativeWidth;
  }

  public float relativeHeight() {
    return relativeHeight;
  }
}
