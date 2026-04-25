package contrib.editor.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/** Tests for {@link LevelEditorModeRegistry}. */
public class LevelEditorModeRegistryTest {

  /** Ensures all editor modes are eagerly registered and can be resolved. */
  @Test
  public void modeReturnsRegisteredModes() {
    LevelEditorModeRegistry registry = new LevelEditorModeRegistry(new LevelEditorSystem());

    for (LevelEditorModeRegistry.Mode mode : LevelEditorModeRegistry.Mode.values()) {
      assertNotNull(registry.mode(mode));
    }
  }

  /** Ensures overlay mode selection text clearly marks the active mode. */
  @Test
  public void modeSelectionTextHighlightsCurrentMode() {
    LevelEditorModeRegistry registry = new LevelEditorModeRegistry(new LevelEditorSystem());

    assertEquals(
      "1 | 2 | [3] | 4 | 5 | 6 | 7 | 8",
      registry.modeSelectionText(LevelEditorModeRegistry.Mode.POINTS));
  }
}
