package contrib.editor.level;

import contrib.components.DecoComponent;
import contrib.components.HealthComponent;
import contrib.editor.level.mode.*;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.InputComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.input.Keys;
import core.input.MouseButtons;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.platform.Platform;
import core.camera.CameraViewportState;
import core.game.render.RenderContext;
import core.game.render.overlay.TileOverlaySizing;
import contrib.debug.systems.DebugDrawSystem;
import core.ui.overlay.UiOverlayRegistry;
import core.ui.StageHandle;
import core.utils.*;
import core.utils.logging.DungeonLogger;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.*;

/**
 * A system that provides an interactive in-game level editor for designing and editing dungeon
 * levels.
 *
 * <p>The editor supports multiple editing modes:
 * <ul>
 *   <li>Tiles - edit tile types and properties
 *   <li>Decos - place and manage decorative entities
 *   <li>Points - set special points like spawn locations
 *   <li>Level Bounds - define level boundaries
 *   <li>Shift Level - shift level content
 *   <li>Start Tiles - configure starting tile positions
 *   <li>Save Level - save level configuration
 *   <li>Deco Collider - manage collision data for decorative objects
 * </ul>
 *
 * <p>The editor can be toggled with F4 and provides keyboard shortcuts (1-8) to switch between
 * modes. It includes debug visualization capabilities for viewing tile properties and entity
 * information.
 */
public final class LevelEditorSystem extends System {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(LevelEditorSystem.class);

  private static final long FEEDBACK_DURATION_MS = 3000L;

  private static final int TOGGLE_ACTIVE = Keys.F4;

  private static final int MODE_1 = Keys.NUM_1;
  private static final int MODE_2 = Keys.NUM_2;
  private static final int MODE_3 = Keys.NUM_3;
  private static final int MODE_4 = Keys.NUM_4;
  private static final int MODE_5 = Keys.NUM_5;
  private static final int MODE_6 = Keys.NUM_6;
  private static final int MODE_7 = Keys.NUM_7;
  private static final int MODE_8 = Keys.NUM_8;

  private final LevelEditorOverlay overlay = new LevelEditorOverlay();

  private boolean active = false;
  private Mode currentMode = Mode.TILES;
  private Map<Integer, InputComponent.InputData> playerCallbacks = null;

  private String feedbackMessage = "";
  private Color feedbackColor = Color.WHITE;
  private long feedbackUntilMs = 0L;

  private static final Color LEVEL_BOUNDS_OUTLINE_COLOR = new Color(0, 255, 0, 77);
  private static final float LEVEL_BOUNDS_OUTLINE_STROKE = 2.0f;

  private boolean internalStopped = false;
  private Mode previousMode = Mode.TILES;

  private static final int TOGGLE_DEBUG_VISUALIZATION = Keys.SPACE;

  private static final Color DEBUG_LEVEL_TILE_OUTLINE_COLOR = new Color(80, 140, 255, 150);
  private static final Color DEBUG_BLOCKED_TILE_FILL_COLOR = new Color(255, 80, 80, 85);
  private static final Color DEBUG_SEE_THROUGH_TILE_FILL_COLOR = new Color(80, 220, 120, 70);

  private static final Color DEBUG_PLAYER_ENTITY_COLOR = Color.RED;
  private static final Color DEBUG_DECO_ENTITY_COLOR = Color.GREEN;
  private static final Color DEBUG_NORMAL_ENTITY_COLOR = Color.WHITE;

  private static final Color DEBUG_COORD_TEXT_COLOR = new Color(230, 230, 255, 220);
  private static final int DEBUG_TEXT_MIN_TILE_PX = 32;
  private static final float DEBUG_ENTITY_STROKE = 2.0f;
  private static final int DEBUG_ENTITY_INSET_PX = 2;

  private boolean debugVisualizationActive = false;

  private final LevelEditorMode tilesMode = new TilesMode(this);
  private final LevelEditorMode decoMode = new DecoMode(this);
  private final LevelEditorMode saveMode = new SaveMode(this);
  private final LevelEditorMode shiftLevelMode = new ShiftLevelMode(this);
  private final LevelEditorMode levelBoundsMode = new LevelBoundsMode(this);
  private final LevelEditorMode pointMode = new PointMode(this);
  private final LevelEditorMode startTilesMode = new StartTilesMode(this);
  private final LevelEditorMode decoColliderMode = new DecoColliderMode(this);

