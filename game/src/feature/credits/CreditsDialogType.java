package feature.credits;

import feature.hud.dialogs.DialogType;

/** Dialog type identifier for credits displayed through the ECS-managed HUD. */
public enum CreditsDialogType implements DialogType {
  /** Credits dialog backed by a room ID and a local room JSON definition. */
  CREDITS("CREDITS");

  private final String type;

  CreditsDialogType(String type) {
    this.type = type;
  }

  @Override
  public String type() {
    return type;
  }
}
