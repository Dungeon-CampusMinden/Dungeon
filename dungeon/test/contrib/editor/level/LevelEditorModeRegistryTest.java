package contrib.editor.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.input.Keys;
import org.junit.jupiter.api.Test;

/** Tests for {@link LevelEditorSystem.LevelEditorModeRegistry}. */
public class LevelEditorModeRegistryTest {

  /** Ensures all editor hotkeys resolve to the expected mode registrations. */
  @Test
  public void modeByHotkeyReturnsRegisteredModes() {
    LevelEditorSystem.LevelEditorModeRegistry registry =
      new LevelEditorSystem.LevelEditorModeRegistry(new LevelEditorSystem());

    assertEquals(
      LevelEditorSystem.LevelEditorModeRegistry.Mode.TILES,
      registry.modeByHotkey(Keys.NUM_1).orElseThrow());
    assertEquals(
      LevelEditorSystem.LevelEditorModeRegistry.Mode.DECOS,
      registry.modeByHotkey(Keys.NUM_2).orElseThrow());
    assertEquals(
      LevelEditorSystem.LevelEditorModeRegistry.Mode.POINTS,
      registry.modeByHotkey(Keys.NUM_3).orElseThrow());
    assertEquals(
      LevelEditorSystem.LevelEditorModeRegistry.Mode.LEVEL_BOUNDS,
      registry.modeByHotkey(Keys.NUM_4).orElseThrow());
    assertEquals(
      LevelEditorSystem.LevelEditorModeRegistry.Mode.SHIFT_LEVEL,
      registry.modeByHotkey(Keys.NUM_5).orElseThrow());
    assertEquals(
      LevelEditorSystem.LevelEditorModeRegistry.Mode.START_TILES,
      registry.modeByHotkey(Keys.NUM_6).orElseThrow());
    assertEquals(
      LevelEditorSystem.LevelEditorModeRegistry.Mode.SAVE_LEVEL,
      registry.modeByHotkey(Keys.NUM_7).orElseThrow());
    assertEquals(
      LevelEditorSystem.LevelEditorModeRegistry.Mode.DECO_COLLIDER,
      registry.modeByHotkey(Keys.NUM_8).orElseThrow());
    assertTrue(registry.modeByHotkey(Keys.UNKNOWN).isEmpty());
  }

  /** Ensures overlay mode selection text clearly marks the active mode. */
  @Test
  public void modeSelectionTextHighlightsCurrentMode() {
    LevelEditorSystem.LevelEditorModeRegistry registry =
      new LevelEditorSystem.LevelEditorModeRegistry(new LevelEditorSystem());

    assertEquals(
      "1 | 2 | [3] | 4 | 5 | 6 | 7 | 8",
      registry.modeSelectionText(LevelEditorSystem.LevelEditorModeRegistry.Mode.POINTS));
  }
}
