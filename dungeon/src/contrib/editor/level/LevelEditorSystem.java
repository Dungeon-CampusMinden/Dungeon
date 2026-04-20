package contrib.editor.level;

import contrib.components.DecoComponent;
import contrib.components.HealthComponent;
import contrib.editor.level.mode.DecoColliderMode;
import contrib.editor.level.mode.DecoMode;
import contrib.editor.level.mode.LevelBoundsMode;
import contrib.editor.level.mode.LevelEditorMode;
import contrib.editor.level.mode.PointMode;
import contrib.editor.level.mode.SaveMode;
import contrib.editor.level.mode.ShiftLevelMode;
import contrib.editor.level.mode.StartTilesMode;
import contrib.editor.level.mode.TilesMode;
import core.Entity;
import core.Game;
import core.System;
import core.camera.CameraViewportState;
import core.components.DrawComponent;
import core.components.InputComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.game.render.RenderContext;
import core.input.Keys;
import core.level.DungeonLevel;
import core.level.Tile;
import core.platform.Platform;
import core.render.AnimationFrameImages;
import core.render.effects.ImageEffects;
import core.ui.overlay.OverlayManager;
import core.utils.InputManager;
import core.utils.Point;
import core.utils.Time;
import core.utils.logging.DungeonLogger;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The LevelEditorSystem class provides a toolset for editing game levels
 * within the game's runtime environment.
 *
 * <p>It includes features for interacting with tiles, entities, and dungeon levels, as well as providing visual
 * feedback and debug overlays for level editing modes.
 *
 * <p>This system manages various modes for editing level elements, toggling
 * debug visuals, and handling user interaction. It also supports rendering
 * overlays and feedback for user actions.
 */
