package mushRoom.modules;

import contrib.hud.dialogs.DialogType;

/**
 * Enum representing different dialog types used in the Escape Room module.
 *
 * <p>This enum implements the {@link DialogType} interface to provide type names for various dialog
 * types specific to the Escape Room context.
 */
public enum EscapeRoomDialogTypes implements DialogType {
  /** A simple {@link hint.HintLogDialog} dialog type. */
  SIMPLE_HINT("simple_hint"),
  /** The {@link mushRoom.modules.journal.JournalUI} dialog type. */
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
