package core.platform.litiengine.systems;

import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.components.HealthComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.systems.PositionSync;
import contrib.utils.components.collide.Collider;
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
import core.platform.litiengine.levelEditor.*;
import core.platform.litiengine.render.LitiengineCameraViews;
import core.platform.litiengine.render.LitiengineGraphicsContext;
import core.platform.litiengine.ui.LitiengineLevelEditorOverlay;
import core.platform.litiengine.ui.LitiengineUiOverlayRegistry;
import core.ui.StageHandle;
import core.utils.*;
import core.utils.logging.DungeonLogger;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.*;

/**
 * Minimal LITIENGINE-native level editor.
 *
 * <p>This version keeps the editor lifecycle inside the LITIENGINE backend and ports the first
 * actual editing mode: tile painting.
 */
public final class LitiengineLevelEditorSystem extends System {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(LitiengineLevelEditorSystem.class);

  private static final long FEEDBACK_DURATION_MS = 3000L;

  private static final int TOGGLE_ACTIVE = Keys.F4;

  private static final int MODE_1 = Keys.NUM_1;
  private static final int MODE_2 = Keys.NUM_2;
  private static final int MODE_3 = Keys.NUM_3;
  private static final int MODE_4 = Keys.NUM_4;
  private static final int MODE_5 = Keys.NUM_5;
  private static final int MODE_6 = Keys.NUM_6;
  private static final int MODE_7 = Keys.NUM_7;

  // Shared editor controls, aligned with the former GDX level editor modes.
  private static final int PRIMARY_UP = Keys.E;
  private static final int PRIMARY_DOWN = Keys.Q;
  private static final int SECONDARY_UP = Keys.C;
  private static final int SECONDARY_DOWN = Keys.Z;
  private static final int TERTIARY = Keys.X;
  private static final int QUARTERNARY = Keys.V;

  /**
   * DrawComponent tintColor uses packed RGBA8888.
   *
   * <p>White with ~50% alpha, equivalent to the old preview idea from the legacy deco editor mode.
   */
  private static final int DECO_PREVIEW_TINT = 0xFFFFFF80;
  private static final int DECO_PREVIEW_BLOCKED_TINT = 0xFF6B6B99;
  private static final int DECO_DEFAULT_TINT = -1;
  private static final int DECO_HOVER_TINT = 0xFFFF00FF;
  private static final double DECO_CURSOR_DISTANCE = 0.75d;

  private final LitiengineLevelEditorOverlay overlay = new LitiengineLevelEditorOverlay();

  private boolean active = false;
  private Mode currentMode = Mode.TILES;
  private Map<Integer, InputComponent.InputData> playerCallbacks = null;

  // Decos mode state.
  private int selectedDecoIndex = 0;
  private EditorSnapMode decoSnapMode = EditorSnapMode.OnGrid;
  private Entity decoPreviewEntity = null;
  private Entity heldDecoEntity = null;
  private Entity hoveredDecoEntity = null;

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

