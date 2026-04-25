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
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.game.render.RenderContext;
import core.input.Keys;
import core.level.DungeonLevel;
import core.platform.Platform;
import core.utils.InputManager;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * The LevelEditorSystem class provides a toolset for editing game levels
 * within the game's runtime environment.
 *
 * <p>It includes features for interacting with tiles, entities, and dungeon levels, as well as providing visual
 * feedback and debug overlays for level editing modes.
 *
 * <p>This system orchestrates editor mode execution, rendering, and overlay updates while the
 * session object manages activation, input capture, and run/pause behavior.
 */
public final class LevelEditorSystem extends System {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LevelEditorSystem.class);

  private static final int TOGGLE_ACTIVE = Keys.F4;
  private static final int TOGGLE_LAYER_DEBUG = Keys.SPACE;

  private final LevelEditorDebugRenderer layerDebugRenderer = new LevelEditorDebugRenderer();
  private final LevelEditorModeRegistry modeRegistry = new LevelEditorModeRegistry(this);
  private final LevelEditorOverlayPresenter overlayPresenter = new LevelEditorOverlayPresenter();
  private final LevelEditorSession session = new LevelEditorSession();

  private boolean layerDebugActive = false;
  private LevelEditorModeRegistry.Mode currentMode = LevelEditorModeRegistry.Mode.TILES;

  /**
   * Returns whether the editor is currently active.
   *
   * @return true if active
   */
  public boolean active() {
    return session.active();
  }

  /**
   * Activates or deactivates the level editor.
   *
   * @param active new editor state
   */
  public void active(boolean active) {
    LevelEditorSession.ActivationTransition transition = session.changeActivation(active);
    if (!transition.changed()) {
      return;
    }

    if (active) {
      if (transition.hasCapturedPlayer()) {
        overlayPresenter.attach();
        onModeEnter(currentMode);
        updateOverlay();
        LOGGER.info("Level editor activated.");
      }
      return;
    }

    if (transition.hadCapturedPlayer()) {
      onModeExit(currentMode);
      LOGGER.info("Level editor deactivated.");
    }

    overlayPresenter.detach();
  }

  @Override
  public void execute() {
    if (InputManager.isKeyJustPressed(TOGGLE_ACTIVE)) {
      active(!active());
    }

    if (!active()) {
      return;
    }

    Optional<PlayerComponent> playerComponent =
      Game.player().flatMap(player -> player.fetch(PlayerComponent.class));

    if (playerComponent.isPresent() && playerComponent.get().openDialogs()) {
      updateOverlay();
      return;
    }

    if (InputManager.isKeyJustPressed(TOGGLE_LAYER_DEBUG)) {
      layerDebugActive = !layerDebugActive;
    }

    LevelEditorModeRegistry.Mode selectedMode = modeRegistry.selectedModeByHotkey().orElse(currentMode);
    boolean modeChanged = selectedMode != currentMode;

    if (modeChanged) {
      onModeExit(currentMode);
      currentMode = selectedMode;
      onModeEnter(currentMode);
    }

    if (session.shouldExecuteMode(modeChanged)) {
      currentModeInstance().doExecute();
    }

    updateOverlay();
  }

  @Override
  public void render(float deltaSeconds) {
    if (!active()) {
      return;
    }

    Graphics2D g = RenderContext.get();
    if (g == null) {
      return;
    }

    layerDebugRenderer.render(g, layerDebugActive);
    currentModeInstance().render(g, deltaSeconds);
    updateOverlay();
  }

  @Override
  public void stop() {
    session.stop();
  }

  @Override
  public void run() {
    session.run();
  }

  /**
   * Displays a feedback message in the editor overlay.
   *
   * @param message message text
   * @param color message color
   */
  public void showModeFeedback(String message, Color color) {
    overlayPresenter.showFeedback(message, color);
  }

  /**
   * Retrieves the current dungeon level for mode operations.
   *
   * @return current dungeon level if present
   */
  public Optional<DungeonLevel> currentDungeonLevelForModes() {
    return currentDungeonLevel();
  }

  /**
   * Returns the cursor position snapped to tile coordinates.
   *
   * @return snapped tile position
   */
  public Point snappedCursorTileForModes() {
    return snappedCursorTile();
  }

  private LevelEditorMode currentModeInstance() {
    return modeRegistry.mode(currentMode);
  }

  private void updateOverlay() {
    if (!active()) {
      return;
    }

    overlayPresenter.update(
      currentModeInstance(), modeRegistry.modeSelectionText(currentMode), layerDebugActive);
  }

  private Optional<DungeonLevel> currentDungeonLevel() {
    return Game.currentLevel()
      .filter(DungeonLevel.class::isInstance)
      .map(DungeonLevel.class::cast);
  }

  private Point snappedCursorTile() {
    Point world = Platform.cursor().world();
    return new Point((float) Math.floor(world.x()), (float) Math.floor(world.y()));
  }

  private void onModeEnter(LevelEditorModeRegistry.Mode mode) {
    modeRegistry.mode(mode).onEnter();
  }

  private void onModeExit(LevelEditorModeRegistry.Mode mode) {
    modeRegistry.mode(mode).onExit();
  }

  /** Registry for all level editor modes, their factories, and hotkey lookup. */
  static final class LevelEditorModeRegistry {

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

    Optional<Mode> modeByHotkey(int hotkey) {
      for (Mode mode : Mode.values()) {
        if (mode.hotkey() == hotkey) {
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

      Mode(
        int hotkey,
        String hotkeyLabel,
        Function<LevelEditorSystem, LevelEditorMode> modeFactory) {
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
}
