package feature.hints;

import feature.hud.dialogs.DialogType;

/** Dialog type used by the reusable hint log. */
public enum HintDialogType implements DialogType {
  /** A dialog that displays one hint at a time. */
  SIMPLE_HINT("simple_hint");

  private final String typeName;

  HintDialogType(String typeName) {
    this.typeName = typeName;
  }

  @Override
  public String type() {
    return typeName;
  }
}