  /** Creates the LITIENGINE level editor. */
  public LitiengineLevelEditorSystem() {
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

    Graphics2D g = LitiengineGraphicsContext.get();
    if (g == null) {
      return;
    }

    renderLevelBoundsOutline(g);

    if (debugVisualizationActive) {
      renderDebugVisualization(g);
    }

    if (this.currentMode == Mode.TILES) {
      tilesMode.render(g, deltaSeconds);
    } else if (this.currentMode == Mode.POINTS) {
      pointMode.render(g, deltaSeconds);
    } else if (this.currentMode == Mode.START_TILES) {
      startTilesMode.render(g, deltaSeconds);
    }
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
    }
  }

  private Point snappedCursorTile() {
    Point cursorWorld = Platform.cursor().world();
    return new Point((float) Math.floor(cursorWorld.x()), (float) Math.floor(cursorWorld.y()));
  }

  private Point currentDecoSnapPosition() {
    return decoSnapMode.getPosition(Platform.cursor().world());
  }

  private Deco selectedDeco() {
    Deco[] values = Deco.values();
    return values[Math.floorMod(selectedDecoIndex, values.length)];
  }

  private void ensureDecoPreviewEntity() {
    if (decoPreviewEntity != null) {
      return;
    }

    setupDecoPreviewEntity(snappedCursorTile());
  }

  private void setupDecoPreviewEntity(Point pos) {
    Entity preview = DecoFactory.createDeco(pos, selectedDeco());

    preview
      .fetch(DrawComponent.class)
      .ifPresent(dc -> dc.tintColor(DECO_PREVIEW_TINT));

    preview
      .fetch(CollideComponent.class)
      .ifPresent(cc -> cc.isSolid(false));

    Game.add(preview);
    decoPreviewEntity = preview;
    updateDecoPreviewPosition(pos);
  }

  private void removeDecoPreviewEntity() {
    if (decoPreviewEntity == null) {
      return;
    }

    Game.remove(decoPreviewEntity);
    decoPreviewEntity = null;
  }

  private void previewDecoEntityChanged() {
    Point currentPos = currentDecoSnapPosition();

    if (decoPreviewEntity != null) {
      currentPos =
        decoPreviewEntity
          .fetch(PositionComponent.class)
          .map(PositionComponent::position)
          .orElse(currentPos);
    }

    removeDecoPreviewEntity();
    setupDecoPreviewEntity(currentPos);
  }

  private void updateDecoPreviewPosition(Point snapPos) {
    if (decoPreviewEntity == null) {
      return;
    }

    setEditorDecoPosition(decoPreviewEntity, snapPos);
  }

  private void updateHeldDecoPosition(Point snapPos) {
    if (heldDecoEntity == null) {
      return;
    }

    setEditorDecoPosition(heldDecoEntity, snapPos);
  }

  private void setEditorDecoPosition(Entity entity, Point snapPos) {
    if (entity == null || snapPos == null) {
      return;
    }

    Point actualPos = alignedDecoPosition(entity, snapPos);
    entity.fetch(PositionComponent.class).ifPresent(pc -> pc.position(actualPos));
    PositionSync.syncPosition(entity);
  }

  private Point alignedDecoPosition(Entity entity, Point snapPos) {
    if (entity == null || snapPos == null) {
      return snapPos;
    }

    Vector2 offset =
      entity
        .fetch(CollideComponent.class)
        .map(CollideComponent::collider)
        .map(Collider::offset)
        .orElse(Vector2.ZERO);

    return snapPos.translate(offset.scale(-1));
  }

  private void updateDecoPlacementIndicator() {
    Entity indicatorEntity = heldDecoEntity != null ? heldDecoEntity : decoPreviewEntity;
    if (indicatorEntity == null) {
      return;
    }

    applyDecoPlacementTint(indicatorEntity, isCurrentDecoPlacementBlocked());
  }

  private void updateHoveredDecoIndicator(Point cursorPos) {
    Optional<Entity> hoveredDeco = findHoverableDecoNear(cursorPos);

    boolean sameEntity =
      hoveredDecoEntity != null
        && hoveredDeco.isPresent()
        && hoveredDecoEntity.equals(hoveredDeco.get());

    if (sameEntity) {
      return;
    }

    clearHoveredDecoIndicator();

    if (hoveredDeco.isPresent()) {
      hoveredDecoEntity = hoveredDeco.get();
      applyHoveredDecoTint(hoveredDecoEntity);
    }
  }

  private Optional<Entity> findHoverableDecoNear(Point worldPos) {
    return findPlacedDecoNear(worldPos)
      .filter(entity -> heldDecoEntity == null || !entity.equals(heldDecoEntity));
  }

  private void applyHoveredDecoTint(Entity entity) {
    if (entity == null) {
      return;
    }

    entity.fetch(DrawComponent.class).ifPresent(dc -> dc.tintColor(DECO_HOVER_TINT));
  }

  private void clearHoveredDecoIndicator() {
    if (hoveredDecoEntity == null) {
      return;
    }

    resetDecoTint(hoveredDecoEntity);
    hoveredDecoEntity = null;
  }

  private String currentHoveredDecoName() {
    if (hoveredDecoEntity == null) {
      return "none";
    }

    return hoveredDecoEntity
      .fetch(DecoComponent.class)
      .map(DecoComponent::type)
      .map(Enum::name)
      .orElse("unknown");
  }

  private boolean isCurrentDecoPlacementBlocked() {
    if (!decoSnapMode.checkBlocked()) {
      return false;
    }

    if (heldDecoEntity != null) {
      Point placementPos =
        heldDecoEntity
          .fetch(PositionComponent.class)
          .map(PositionComponent::position)
          .orElse(currentDecoSnapPosition());
      return isDecoPlacementBlocked(heldDecoEntity, placementPos);
    }

    if (decoPreviewEntity != null) {
      return isDecoPlacementBlocked(decoPreviewEntity, currentDecoPreviewPosition());
    }

    return false;
  }

  private void applyDecoPlacementTint(Entity entity, boolean blocked) {
    if (entity == null) {
      return;
    }

    entity
      .fetch(DrawComponent.class)
      .ifPresent(
        dc -> dc.tintColor(blocked ? DECO_PREVIEW_BLOCKED_TINT : DECO_PREVIEW_TINT));
  }

  private void resetDecoTint(Entity entity) {
    if (entity == null) {
      return;
    }

    entity.fetch(DrawComponent.class).ifPresent(dc -> dc.tintColor(DECO_DEFAULT_TINT));
  }

  private void pickupDecoAtCursor() {
    Optional<Entity> placedDeco = findPlacedDecoNear(Platform.cursor().world());
    if (placedDeco.isEmpty()) {
      return;
    }

    clearHoveredDecoIndicator();
    heldDecoEntity = placedDeco.get();
    removeDecoPreviewEntity();

    String pickedName =
      heldDecoEntity
        .fetch(DecoComponent.class)
        .map(DecoComponent::type)
        .map(Enum::name)
        .orElse("Deco");

    updateHeldDecoPosition(snappedCursorTile());
    showFeedback("Picked up deco: " + pickedName, new Color(120, 220, 120));
  }

  private void placeHeldDeco(Point snapPos) {
    if (heldDecoEntity == null) {
      return;
    }

    Point placementPos = alignedDecoPosition(heldDecoEntity, snapPos);

    if (decoSnapMode.checkBlocked() && isDecoPlacementBlocked(heldDecoEntity, placementPos)) {
      showFeedback("Cannot place held deco: target blocked", new Color(255, 210, 120));
      return;
    }

    setEditorDecoPosition(heldDecoEntity, snapPos);
    Entity placedEntity = heldDecoEntity;

    String placedName =
      placedEntity
        .fetch(DecoComponent.class)
        .map(DecoComponent::type)
        .map(Enum::name)
        .orElse("Deco");

    resetDecoTint(placedEntity);
    syncPlacedDecos();

    heldDecoEntity = null;
    ensureDecoPreviewEntity();
    updateDecoPreviewPosition(snapPos);
    updateDecoPlacementIndicator();

    showFeedback("Placed held deco: " + placedName, new Color(120, 220, 120));
  }

  private void pipetteDecoAtCursor() {
    Optional<Entity> placedDeco = findPlacedDecoNear(Platform.cursor().world());
    if (placedDeco.isEmpty()) {
      return;
    }

    Optional<Deco> decoType = placedDeco.get().fetch(DecoComponent.class).map(DecoComponent::type);
    if (decoType.isEmpty()) {
      return;
    }

    Deco[] values = Deco.values();
    for (int i = 0; i < values.length; i++) {
      if (values[i] == decoType.get()) {
        selectedDecoIndex = i;
        if (heldDecoEntity == null) {
          previewDecoEntityChanged();
        }
        showFeedback("Picked deco type: " + decoType.get().name(), Color.WHITE);
        return;
      }
    }
  }

  private void releaseHeldDecoIfNecessary() {
    if (heldDecoEntity == null) {
      return;
    }

    resetDecoTint(heldDecoEntity);
    syncPlacedDecos();
    heldDecoEntity = null;
  }

  private void placeSelectedDeco(Point snapPos) {
    Point placementPos =
      decoPreviewEntity != null ? alignedDecoPosition(decoPreviewEntity, snapPos) : snapPos;

    if (decoSnapMode.checkBlocked()
      && decoPreviewEntity != null
      && isDecoPlacementBlocked(decoPreviewEntity, placementPos)) {
      showFeedback("Cannot place deco: target blocked", new Color(255, 210, 120));
      return;
    }

    Entity placedDeco = DecoFactory.createDeco(placementPos, selectedDeco());
    Game.add(placedDeco);
    syncPlacedDecos();

    showFeedback("Placed deco: " + selectedDeco().name(), new Color(120, 220, 120));
  }

  private void deleteDecoAtCursor() {
    Optional<Entity> placedDeco = findPlacedDecoNear(Platform.cursor().world());
    if (placedDeco.isEmpty()) {
      return;
    }

    String removedName =
      placedDeco.get()
        .fetch(DecoComponent.class)
        .map(DecoComponent::type)
        .map(Enum::name)
        .orElse("Deco");

    if (hoveredDecoEntity != null && hoveredDecoEntity.equals(placedDeco.get())) {
      clearHoveredDecoIndicator();
    }

    Game.remove(placedDeco.get());
    syncPlacedDecos();

    showFeedback("Removed deco: " + removedName, new Color(255, 180, 180));
  }

  private Point currentDecoPreviewPosition() {
    if (decoPreviewEntity == null) {
      return snappedCursorTile();
    }

    return decoPreviewEntity
      .fetch(PositionComponent.class)
      .map(PositionComponent::position)
      .orElse(snappedCursorTile());
  }

  private Optional<Entity> findPlacedDecoNear(Point worldPos) {
    if (worldPos == null) {
      return Optional.empty();
    }

    return Game.levelEntities()
      .filter(entity -> entity.isPresent(DecoComponent.class))
      .filter(entity -> entity.isPresent(PositionComponent.class))
      .filter(entity -> !entity.equals(decoPreviewEntity))
      .filter(
        entity ->
          entity
            .fetch(PositionComponent.class)
            .map(PositionComponent::position)
            .map(pos -> pos.distance(worldPos) <= DECO_CURSOR_DISTANCE)
            .orElse(false))
      .findFirst();
  }

  private boolean isDecoPlacementBlocked(Entity movingDeco, Point placementPos) {
    if (movingDeco == null || placementPos == null) {
      return true;
    }

    Optional<CollideComponent> movingCollide = movingDeco.fetch(CollideComponent.class);
    if (movingCollide.isEmpty()) {
      return findPlacedDecoNear(placementPos).filter(entity -> !entity.equals(movingDeco)).isPresent();
    }

    Point oldColliderPos = movingCollide.get().collider().position();
    movingCollide.get().collider().position(placementPos);

    try {
      return Game.levelEntities()
        .filter(entity -> entity.isPresent(DecoComponent.class))
        .filter(entity -> entity.isPresent(CollideComponent.class))
        .filter(entity -> !entity.equals(movingDeco))
        .filter(entity -> !entity.equals(decoPreviewEntity))
        .map(entity -> entity.fetch(CollideComponent.class).orElseThrow())
        .anyMatch(otherCollide -> movingCollide.get().collider().collide(otherCollide.collider()));
    } finally {
      movingCollide.get().collider().position(oldColliderPos);
    }
  }

  private void syncPlacedDecos() {
    Game.currentLevel()
      .ifPresent(
        level -> {
          List<Tuple<Deco, Point>> placedDecos =
            Game.levelEntities()
              .filter(entity -> entity.isPresent(DecoComponent.class))
              .filter(entity -> entity.isPresent(PositionComponent.class))
              .filter(entity -> !entity.equals(decoPreviewEntity))
              .map(
                entity ->
                  new Tuple<>(
                    entity.fetch(DecoComponent.class).orElseThrow().type(),
                    entity.fetch(PositionComponent.class).orElseThrow().position()))
              .toList();

          level.decorations().clear();
          level.decorations().addAll(placedDecos);
        });
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
      if (!LitiengineUiOverlayRegistry.contains(overlay)) {
        LitiengineUiOverlayRegistry.add(overlay);
      }

      showFeedback("LITIENGINE level editor active", new Color(120, 220, 120));
      syncOverlay();
      LOGGER.info("Activated LITIENGINE level editor.");
      return;
    }

    onModeExit(this.currentMode);

    restorePlayerCallbacks();
    enablePlayerGodMode(false);

    overlay.visible(false);
    LitiengineUiOverlayRegistry.remove(overlay);

    feedbackMessage = "";
    feedbackUntilMs = 0L;

    LOGGER.info("Deactivated LITIENGINE level editor.");
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
    }
  }

  private void switchMode(Mode newMode) {
    if (this.currentMode == newMode) {
      return;
    }

    Mode oldMode = this.currentMode;

    if (oldMode == Mode.DECOS) {
      clearHoveredDecoIndicator();
      releaseHeldDecoIfNecessary();
      removeDecoPreviewEntity();
    }

    this.currentMode = newMode;

    if (this.active && newMode == Mode.DECOS && heldDecoEntity == null) {
      Point snapPos = currentDecoSnapPosition();
      ensureDecoPreviewEntity();
      updateDecoPreviewPosition(snapPos);
      updateDecoPlacementIndicator();
    }

    showFeedback("Switched to " + newMode.displayName() + " mode", Color.WHITE);
  }

  private void syncOverlay() {
    if (!LitiengineUiOverlayRegistry.contains(overlay)) {
      LitiengineUiOverlayRegistry.add(overlay);
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
          ? 320
          : 230);
    }

    overlay.content(
      "LITIENGINE Level Editor",
      buildStatusLines(),
      currentFeedbackMessage(),
      currentFeedbackColor());
  }

  private List<String> buildStatusLines() {
    List<String> lines = new ArrayList<>();
    lines.add("F4: toggle editor");
    lines.add("1-7: switch mode");
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
    } else {
      lines.add("This mode is not ported yet on the LITIENGINE path.");
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
    SAVE_LEVEL("SaveLevel");

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

  private void renderLevelBoundsOutline(Graphics2D g) {
    LitiengineCameraViews.View view = LitiengineCameraViews.get();
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

          int tilePx = view.tilePx();
          int levelWidth = layout[0].length;
          int levelHeight = layout.length;

          int drawX = (int) Math.round(view.offsetX());
          int drawY = (int) Math.round(view.offsetY());
          int drawWidth = levelWidth * tilePx;
          int drawHeight = levelHeight * tilePx;

          Graphics2D g2 = (Graphics2D) g.create();
          try {
            g2.setColor(LEVEL_BOUNDS_OUTLINE_COLOR);
            g2.setStroke(new BasicStroke(LEVEL_BOUNDS_OUTLINE_STROKE));
            g2.drawRect(drawX, drawY, Math.max(0, drawWidth - 1), Math.max(0, drawHeight - 1));
          } finally {
            g2.dispose();
          }
        });
  }

  private void onModeEnter(Mode mode) {
    switch (Objects.requireNonNull(mode)) {
      case DECOS -> decoMode.onEnter();
      case POINTS -> pointMode.onEnter();
      case START_TILES -> startTilesMode.onEnter();
      default -> {
        // no-op for now
      }
    }
  }

  private void onModeExit(Mode mode) {
    switch (mode) {
      case DECOS -> decoMode.onExit();
      case POINTS -> pointMode.onExit();
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

  private void renderDebugVisualization(Graphics2D g) {
    LitiengineCameraViews.View view = LitiengineCameraViews.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    currentDungeonLevel()
      .ifPresent(
        level -> {
          Graphics2D g2 = (Graphics2D) g.create();
          try {
            renderDebugTiles(g2, level, view);
            renderDebugEntities(g2, level.layout().length, view);
          } finally {
            g2.dispose();
          }
        });
  }

  private void renderDebugTiles(Graphics2D g, DungeonLevel level, LitiengineCameraViews.View view) {
    Tile[][] layout = level.layout();
    int levelHeight = layout.length;
    int tilePx = view.tilePx();

    for (int y = 0; y < layout.length; y++) {
      for (int x = 0; x < layout[y].length; x++) {
        Tile tile = layout[y][x];
        LevelElement element = tile.levelElement();

        int drawX = (int) Math.round(x * tilePx + view.offsetX());
        int drawY = (int) Math.round((levelHeight - 1 - y) * tilePx + view.offsetY());

        if (!element.value()) {
          g.setColor(DEBUG_BLOCKED_TILE_FILL_COLOR);
          g.fillRect(drawX, drawY, tilePx, tilePx);
        } else if (element.canSeeThrough()) {
          g.setColor(DEBUG_SEE_THROUGH_TILE_FILL_COLOR);
          g.fillRect(drawX, drawY, tilePx, tilePx);
        }

        g.setColor(DEBUG_LEVEL_TILE_OUTLINE_COLOR);
        g.drawRect(drawX, drawY, Math.max(0, tilePx - 1), Math.max(0, tilePx - 1));

        if (tilePx >= DEBUG_TEXT_MIN_TILE_PX) {
          g.setColor(DEBUG_COORD_TEXT_COLOR);
          g.drawString(x + "," + y, drawX + 4, drawY + Math.max(14, tilePx / 2));
        }
      }
    }
  }

  private void renderDebugEntities(
    Graphics2D g, int levelHeight, LitiengineCameraViews.View view) {

    int tilePx = view.tilePx();

    g.setStroke(new BasicStroke(DEBUG_ENTITY_STROKE));

    Game.levelEntities(Set.of(PositionComponent.class, DrawComponent.class))
      .forEach(
        entity -> {
          PositionComponent pc = entity.fetch(PositionComponent.class).orElse(null);
          if (pc == null) {
            return;
          }

          Point pos = pc.position();
          int drawX = (int) Math.round(pos.x() * tilePx + view.offsetX());
          int drawY =
            (int) Math.round((levelHeight - 1 - pos.y()) * tilePx + view.offsetY());

          g.setColor(debugEntityColor(entity));
          g.drawRect(
            drawX + DEBUG_ENTITY_INSET_PX,
            drawY + DEBUG_ENTITY_INSET_PX,
            Math.max(0, tilePx - 1 - DEBUG_ENTITY_INSET_PX * 2),
            Math.max(0, tilePx - 1 - DEBUG_ENTITY_INSET_PX * 2));

          if (tilePx >= DEBUG_TEXT_MIN_TILE_PX) {
            g.drawString(debugEntityLabel(entity), drawX + 4, drawY + tilePx - 6);
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

  public void showModeFeedback(String message, Color color) {
    showFeedback(message, color);
  }

  public java.util.Optional<core.level.DungeonLevel> currentDungeonLevelForModes() {
    return currentDungeonLevel();
  }

  public Point snappedCursorTileForModes() {
    return snappedCursorTile();
  }

  public void changeSelectedDecoByForModes(int delta) {
    selectedDecoIndex = Math.floorMod(selectedDecoIndex + delta, Deco.values().length);
  }

  public boolean isHoldingDecoForModes() {
    return heldDecoEntity != null;
  }

  public void cycleDecoSnapModeForModes() {
    decoSnapMode = decoSnapMode.nextMode();
  }

  public String decoSnapModeDisplayNameForModes() {
    return decoSnapMode.displayName();
  }

  public Deco selectedDecoForModes() {
    return selectedDeco();
  }

  public int selectedDecoDisplayIndexForModes() {
    return Math.floorMod(selectedDecoIndex, Deco.values().length) + 1;
  }

  public int availableDecoCountForModes() {
    return Deco.values().length;
  }

  public Point currentDecoSnapPositionForModes() {
    return currentDecoSnapPosition();
  }

  public void previewDecoEntityChangedForModes() {
    previewDecoEntityChanged();
  }

  public void pipetteDecoAtCursorForModes() {
    pipetteDecoAtCursor();
  }

  public void pickupDecoAtCursorForModes() {
    pickupDecoAtCursor();
  }

  public void deleteDecoAtCursorForModes() {
    deleteDecoAtCursor();
  }

  public void placeHeldDecoForModes(Point snapPos) {
    placeHeldDeco(snapPos);
  }

  public void placeSelectedDecoForModes(Point snapPos) {
    placeSelectedDeco(snapPos);
  }

  public void updateHeldDecoPlacementForModes(Point snapPos) {
    clearHoveredDecoIndicator();
    updateHeldDecoPosition(snapPos);
    updateDecoPlacementIndicator();
  }

  public void refreshDecoPreviewForModes(Point snapPos) {
    ensureDecoPreviewEntity();
    updateDecoPreviewPosition(snapPos);
    updateDecoPlacementIndicator();
  }

  public void updateHoveredDecoForModes(Point cursorPos) {
    updateHoveredDecoIndicator(cursorPos);
  }

  public boolean isCurrentDecoPlacementBlockedForModes() {
    return isCurrentDecoPlacementBlocked();
  }

  public String currentHoveredDecoNameForModes() {
    return currentHoveredDecoName();
  }

  public void clearDecoEditingArtifactsForModes() {
    clearHoveredDecoIndicator();
    releaseHeldDecoIfNecessary();
    removeDecoPreviewEntity();
  }
}