public final class LevelEditorSystem extends System {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LevelEditorSystem.class);

  private static final long FEEDBACK_DURATION_MS = 3000L;

  private static final int TOGGLE_ACTIVE = Keys.F4;
  private static final int TOGGLE_LAYER_DEBUG = Keys.SPACE;

  private static final int MODE_1 = Keys.NUM_1;
  private static final int MODE_2 = Keys.NUM_2;
  private static final int MODE_3 = Keys.NUM_3;
  private static final int MODE_4 = Keys.NUM_4;
  private static final int MODE_5 = Keys.NUM_5;
  private static final int MODE_6 = Keys.NUM_6;
  private static final int MODE_7 = Keys.NUM_7;
  private static final int MODE_8 = Keys.NUM_8;

  private static final Color LEVEL_BOUNDS_OUTLINE_COLOR = new Color(0, 255, 0, 77);

  private static final Color DEBUG_LEVEL_TILE_OUTLINE_COLOR = new Color(80, 140, 255, 150);
  private static final Color DEBUG_PLAYER_ENTITY_COLOR = Color.RED;
  private static final Color DEBUG_DECO_ENTITY_COLOR = Color.GREEN;
  private static final Color DEBUG_NORMAL_ENTITY_COLOR = Color.WHITE;

  private static final int DEBUG_ENTITY_INSET_PX = 2;

  private final LevelEditorOverlay overlay = new LevelEditorOverlay();

  private final LevelEditorMode tilesMode = new TilesMode(this);
  private final LevelEditorMode decoMode = new DecoMode(this);
  private final LevelEditorMode pointMode = new PointMode(this);
  private final LevelEditorMode levelBoundsMode = new LevelBoundsMode(this);
  private final LevelEditorMode shiftLevelMode = new ShiftLevelMode(this);
  private final LevelEditorMode startTilesMode = new StartTilesMode(this);
  private final LevelEditorMode saveMode = new SaveMode(this);
  private final LevelEditorMode decoColliderMode = new DecoColliderMode(this);

  private boolean active = false;
  private boolean internalStopped = false;
  private boolean layerDebugActive = false;

  private Mode currentMode = Mode.TILES;
  private Map<Integer, InputComponent.InputData> playerCallbacks = null;

  private String feedbackMessage = "";
  private Color feedbackColor = Color.WHITE;
  private long feedbackUntilMs = 0L;

  /**
   * Returns whether the editor is currently active.
   *
   * @return true if active
   */
  public boolean active() {
    return active;
  }

  /**
   * Activates or deactivates the level editor.
   *
   * @param active new editor state
   */
  public void active(boolean active) {
    if (this.active == active) {
      return;
    }

    this.active = active;

    Entity player = Game.player().orElse(null);
    if (player == null) {
      if (!active) {
        detachOverlay();
      }
      return;
    }

    if (active) {
      player
        .fetch(InputComponent.class)
        .ifPresent(
          pc -> {
            playerCallbacks = pc.callbacks();
            pc.removeCallback(LevelEditorMode.PRIMARY_UP);
            pc.removeCallback(LevelEditorMode.PRIMARY_DOWN);
            pc.removeCallback(LevelEditorMode.SECONDARY_UP);
            pc.removeCallback(LevelEditorMode.SECONDARY_DOWN);
            pc.removeCallback(LevelEditorMode.TERTIARY);
            pc.removeCallback(core.input.MouseButtons.LEFT);
            pc.removeCallback(core.input.MouseButtons.RIGHT);
          });

      player.fetch(HealthComponent.class).ifPresent(hc -> hc.godMode(true));

      attachOverlay();
      onModeEnter(currentMode);
      updateOverlay();
      LOGGER.info("Level editor activated.");
      return;
    }

    if (playerCallbacks != null) {
      player
        .fetch(InputComponent.class)
        .ifPresent(
          pc ->
            playerCallbacks.forEach(
              (key, value) ->
                pc.registerCallback(
                  key, value.callback(), value.repeat(), value.pauseable())));
      playerCallbacks = null;
    }

    player.fetch(HealthComponent.class).ifPresent(hc -> hc.godMode(false));

    onModeExit(currentMode);
    detachOverlay();
    LOGGER.info("Level editor deactivated.");
  }

  @Override
  public void execute() {
    if (InputManager.isKeyJustPressed(TOGGLE_ACTIVE)) {
      active(!active);
    }

    if (!active) {
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

    Mode selectedMode = selectedModeByHotkey().orElse(currentMode);
    boolean modeChanged = selectedMode != currentMode;

    if (modeChanged) {
      onModeExit(currentMode);
      currentMode = selectedMode;
      onModeEnter(currentMode);
    }

    if (!internalStopped || modeChanged) {
      currentModeInstance().doExecute();
    }

    updateOverlay();
  }

  @Override
  public void render(float deltaSeconds) {
    if (!active) {
      return;
    }

    Graphics2D g = RenderContext.get();
    if (g == null) {
      return;
    }

    renderLevelBoundsOutline(g);

    if (layerDebugActive) {
      renderLayerDebug(g);
    }

    currentModeInstance().render(g, deltaSeconds);
    updateOverlay();
  }

  @Override
  public void stop() {
    this.internalStopped = true;
  }

  @Override
  public void run() {
    this.internalStopped = false;
  }

  /**
   * Displays a feedback message in the editor overlay.
   *
   * @param message message text
   * @param color message color
   */
  public void showModeFeedback(String message, Color color) {
    showFeedback(message, color);
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

  private void attachOverlay() {
    overlay.visible(true);

    if (!OverlayManager.contains(overlay)) {
      OverlayManager.add(overlay);
    }

    OverlayManager.toFront(overlay);
  }

  private void detachOverlay() {
    overlay.visible(false);
    OverlayManager.remove(overlay);
  }

  private Optional<Mode> selectedModeByHotkey() {
    if (InputManager.isKeyJustPressed(MODE_1)) {
      return Optional.of(Mode.getMode(0));
    }
    if (InputManager.isKeyJustPressed(MODE_2)) {
      return Optional.of(Mode.getMode(1));
    }
    if (InputManager.isKeyJustPressed(MODE_3)) {

      return Optional.of(Mode.getMode(2));
    }
    if (InputManager.isKeyJustPressed(MODE_4)) {
      return Optional.of(Mode.getMode(3));
    }
    if (InputManager.isKeyJustPressed(MODE_5)) {
      return Optional.of(Mode.getMode(4));
    }
    if (InputManager.isKeyJustPressed(MODE_6)) {
      return Optional.of(Mode.getMode(5));
    }
    if (InputManager.isKeyJustPressed(MODE_7)) {
      return Optional.of(Mode.getMode(6));
    }
    if (InputManager.isKeyJustPressed(MODE_8)) {
      return Optional.of(Mode.getMode(7));
    }

    return Optional.empty();
  }

  private LevelEditorMode currentModeInstance() {
    return switch (currentMode) {
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

  private void updateOverlay() {
    if (!active) {
      return;
    }

    overlay.content(
      "",
      buildStatusLines(),
      currentFeedbackMessage(),
      currentFeedbackColor());
  }

  private List<String> buildStatusLines() {
    List<String> lines = new ArrayList<>();
    lines.add("Level Editor v2 | Modes: " + modeSelectionText());
    lines.add("( SPACE to toggle layer debug shader [" + layerDebugActive + "] )");
    lines.add("");
    lines.addAll(currentModeInstance().getFullStatusLines());
    return lines;
  }

  private String modeSelectionText() {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < Mode.values().length; i++) {
      if (i > 0) {
        sb.append(" | ");
      }

      if (Mode.values()[i] == currentMode) {
        sb.append("[").append(i + 1).append("]");
      } else {
        sb.append(i + 1);
      }
    }

    return sb.toString();
  }

  private void showFeedback(String message, Color color) {
    this.feedbackMessage = message == null ? "" : message;
    this.feedbackColor = color == null ? Color.WHITE : color;
    this.feedbackUntilMs = Time.nowMs() + FEEDBACK_DURATION_MS;

    if (Color.RED.equals(this.feedbackColor)) {
      LOGGER.error(this.feedbackMessage);
    } else if (Color.YELLOW.equals(this.feedbackColor)) {
      LOGGER.warn(this.feedbackMessage);
    } else {
      LOGGER.info(this.feedbackMessage);
    }
  }

  private String currentFeedbackMessage() {
    return Time.nowMs() <= feedbackUntilMs ? feedbackMessage : "";
  }

  private Color currentFeedbackColor() {
    return Time.nowMs() <= feedbackUntilMs ? feedbackColor : Color.WHITE;
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

  private void renderLevelBoundsOutline(Graphics2D g) {
    CameraViewportState.Viewport view = CameraViewportState.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    currentDungeonLevel()
      .ifPresent(level -> {
        Tile[][] layout = level.layout();
        if (layout.length == 0 || layout[0].length == 0) {
          return;
        }

        int levelWidth = layout[0].length;
        int levelHeight = layout.length;

        drawWorldRectangleOutline(
          g,
          0f,
          0f,
          levelWidth,
          levelHeight,
          LEVEL_BOUNDS_OUTLINE_COLOR);
      });
  }

  private void renderLayerDebug(Graphics2D g) {
    CameraViewportState.Viewport view = CameraViewportState.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    renderLayerDebugTiles(g);
    renderLayerDebugEntities(g, view);
  }

  private void renderLayerDebugTiles(Graphics2D g) {
    currentDungeonLevel()
      .ifPresent(level -> {
        Tile[][] layout = level.layout();
        for (int y = 0; y < layout.length; y++) {
          for (int x = 0; x < layout[y].length; x++) {
            drawWorldRectangleOutline(
              g,
              x,
              y,
              1.0f,
              1.0f,
              DEBUG_LEVEL_TILE_OUTLINE_COLOR);
          }
        }
      });
  }

  private void renderLayerDebugEntities(Graphics2D g, CameraViewportState.Viewport view) {
    Game.levelEntities(Set.of(PositionComponent.class, DrawComponent.class))
      .forEach(entity -> {
        PositionComponent pc = entity.fetch(PositionComponent.class).orElse(null);
        DrawComponent dc = entity.fetch(DrawComponent.class).orElse(null);

        if (pc == null || dc == null || !dc.isVisible()) {
          return;
        }

        if (!tryDrawLayerDebugEntitySpriteOutline(g, entity, pc, dc, view)) {
          drawLayerDebugEntityFallbackRectangle(g, entity, pc, view);
        }
      });
  }

  private boolean tryDrawLayerDebugEntitySpriteOutline(
    Graphics2D g,
    Entity entity,
    PositionComponent pc,
    DrawComponent dc,
    CameraViewportState.Viewport view) {

    final core.utils.components.draw.animation.AnimationFrame frame;
    try {
      frame = dc.stateMachine().getFrame();
    } catch (Exception ignored) {
      return false;
    }

    BufferedImage sprite = AnimationFrameImages.toImage(frame);
    if (sprite == null || sprite.getWidth() <= 0 || sprite.getHeight() <= 0) {
      return false;
    }

    int tilePx = view.tilePx();

    int wPx = tilePx;
    int hPx = tilePx;

    try {
      float wWorld = dc.stateMachine().getWidth();
      float hWorld = dc.stateMachine().getHeight();

      if (wWorld > 0f) {
        wPx = Math.max(1, Math.round(wWorld * tilePx));
      }

      if (hWorld > 0f) {
        hPx = Math.max(1, Math.round(hWorld * tilePx));
      }
    } catch (Exception ignored) {
      // keep default tile-sized fallback dimensions
    }

    Point screenOrigin = CameraViewportState.worldToScreen(pc.position());

    int drawX = Math.round(screenOrigin.x() + (tilePx - wPx) / 2f);
    int drawY = Math.round(screenOrigin.y() + tilePx - hPx);

    ImageEffects.drawOutlinedSprite(
      g,
      sprite,
      drawX,
      drawY,
      wPx,
      hPx,
      debugEntityColor(entity),
      Math.max(1, DEBUG_ENTITY_INSET_PX));

    return true;
  }

  private void drawLayerDebugEntityFallbackRectangle(
    Graphics2D g,
    Entity entity,
    PositionComponent pc,
    CameraViewportState.Viewport view) {

    float insetWorld = DEBUG_ENTITY_INSET_PX / (float) view.tilePx();
    float sizeWorld = Math.max(0.05f, 1.0f - (2f * insetWorld));

    Point pos = pc.position();

    drawWorldRectangleOutline(
      g,
      pos.x() + insetWorld,
      pos.y() + insetWorld,
      sizeWorld,
      sizeWorld,
      debugEntityColor(entity));
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

  private void onModeEnter(Mode mode) {
    switch (Objects.requireNonNull(mode)) {
      case TILES -> tilesMode.onEnter();
      case DECOS -> decoMode.onEnter();
      case POINTS -> pointMode.onEnter();
      case LEVEL_BOUNDS -> levelBoundsMode.onEnter();
      case SHIFT_LEVEL -> shiftLevelMode.onEnter();
      case START_TILES -> startTilesMode.onEnter();
      case SAVE_LEVEL -> saveMode.onEnter();
      case DECO_COLLIDER -> decoColliderMode.onEnter();
    }
  }

  private void onModeExit(Mode mode) {
    switch (Objects.requireNonNull(mode)) {
      case TILES -> tilesMode.onExit();
      case DECOS -> decoMode.onExit();
      case POINTS -> pointMode.onExit();
      case LEVEL_BOUNDS -> levelBoundsMode.onExit();
      case SHIFT_LEVEL -> shiftLevelMode.onExit();
      case START_TILES -> startTilesMode.onExit();
      case SAVE_LEVEL -> saveMode.onExit();
      case DECO_COLLIDER -> decoColliderMode.onExit();
    }
  }

  private enum Mode {
    TILES(),
    DECOS(),
    POINTS(),
    LEVEL_BOUNDS(),
    SHIFT_LEVEL(),
    START_TILES(),
    SAVE_LEVEL(),
    DECO_COLLIDER();

    public static Mode getMode(int number) {
      if (number < 0 || number >= values().length) {
        throw new IllegalArgumentException("Invalid mode number: " + number);
      }
      return values()[number];
    }
  }

  private void drawWorldRectangleOutline(
    Graphics2D g, float worldX, float worldY, float worldWidth, float worldHeight, Color color) {
    CameraViewportState.Viewport view = CameraViewportState.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    Point screenTopLeft =
      CameraViewportState.worldToScreen(new Point(worldX, worldY + worldHeight - 1f));

    int px = Math.round(screenTopLeft.x());
    int py = Math.round(screenTopLeft.y());
    int pw = CameraViewportState.worldLengthToScreen(worldWidth);
    int ph = CameraViewportState.worldLengthToScreen(worldHeight);

    Color oldColor = g.getColor();
    g.setColor(color);
    g.drawRect(px, py, pw, ph);
    g.setColor(oldColor);
  }
}
