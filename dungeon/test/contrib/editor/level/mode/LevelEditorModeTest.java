package contrib.editor.level.mode;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import core.input.MouseButtons;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link LevelEditorMode}. */
public class LevelEditorModeTest {

  /** Ensures that all editor inputs are reserved together while the editor is active. */
  @Test
  public void editorInputsContainAllReservedInputs() {
    assertIterableEquals(
      List.of(
        LevelEditorMode.PRIMARY_UP,
        LevelEditorMode.PRIMARY_DOWN,
        LevelEditorMode.SECONDARY_UP,
        LevelEditorMode.SECONDARY_DOWN,
        LevelEditorMode.TERTIARY,
        LevelEditorMode.QUARTERNARY,
        MouseButtons.LEFT,
        MouseButtons.RIGHT),
      LevelEditorMode.editorInputs());
  }

  /** Ensures callers cannot accidentally mutate the central editor input list. */
  @Test
  public void editorInputsAreImmutable() {
    assertThrows(UnsupportedOperationException.class, () -> LevelEditorMode.editorInputs().clear());
  }
}
