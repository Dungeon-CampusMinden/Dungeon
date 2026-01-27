package modules.computer;

import contrib.hud.dialogs.DialogType;

/**
 * Enum representing different dialog types used in the Last Hour escape room.
 */
public enum LastHourDialogTypes implements DialogType {

  /** A simple {@link hint.HintLogDialog} dialog type. */
  COMPUTER("computer"),
  TRASHCAN("trashcan"),

  ;

  private final String typeName;

  LastHourDialogTypes(String typeName) {
    this.typeName = typeName;
  }

  @Override
  public String type() {
    return typeName;
  }
}
