package contrib.editor.level.mode;

import contrib.editor.level.LevelEditorSystem;
import contrib.editor.level.mode.deco.DecoColliderMode;
import contrib.editor.level.mode.deco.DecoMode;
import contrib.editor.level.mode.point.PointMode;
import core.input.Keys;
import core.utils.InputManager;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Registry for managing different level editor modes.
 *
 * <p>This class provides access to level editor modes identified by their {@link Mode} enum
 * and handles mode selection through hotkeys.
 */
public final class LevelEditorModeRegistry {

  private final EnumMap<Mode, LevelEditorMode> modes = new EnumMap<>(Mode.class);

  /**
   * Constructs a new LevelEditorModeRegistry and initializes all available editor modes.
   *
   * @param system the {@link LevelEditorSystem} to be used by the modes
   * @throws NullPointerException if system is null
   */
  public LevelEditorModeRegistry(LevelEditorSystem system) {
    Objects.requireNonNull(system, "system must not be null");

    for (Mode mode : Mode.values()) {
      modes.put(mode, mode.create(system));
    }
  }

  /**
   * Retrieves the {@link LevelEditorMode} instance for the specified mode.
   *
   * @param mode the {@link Mode} enum value identifying the desired mode
   * @return the corresponding {@link LevelEditorMode} instance
   * @throws NullPointerException if mode is null
   * @throws IllegalStateException if no mode is registered for the given mode
   */
  public LevelEditorMode mode(Mode mode) {
    LevelEditorMode modeInstance = modes.get(Objects.requireNonNull(mode, "mode must not be null"));
    if (modeInstance == null) {
      throw new IllegalStateException("No level editor mode registered for " + mode);
    }
    return modeInstance;
  }

  /**
   * Detects if a hotkey for any registered mode was just pressed.
   *
   * @return an {@link Optional} containing the {@link Mode} if a hotkey was pressed,
   *         or an empty {@link Optional} otherwise
   */
  public Optional<Mode> selectedModeByHotkey() {
    for (Mode mode : Mode.values()) {
      if (InputManager.isKeyJustPressed(mode.hotkey())) {
        return Optional.of(mode);
      }
    }

    return Optional.empty();
  }

  /**
   * Generates a formatted text representation of all available modes and their hotkeys.
   * The currently selected mode is highlighted with brackets.
   *
   * @param currentMode the {@link Mode} that is currently active
   * @return a formatted string displaying all modes with their hotkey labels,
   *         with the current mode highlighted in brackets
   */
  public String modeSelectionText(Mode currentMode) {
    StringBuilder sb = new StringBuilder();

    Mode[] selectableModes = Mode.values();
    for (int i = 0; i < selectableModes.length; i++) {
      Mode mode = selectableModes[i];

      if (i > 0) {
        sb.append(" | ");
      }

      if (mode == currentMode) {
        sb.append("[").append(mode.hotkeyLabel()).append("]");
      } else {
        sb.append(mode.hotkeyLabel());
      }
    }

    return sb.toString();
  }

  /**
   * Represents the various modes available in the level editor.
   *
   * <p>Each mode is associated with a specific hotkey, its corresponding label, and a
   * factory function used to create the associated {@link LevelEditorMode}.
   */
  public enum Mode {
    /** Represents the Tiles (TILES) mode in the level editor. */
    TILES(Keys.NUM_1, "1", TilesMode::new),
    /** Represents the Decorations (DECOS) mode in the level editor. */
    DECOS(Keys.NUM_2, "2", DecoMode::new),
    /** Represents the Points (POINTS) mode in the level editor. */
    POINTS(Keys.NUM_3, "3", PointMode::new),
    /** Represents the Level Bounds (LEVEL_BOUNDS) mode in the level editor. */
    LEVEL_BOUNDS(Keys.NUM_4, "4", LevelBoundsMode::new),
    /** Represents the Shift Level (SHIFT_LEVEL) mode in the level editor. */
    SHIFT_LEVEL(Keys.NUM_5, "5", ShiftLevelMode::new),
    /** Represents the Start Tiles (START_TILES) mode in the level editor. */
    START_TILES(Keys.NUM_6, "6", StartTilesMode::new),
    /** Represents the Save Level (SAVE_LEVEL) mode in the level editor. */
    SAVE_LEVEL(Keys.NUM_7, "7", SaveMode::new),
    /** Represents the Deco Collider (DECO_COLLIDER) mode in the level editor. */
    DECO_COLLIDER(Keys.NUM_8, "8", DecoColliderMode::new);

    private final int hotkey;
    private final String hotkeyLabel;
    private final Function<LevelEditorSystem, LevelEditorMode> modeFactory;

    Mode(int hotkey, String hotkeyLabel, Function<LevelEditorSystem, LevelEditorMode> modeFactory) {
      this.hotkey = hotkey;
      this.hotkeyLabel = Objects.requireNonNull(hotkeyLabel, "hotkeyLabel must not be null");
      this.modeFactory = Objects.requireNonNull(modeFactory, "modeFactory must not be null");
    }

    int hotkey() {
      return hotkey;
    }

    private String hotkeyLabel() {
      return hotkeyLabel;
    }

    private LevelEditorMode create(LevelEditorSystem system) {
      return modeFactory.apply(system);
    }
  }
}
