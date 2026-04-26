package contrib.editor.level.mode.deco;

import contrib.entities.deco.Deco;

/**
 * Represents the current state of a DecoCollider, managing the selected decorative object and the
 * editor mode.
 *
 * <p>This class stores the current decoration selection and editing mode for a DecoCollider,
 * allowing updates to these states through cycling or shifting operations.
 */
final class DecoColliderState {
  private Deco selectedDeco = Deco.values()[0];
  private DecoColliderEditOperation editMode = DecoColliderEditOperation.CHANGE_DECO;

  Deco selectedDeco() {
    return selectedDeco;
  }

  DecoColliderEditOperation editMode() {
    return editMode;
  }

  void cycleEditMode() {
    editMode = editMode.next();
  }

  void shiftSelectedDeco(int change) {
    Deco[] values = Deco.values();
    int nextIndex = Math.floorMod(selectedDeco.ordinal() + change, values.length);
    selectedDeco = values[nextIndex];
  }
}
