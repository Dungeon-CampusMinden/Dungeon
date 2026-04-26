package contrib.editor.level;

import contrib.editor.level.mode.LevelBoundsMode;
import contrib.editor.level.mode.LevelEditorMode;
import contrib.editor.level.mode.SaveMode;
import contrib.editor.level.mode.ShiftLevelMode;
import contrib.editor.level.mode.StartTilesMode;
import contrib.editor.level.mode.TilesMode;
import contrib.editor.level.mode.deco.DecoColliderMode;
import contrib.editor.level.mode.deco.DecoMode;
import contrib.editor.level.mode.point.PointMode;
import core.input.Keys;
import core.utils.InputManager;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Registry for all level editor modes, their factories, and hotkey lookup. */
final class LevelEditorModeRegistry {

  private final EnumMap<Mode, LevelEditorMode> modes = new EnumMap<>(Mode.class);

  LevelEditorModeRegistry(LevelEditorSystem system) {
    Objects.requireNonNull(system, "system must not be null");

    for (Mode mode : Mode.values()) {
      modes.put(mode, mode.create(system));
    }
  }

  LevelEditorMode mode(Mode mode) {
    LevelEditorMode modeInstance = modes.get(Objects.requireNonNull(mode, "mode must not be null"));
    if (modeInstance == null) {
      throw new IllegalStateException("No level editor mode registered for " + mode);
    }
    return modeInstance;
  }

  Optional<Mode> selectedModeByHotkey() {
    for (Mode mode : Mode.values()) {
      if (InputManager.isKeyJustPressed(mode.hotkey())) {
        return Optional.of(mode);
      }
    }

    return Optional.empty();
  }

  String modeSelectionText(Mode currentMode) {
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

  enum Mode {
    TILES(Keys.NUM_1, "1", TilesMode::new),
    DECOS(Keys.NUM_2, "2", DecoMode::new),
    POINTS(Keys.NUM_3, "3", PointMode::new),
    LEVEL_BOUNDS(Keys.NUM_4, "4", LevelBoundsMode::new),
    SHIFT_LEVEL(Keys.NUM_5, "5", ShiftLevelMode::new),
    START_TILES(Keys.NUM_6, "6", StartTilesMode::new),
    SAVE_LEVEL(Keys.NUM_7, "7", SaveMode::new),
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
