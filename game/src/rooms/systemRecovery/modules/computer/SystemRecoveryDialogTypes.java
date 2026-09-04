package rooms.systemRecovery.modules.computer;

import feature.hud.dialogs.DialogType;

/** Custom dialog identifiers used by the System Recovery room. */
public enum SystemRecoveryDialogTypes implements DialogType {
  COMPUTER("systemRecoveryComputer");

  private final String typeName;

  SystemRecoveryDialogTypes(String typeName) {
    this.typeName = typeName;
  }

  @Override
  public String type() {
    return typeName;
  }
}
