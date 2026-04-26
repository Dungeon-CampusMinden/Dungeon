package contrib.editor.level.mode;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import contrib.editor.level.LevelEditorSystem;
import core.input.InputLabelFormatter.InputCode;
import core.input.Keys;
import core.input.MouseButtons;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            LevelEditorMode.QUATERNARY,
            MouseButtons.LEFT,
            MouseButtons.RIGHT),
        LevelEditorMode.editorInputs());
  }

  /** Ensures callers cannot accidentally mutate the central editor input list. */
  @Test
  public void editorInputsAreImmutable() {
    assertThrows(UnsupportedOperationException.class, () -> LevelEditorMode.editorInputs().clear());
  }

  /** Ensures mode control labels preserve input type even when raw integer codes overlap. */
  @Test
  public void fullStatusLinesUseTypedControlLabels() {
    LevelEditorMode mode = new TypedControlMode();

    assertIterableEquals(
        List.of(
            "--- Typed Controls ---",
            "Controls:",
            " - UNKNOWN: Keyboard code 0",
            " - LMB: Mouse code 0",
            "",
            "Settings:",
            " -"),
        mode.getFullStatusLines());
  }

  private static final class TypedControlMode extends LevelEditorMode {
    private TypedControlMode() {
      super(new LevelEditorSystem(), "Typed Controls");
    }

    @Override
    protected void execute() {
      // test mode does not execute editor actions
    }

    @Override
    protected List<String> getStatusLines() {
      return List.of();
    }

    @Override
    protected Map<InputCode, String> getControls() {
      Map<InputCode, String> controls = new LinkedHashMap<>();
      controls.put(key(Keys.UNKNOWN), "Keyboard code 0");
      controls.put(mouseButton(MouseButtons.LEFT), "Mouse code 0");
      return controls;
    }
  }
}
