package feature.prefabs.types;

import engine.Game;
import feature.systems.LevelEditorSystem;

/** Tells prefabs whether the game runs to edit levels, see {@link LevelEditorSystem#editing()}. */
final class LevelEditing {

  private LevelEditing() {}

  /**
   * Returns whether levels are being edited, so play-only effects such as intros are skipped.
   *
   * @return true while editing levels, always false on headless servers
   */
  static boolean active() {
    // Checked first: loading the editor needs a font renderer, which headless servers lack.
    return !Game.isHeadless() && LevelEditorSystem.editing();
  }
}