  /** Creates the level editor. */
  public LevelEditorSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    if (InputManager.isKeyJustPressed(TOGGLE_ACTIVE)) {
      setActive(!this.active);
    }

    if (!this.active) {
      return;
    }

    Optional<PlayerComponent> pc = Game.player().flatMap(e -> e.fetch(PlayerComponent.class));
    if (pc.isPresent() && pc.get().openDialogs()) {
      syncOverlay();
      return;
    }

    if (InputManager.isKeyJustPressed(TOGGLE_DEBUG_VISUALIZATION)) {
      debugVisualizationActive = !debugVisualizationActive;
      showFeedback(
        "Debug visualization: " + (debugVisualizationActive ? "ON" : "OFF"),
        Color.WHITE);
    }

    Mode oldMode = this.currentMode;
    handleModeHotkeys();

    if (oldMode != this.currentMode) {
      onModeExit(oldMode);
      onModeEnter(this.currentMode);
      this.previousMode = this.currentMode;
    }

    if (!this.internalStopped || oldMode != this.currentMode) {
      executeCurrentMode();
    }

    syncOverlay();
  }

  @Override
  public void render(float deltaSeconds) {
    if (!this.active) {
      return;
    }

    Graphics2D g = RenderContext.get();
    if (g == null) {
      return;
    }

    renderLevelBoundsOutline();

    if (debugVisualizationActive) {
      renderDebugVisualization();
    }

    activeModeInstance().render(g, deltaSeconds);
  }

  private LevelEditorMode activeModeInstance() {
    return switch (this.currentMode) {
      case TILES -> tilesMode;
      case DECOS -> decoMode;
      case POINTS -> pointMode;
      case LEVEL_BOUNDS -> levelBoundsMode;
      case SHIFT_LEVEL -> shiftLevelMode;
      case START_TILES -> startTilesMode;
      case SAVE_LEVEL -> saveMode;
      case DECO_COLLIDER -> decoColliderMode;
    };
  }

  /** Returns whether the editor is currently active. */
  public boolean active() {
    return this.active;
  }

  private void executeCurrentMode() {
    switch (currentMode) {
      case TILES -> tilesMode.doExecute();
      case DECOS -> decoMode.doExecute();
      case POINTS -> pointMode.doExecute();
      case LEVEL_BOUNDS -> levelBoundsMode.doExecute();
      case SHIFT_LEVEL -> shiftLevelMode.doExecute();
      case START_TILES -> startTilesMode.doExecute();
      case SAVE_LEVEL -> saveMode.doExecute();
      case DECO_COLLIDER -> decoColliderMode.doExecute();
    }
  }

  private Point snappedCursorTile() {
    Point cursorWorld = Platform.cursor().world();
    return new Point((float) Math.floor(cursorWorld.x()), (float) Math.floor(cursorWorld.y()));
  }

  private void setActive(boolean active) {
    if (this.active == active) {
      return;
    }

    this.active = active;

    if (active) {
      suspendConflictingPlayerCallbacks();
      enablePlayerGodMode(true);

      onModeEnter(this.currentMode);

      overlay.visible(true);
      if (!UiOverlayRegistry.contains(overlay)) {
        UiOverlayRegistry.add(overlay);
      }

      showFeedback("Level editor active", new Color(120, 220, 120));
      syncOverlay();
      LOGGER.info("Activated level editor.");
      return;
    }

    onModeExit(this.currentMode);

    restorePlayerCallbacks();
    enablePlayerGodMode(false);

    overlay.visible(false);
    UiOverlayRegistry.remove(overlay);

    feedbackMessage = "";
    feedbackUntilMs = 0L;

    LOGGER.info("Deactivated level editor.");
  }

  private void suspendConflictingPlayerCallbacks() {
    Entity player = Game.player().orElse(null);
    if (player == null) {
      return;
    }

    player.fetch(InputComponent.class)
      .ifPresent(
        pc -> {
          playerCallbacks = pc.callbacks();

          pc.removeCallback(Keys.E);
          pc.removeCallback(Keys.Q);
          pc.removeCallback(Keys.C);
          pc.removeCallback(Keys.Z);
          pc.removeCallback(Keys.X);
          pc.removeCallback(Keys.V);
          pc.removeCallback(MouseButtons.LEFT);
          pc.removeCallback(MouseButtons.RIGHT);
        });
  }

  private void restorePlayerCallbacks() {
    if (playerCallbacks == null) {
      return;
    }

    Entity player = Game.player().orElse(null);
    if (player == null) {
      playerCallbacks = null;
      return;
    }

    player.fetch(InputComponent.class)
      .ifPresent(
        pc ->
          playerCallbacks.forEach(
            (key, value) ->
              pc.registerCallback(key, value.callback(), value.repeat(), value.pauseable())));

    playerCallbacks = null;
  }

  private void enablePlayerGodMode(boolean enabled) {
    Game.player()
      .flatMap(player -> player.fetch(HealthComponent.class))
      .ifPresent(hc -> hc.godMode(enabled));
  }

  private void handleModeHotkeys() {
    if (InputManager.isKeyJustPressed(MODE_1)) {
      switchMode(Mode.getMode(0));
    } else if (InputManager.isKeyJustPressed(MODE_2)) {
      switchMode(Mode.getMode(1));
    } else if (InputManager.isKeyJustPressed(MODE_3)) {
      switchMode(Mode.getMode(2));
    } else if (InputManager.isKeyJustPressed(MODE_4)) {
      switchMode(Mode.getMode(3));
    } else if (InputManager.isKeyJustPressed(MODE_5)) {
      switchMode(Mode.getMode(4));
    } else if (InputManager.isKeyJustPressed(MODE_6)) {
      switchMode(Mode.getMode(5));
    } else if (InputManager.isKeyJustPressed(MODE_7)) {
      switchMode(Mode.getMode(6));
    } else if (InputManager.isKeyJustPressed(MODE_8)) {
      switchMode(Mode.getMode(7));
    }
  }

  private void switchMode(Mode newMode) {
    if (this.currentMode == newMode) {
      return;
    }

    Mode oldMode = this.currentMode;

    onModeExit(oldMode);
    this.currentMode = newMode;
    this.previousMode = newMode;
    onModeEnter(newMode);

    showFeedback("Switched to " + newMode.displayName() + " mode", Color.WHITE);
  }

  private void syncOverlay() {
    if (!UiOverlayRegistry.contains(overlay)) {
      UiOverlayRegistry.add(overlay);
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage != null) {
      overlay.x(12);
      overlay.y(12);
      overlay.width(Math.clamp(Math.round(stage.getWidth()) - 24, 420, 820));
      overlay.height(
        currentMode == Mode.TILES
          || currentMode == Mode.DECOS
          || currentMode == Mode.POINTS
          || currentMode == Mode.START_TILES
          || currentMode == Mode.DECO_COLLIDER
          ? 320
          : 230);
    }

    overlay.content(
      "Level Editor",
      buildStatusLines(),
      currentFeedbackMessage(),
      currentFeedbackColor());
  }

  private List<String> buildStatusLines() {
    List<String> lines = new ArrayList<>();
    lines.add("F4: toggle editor");
    lines.add("1-8: switch mode");
    lines.add("Current mode: " + currentMode.displayName());
    lines.add(
      "SPACE: toggle debug visualization [" + (debugVisualizationActive ? "ON" : "OFF") + "]");
    lines.add("Modes: " + modeSelectionText());
    lines.add("");

    if (currentMode == Mode.TILES) {
      lines.addAll(tilesMode.getFullStatusLines());
    } else if (currentMode == Mode.DECOS) {
      lines.addAll(decoMode.getFullStatusLines());
    } else if (currentMode == Mode.POINTS) {
      lines.addAll(pointMode.getFullStatusLines());
    } else if (currentMode == Mode.LEVEL_BOUNDS) {
      lines.addAll(levelBoundsMode.getFullStatusLines());
    } else if (currentMode == Mode.SHIFT_LEVEL) {
      lines.addAll(shiftLevelMode.getFullStatusLines());
    } else if (currentMode == Mode.START_TILES) {
      lines.addAll(startTilesMode.getFullStatusLines());
    } else if (currentMode == Mode.SAVE_LEVEL) {
      lines.addAll(saveMode.getFullStatusLines());
    } else if (currentMode == Mode.DECO_COLLIDER) {
      lines.addAll(decoColliderMode.getFullStatusLines());
    } else {
      lines.add("This mode is not yet ported.");
    }

    return lines;
  }

  private String modeSelectionText() {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < Mode.values().length; i++) {
      if (i > 0) {
        sb.append(" | ");
      }

      Mode mode = Mode.values()[i];
      if (mode == currentMode) {
        sb.append("[").append(i + 1).append("] ").append(mode.displayName());
      } else {
        sb.append(i + 1).append(" ").append(mode.displayName());
      }
    }

    return sb.toString();
  }

  private void showFeedback(String message, Color color) {
    this.feedbackMessage = message == null ? "" : message;
    this.feedbackColor = color == null ? Color.WHITE : color;
    this.feedbackUntilMs = Time.nowMs() + FEEDBACK_DURATION_MS;
  }

  private String currentFeedbackMessage() {
    return Time.nowMs() <= feedbackUntilMs ? feedbackMessage : "";
  }

  private Color currentFeedbackColor() {
    return Time.nowMs() <= feedbackUntilMs ? feedbackColor : Color.WHITE;
  }

  private enum Mode {
    TILES("Tiles"),
    DECOS("Decos"),
    POINTS("Points"),
    LEVEL_BOUNDS("LevelBounds"),
    SHIFT_LEVEL("ShiftLevel"),
    START_TILES("StartTiles"),
    SAVE_LEVEL("SaveLevel"),
    DECO_COLLIDER("DecoCollider");

    private final String displayName;

    Mode(String displayName) {
      this.displayName = displayName;
    }

    public String displayName() {
      return displayName;
    }

    public static Mode getMode(int number) {
      if (number < 0 || number >= values().length) {
        throw new IllegalArgumentException("Invalid mode number: " + number);
      }
      return values()[number];
    }
  }

  private Optional<DungeonLevel> currentDungeonLevel() {
    return Game.currentLevel()
      .filter(DungeonLevel.class::isInstance)
      .map(DungeonLevel.class::cast);
  }

  private void renderLevelBoundsOutline() {
    CameraViewportState.Viewport view = CameraViewportState.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    currentDungeonLevel()
      .ifPresent(
        level -> {
          Tile[][] layout = level.layout();
          if (layout.length == 0 || layout[0].length == 0) {
            return;
          }

          int levelWidth = layout[0].length;
          int levelHeight = layout.length;

          DebugDrawSystem.drawRectangleOutline(
            0f,
            0f,
            levelWidth,
            levelHeight,
            LEVEL_BOUNDS_OUTLINE_COLOR);
        });
  }

  private void onModeEnter(Mode mode) {
    switch (Objects.requireNonNull(mode)) {
      case DECOS -> decoMode.onEnter();
      case POINTS -> pointMode.onEnter();
      case START_TILES -> startTilesMode.onEnter();
      case DECO_COLLIDER -> decoColliderMode.onEnter();
      default -> {
        // no-op for now
      }
    }
  }

  private void onModeExit(Mode mode) {
    switch (mode) {
      case DECOS -> decoMode.onExit();
      case POINTS -> pointMode.onExit();
      case DECO_COLLIDER -> decoColliderMode.onExit();
      default -> {
        // no-op for now
      }
    }
  }

  @Override
  public void stop() {
    this.internalStopped = true;
  }

  @Override
  public void run() {
    this.internalStopped = false;
  }

  private void renderDebugVisualization() {
    CameraViewportState.activeViewport()
      .ifPresent(
        view ->
          currentDungeonLevel()
            .ifPresent(
              level -> {
                renderDebugTiles(level, view);
                renderDebugEntities(view);
              }));
  }

  private void renderDebugTiles(DungeonLevel level, CameraViewportState.Viewport view) {
    Tile[][] layout = level.layout();
    int tilePx = view.tilePx();

    for (int y = 0; y < layout.length; y++) {
      for (int x = 0; x < layout[y].length; x++) {
        Tile tile = layout[y][x];
        LevelElement element = tile.levelElement();

        if (!element.value()) {
          DebugDrawSystem.fillWorldRectangle(
            x, y, 1f, 1f, DEBUG_BLOCKED_TILE_FILL_COLOR);
        } else if (element.canSeeThrough()) {
          DebugDrawSystem.fillWorldRectangle(
            x, y, 1f, 1f, DEBUG_SEE_THROUGH_TILE_FILL_COLOR);
        }

        DebugDrawSystem.drawRectangleOutline(
          x, y, 1f, 1f, DEBUG_LEVEL_TILE_OUTLINE_COLOR);

        if (tilePx >= DEBUG_TEXT_MIN_TILE_PX) {
          DebugDrawSystem.drawText(
            x + "," + y,
            tileDebugTextPosition(x, y, view),
            DEBUG_COORD_TEXT_COLOR);
        }
      }
    }
  }

  private void renderDebugEntities(CameraViewportState.Viewport view) {
    int tilePx = view.tilePx();
    float insetWorld = TileOverlaySizing.worldInsetFromPixels(tilePx, DEBUG_ENTITY_INSET_PX);
    float sizeWorld = Math.max(0.05f, 1f - insetWorld * 2f);

    Game.levelEntities(Set.of(PositionComponent.class, DrawComponent.class))
      .forEach(
        entity -> {
          PositionComponent pc = entity.fetch(PositionComponent.class).orElse(null);
          if (pc == null) {
            return;
          }

          Point pos = pc.position();

          DebugDrawSystem.drawRectangleOutline(
            pos.x() + insetWorld,
            pos.y() + insetWorld,
            sizeWorld,
            sizeWorld,
            debugEntityColor(entity));

          if (tilePx >= DEBUG_TEXT_MIN_TILE_PX) {
            DebugDrawSystem.drawText(
              debugEntityLabel(entity),
              entityDebugLabelPosition(pos, view),
              debugEntityColor(entity));
          }
        });
  }

  private Color debugEntityColor(Entity entity) {
    if (entity.isPresent(PlayerComponent.class)) {
      return DEBUG_PLAYER_ENTITY_COLOR;
    }

    if (entity.isPresent(DecoComponent.class)) {
      return DEBUG_DECO_ENTITY_COLOR;
    }

    return DEBUG_NORMAL_ENTITY_COLOR;
  }

  private String debugEntityLabel(Entity entity) {
    if (entity.isPresent(PlayerComponent.class)) {
      return "PLAYER";
    }

    if (entity.isPresent(DecoComponent.class)) {
      return "DECO";
    }

    return "DRAW";
  }

  /**
   * Displays a feedback message to the user in the UI overlay.
   *
   * @param message the feedback message to display
   * @param color the color of the feedback message
   */
  public void showModeFeedback(String message, Color color) {
    showFeedback(message, color);
  }

  /**
   * Retrieves the current dungeon level for mode operations.
   *
   * @return an Optional containing the current DungeonLevel, or empty if no level is loaded
   */
  public Optional<DungeonLevel> currentDungeonLevelForModes() {
    return currentDungeonLevel();
  }

  /**
   * Returns the current cursor position snapped to the nearest tile coordinates.
   *
   * @return a Point representing the snapped cursor position in world coordinates
   */
  public Point snappedCursorTileForModes() {
    return snappedCursorTile();
  }

  private Point tileDebugTextPosition(
    int tileX,
    int tileY,
    CameraViewportState.Viewport view) {

    Point screenTopLeft = CameraViewportState.worldToScreen(new Point(tileX, tileY));

    return new Point(
      screenTopLeft.x() + 4,
      screenTopLeft.y() + TileOverlaySizing.tileLabelYOffset(view.tilePx()));
  }

  private Point entityDebugLabelPosition(
    Point pos,
    CameraViewportState.Viewport view) {

    Point screenTopLeft = CameraViewportState.worldToScreen(pos);

    return new Point(
      screenTopLeft.x() + 4,
      screenTopLeft.y() + TileOverlaySizing.bottomAlignedLabelBaseline(view.tilePx(), 6));
  }
}
