package rooms.mushroom.modules;

import feature.hud.dialogs.DialogType;
import rooms.mushroom.modules.journal.JournalUI;

/**
 * Enum representing different dialog types used in the Escape Room module.
 *
 * <p>This enum implements the {@link DialogType} interface to provide type names for various dialog
 * types specific to the Escape Room context.
 */
public enum EscapeRoomDialogTypes implements DialogType {
  /** The {@link JournalUI} dialog type. */
  JOURNAL("journal");

  private final String typeName;

  EscapeRoomDialogTypes(String typeName) {
    this.typeName = typeName;
  }

  @Override
  public String type() {
    return typeName;
  }
}
