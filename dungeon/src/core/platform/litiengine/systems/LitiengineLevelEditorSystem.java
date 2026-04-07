package core.platform.litiengine.systems;

import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.components.HealthComponent;
import contrib.components.UIComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
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
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.platform.Platform;
import core.platform.litiengine.render.LitiengineCameraViews;
import core.platform.litiengine.render.LitiengineGraphicsContext;
import core.platform.litiengine.ui.LitiengineLevelEditorOverlay;
import core.platform.litiengine.ui.LitiengineUiOverlayRegistry;
import core.systems.LevelSystem;
import core.ui.StageHandle;
import core.utils.*;
import core.utils.logging.DungeonLogger;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.*;
import java.util.function.Consumer;

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

  private static final int MAX_BRUSH_SIZE = 7;
  private static final Color BRUSH_PREVIEW_FILL = new Color(255, 255, 255, 36);
  private static final Color BRUSH_PREVIEW_BORDER = new Color(255, 255, 255, 175);
  private static final int BRUSH_PREVIEW_INSET_PX = 1;

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

  // Tiles mode state.
  private int selectedTileIndexL = 1;
  private int selectedTileIndexR = 2;
  private int brushSize = 1;

  // Decos mode state.
  private int selectedDecoIndex = 0;
  private SnapMode decoSnapMode = SnapMode.OnGrid;
  private Entity decoPreviewEntity = null;
  private Entity heldDecoEntity = null;
  private Entity hoveredDecoEntity = null;

  private String feedbackMessage = "";
  private Color feedbackColor = Color.WHITE;
  private long feedbackUntilMs = 0L;

  // Points mode state.
  private SnapMode pointSnapMode = SnapMode.OnGrid;
  private String heldPointName = null;

  private static final Color POINT_MARKER_COLOR = new Color(255, 196, 77, 220);
  private static final Color HELD_POINT_MARKER_COLOR = new Color(120, 220, 120, 230);
  private static final Color POINT_LABEL_COLOR = Color.WHITE;
  private static final int POINT_MARKER_MIN_PX = 8;
  private static final int POINT_MARKER_MAX_PX = 18;

  private static final Color LEVEL_BOUNDS_OUTLINE_COLOR = new Color(0, 255, 0, 77);
  private static final float LEVEL_BOUNDS_OUTLINE_STROKE = 2.0f;

  // Start tiles mode state.
  private int currentStartTileIndex = 0;

  private static final Color[] START_TILE_COLORS = {
    Color.GREEN,
    Color.BLUE,
    Color.YELLOW,
    Color.CYAN,
    Color.MAGENTA,
    Color.ORANGE,
    Color.PINK,
    new Color(50, 205, 50),   // lime
    new Color(135, 206, 235), // sky
    new Color(250, 128, 114)  // salmon
  };

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
      renderTileBrushPreview(g);
    } else if (this.currentMode == Mode.POINTS) {
      renderPointMarkers(g);
    } else if (this.currentMode == Mode.START_TILES) {
      renderStartTileMarkers(g);
    }
  }
  /** Returns whether the editor is currently active. */
  public boolean active() {
    return this.active;
  }

  private void executeCurrentMode() {
    switch (currentMode) {
      case TILES -> executeTilesMode();
      case DECOS -> executeDecosMode();
      case POINTS -> executePointsMode();
      case LEVEL_BOUNDS -> executeLevelBoundsMode();
      case SHIFT_LEVEL -> executeShiftLevelMode();
      case START_TILES -> executeStartTilesMode();
      case SAVE_LEVEL -> executeSaveLevelMode();
    }
  }

  private void executeTilesMode() {
    if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      if (InputManager.isButtonPressed(MouseButtons.RIGHT)) {
        selectedTileIndexR -= 1;
      } else {
        selectedTileIndexL -= 1;
      }
    } else if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      if (InputManager.isButtonPressed(MouseButtons.RIGHT)) {
        selectedTileIndexR += 1;
      } else {
        selectedTileIndexL += 1;
      }
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      brushSize = Math.min(MAX_BRUSH_SIZE, brushSize + 1);
    } else if (InputManager.isKeyJustPressed(SECONDARY_DOWN)) {
      brushSize = Math.max(1, brushSize - 1);
    }

    if (InputManager.isKeyJustPressed(QUARTERNARY)) {
      tileElementAtCursor()
        .ifPresent(
          element -> {
            selectedTileIndexL = element.ordinal();
            showFeedback("Picked tile " + element.name() + " for left paint", Color.WHITE);
          });
    }

    if (InputManager.isButtonPressed(MouseButtons.LEFT)) {
      applyBrush(selectedLeftElement(), brushSize);
    } else if (InputManager.isButtonPressed(MouseButtons.RIGHT)) {
      applyBrush(selectedRightElement(), 1);
    } else if (InputManager.isKeyPressed(TERTIARY)) {
      applyBrush(LevelElement.SKIP, 1);
    }
  }

  private void executeDecosMode() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      selectedDecoIndex = Math.floorMod(selectedDecoIndex + 1, Deco.values().length);
      if (heldDecoEntity == null) {
        previewDecoEntityChanged();
      }
      showFeedback("Selected deco: " + selectedDeco().name(), Color.WHITE);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      selectedDecoIndex = Math.floorMod(selectedDecoIndex - 1, Deco.values().length);
      if (heldDecoEntity == null) {
        previewDecoEntityChanged();
      }
      showFeedback("Selected deco: " + selectedDeco().name(), Color.WHITE);
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      decoSnapMode = decoSnapMode.nextMode();
      if (heldDecoEntity == null) {
        previewDecoEntityChanged();
      }
      showFeedback("Snap mode: " + decoSnapMode.displayName(), Color.WHITE);
    }

    Point cursorPos = Platform.cursor().world();
    Point snapPos = currentDecoSnapPosition();

    if (InputManager.isKeyJustPressed(QUARTERNARY)) {
      pipetteDecoAtCursor();
    } else if (InputManager.isButtonJustPressed(MouseButtons.RIGHT) && heldDecoEntity == null) {
      pickupDecoAtCursor();
    } else if (InputManager.isKeyJustPressed(TERTIARY) && heldDecoEntity == null) {
      deleteDecoAtCursor();
    } else if (InputManager.isButtonJustPressed(MouseButtons.LEFT)) {
      if (heldDecoEntity != null) {
        placeHeldDeco(snapPos);
      } else {
        placeSelectedDeco(snapPos);
      }
    }

    if (heldDecoEntity != null) {
      clearHoveredDecoIndicator();
      updateHeldDecoPosition(snapPos);
      updateDecoPlacementIndicator();
      return;
    }

    ensureDecoPreviewEntity();
    updateDecoPreviewPosition(snapPos);
    updateDecoPlacementIndicator();
    updateHoveredDecoIndicator(cursorPos);
  }

  private void executeSaveLevelMode() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      core.level.loader.DungeonSaver.saveCurrentDungeon();
      showFeedback("Exported level to clipboard!", new Color(120, 220, 120));
    }
  }

  private void applyBrush(LevelElement element, int targetBrushSize) {
    LevelSystem.level()
      .ifPresent(
        level ->
          forEachBrushTile(
            targetBrushSize,
            targetPos ->
              level.tileAt(targetPos)
                .ifPresent(tile -> level.changeTileElementType(tile, element))));
  }

  private void forEachBrushTile(int targetBrushSize, Consumer<Point> consumer) {
    if (consumer == null) {
      return;
    }

    int normalizedBrushSize = Math.max(1, targetBrushSize);
    Point cursorPos = snappedCursorTile();

    for (int dx = -normalizedBrushSize + 1; dx < normalizedBrushSize; dx++) {
      for (int dy = -normalizedBrushSize + 1; dy < normalizedBrushSize; dy++) {
        if (Math.abs(dx) + Math.abs(dy) >= normalizedBrushSize) {
          continue;
        }

        consumer.accept(cursorPos.translate(Vector2.of(dx, dy)));
      }
    }
  }

  private void renderTileBrushPreview(Graphics2D g) {
    LitiengineCameraViews.View view = LitiengineCameraViews.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    int levelHeight =
      view.levelHeight() > 0
        ? view.levelHeight()
        : Game.currentLevel().map(level -> level.layout().length).orElse(0);

    int previewBrushSize = currentPreviewBrushSize();

    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setStroke(new BasicStroke(Math.max(1f, view.tilePx() / 16f)));

      forEachBrushTile(previewBrushSize, tilePos -> drawPreviewTile(g2, tilePos, view, levelHeight));
    } finally {
      g2.dispose();
    }
  }

  private int currentPreviewBrushSize() {
    if (InputManager.isButtonPressed(MouseButtons.RIGHT) || InputManager.isKeyPressed(TERTIARY)) {
      return 1;
    }

    return this.brushSize;
  }

  private void drawPreviewTile(
    Graphics2D g, Point tilePos, LitiengineCameraViews.View view, int levelHeight) {
    int tilePx = view.tilePx();

    int screenX = (int) Math.round(tilePos.x() * tilePx + view.offsetX());

    float screenTileY =
      levelHeight > 0 ? (levelHeight - 1 - tilePos.y()) * tilePx : tilePos.y() * tilePx;
    int screenY = (int) Math.round(screenTileY + view.offsetY());

    int inset = Math.clamp(tilePx / 4, 0, BRUSH_PREVIEW_INSET_PX);
    int size = Math.max(1, tilePx - 2 * inset);

    g.setColor(BRUSH_PREVIEW_FILL);
    g.fillRect(screenX + inset, screenY + inset, size, size);

    g.setColor(BRUSH_PREVIEW_BORDER);
    g.drawRect(screenX + inset, screenY + inset, size, size);
  }

  private Point snappedCursorTile() {
    Point cursorWorld = Platform.cursor().world();
    return new Point((float) Math.floor(cursorWorld.x()), (float) Math.floor(cursorWorld.y()));
  }

  private Point currentDecoSnapPosition() {
    return decoSnapMode.getPosition(Platform.cursor().world());
  }

  private java.util.Optional<LevelElement> tileElementAtCursor() {
    Point cursorPos = snappedCursorTile();
    return LevelSystem.level().flatMap(level -> level.tileAt(cursorPos)).map(Tile::levelElement);
  }

  private LevelElement selectedLeftElement() {
    LevelElement[] values = LevelElement.values();
    return values[Math.floorMod(selectedTileIndexL, values.length)];
  }

  private LevelElement selectedRightElement() {
    LevelElement[] values = LevelElement.values();
    return values[Math.floorMod(selectedTileIndexR, values.length)];
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

      if (this.currentMode == Mode.DECOS) {
        Point snapPos = currentDecoSnapPosition();
        ensureDecoPreviewEntity();
        updateDecoPreviewPosition(snapPos);
        updateDecoPlacementIndicator();
      }

      showFeedback("LITIENGINE level editor active", new Color(120, 220, 120));
      syncOverlay();
      LOGGER.info("Activated LITIENGINE level editor.");
      return;
    }

    onModeExit(this.currentMode);

    restorePlayerCallbacks();
    enablePlayerGodMode(false);
    clearHoveredDecoIndicator();
    releaseHeldDecoIfNecessary();
    removeDecoPreviewEntity();

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
      lines.addAll(buildTilesModeLines());
    } else if (currentMode == Mode.DECOS) {
      lines.addAll(buildDecosModeLines());
    } else if (currentMode == Mode.POINTS) {
      lines.addAll(buildPointsModeLines());
    } else if (currentMode == Mode.LEVEL_BOUNDS) {
      lines.addAll(buildLevelBoundsModeLines());
    } else if (currentMode == Mode.SHIFT_LEVEL) {
      lines.addAll(buildShiftLevelModeLines());
    } else if (currentMode == Mode.START_TILES) {
      lines.addAll(buildStartTilesModeLines());
    } else if (currentMode == Mode.SAVE_LEVEL) {
      lines.add("E: save level to clipboard");
      lines.add("Uses DungeonSaver serialization of the current DungeonLevel.");
    } else {
      lines.add("This mode is not ported yet on the LITIENGINE path.");
    }

    return lines;
  }

  private List<String> buildTilesModeLines() {
    Point cursor = snappedCursorTile();

    List<String> lines = new ArrayList<>();
    lines.add("Cursor tile: (" + (int) cursor.x() + ", " + (int) cursor.y() + ")");
    lines.add("Left paint: " + selectedLeftElement().name());
    lines.add("Right paint: " + selectedRightElement().name());
    lines.add("Brush size: " + brushSize);
    lines.add("E/Q: next/prev left tile");
    lines.add("Hold RMB + E/Q: next/prev right tile");
    lines.add("C/Z: brush + / -");
    lines.add("LMB: paint | RMB: alt paint | X: erase to SKIP | V: pipette to left paint");
    return lines;
  }

  private List<String> buildDecosModeLines() {
    Point cursor = snappedCursorTile();
    Deco currentDeco = selectedDeco();

    List<String> lines = new ArrayList<>();
    lines.add("Cursor tile: (" + (int) cursor.x() + ", " + (int) cursor.y() + ")");
    lines.add(
      "Current deco: "
        + (Math.floorMod(selectedDecoIndex, Deco.values().length) + 1)
        + "/"
        + Deco.values().length
        + " ("
        + currentDeco.name()
        + ")");
    lines.add("Snap mode: " + decoSnapMode.displayName());
    lines.add("Placement: " + (isCurrentDecoPlacementBlocked() ? "blocked" : "valid"));
    lines.add("Hover: " + currentHoveredDecoName());
    lines.add("Preview tint: white = valid, red = blocked");
    lines.add("C: next snap mode");
    lines.add("E/Q: next/prev deco");
    lines.add("LMB: place new deco or place held deco");
    lines.add("RMB: pick up placed deco near cursor");
    lines.add("X: delete placed deco near cursor");
    lines.add("V: pipette deco type near cursor");
    lines.add(
      heldDecoEntity == null
        ? "State: preview ghost active"
        : "State: holding placed deco");
    return lines;
  }

  private List<String> buildPointsModeLines() {
    Point cursor = currentPointSnapPosition();

    List<String> lines = new ArrayList<>();
    lines.add("Cursor point: (" + cursor.x() + ", " + cursor.y() + ")");
    lines.add("Snap mode: " + pointSnapMode.displayName());
    lines.add("Held point: " + (heldPointName == null ? "<none>" : heldPointName));
    lines.add(
      "Total points: " + currentDungeonLevel().map(level -> level.namedPoints().size()).orElse(0));
    lines.add("C: next snap mode");
    lines.add("LMB: place held point or open point-name dialog");
    lines.add("RMB: pick point on cursor or clone held point");
    lines.add("X: delete point on cursor");
    return lines;
  }

  private List<String> buildLevelBoundsModeLines() {
    List<String> lines = new ArrayList<>();

    currentDungeonLevel()
      .ifPresentOrElse(
        level -> lines.add("Current size: " + level.layout()[0].length + "x" + level.layout().length),
        () -> lines.add("Current size: <no dungeon level>"));

    lines.add("E/Q: height + / -");
    lines.add("C/Z: width + / -");
    lines.add("New cells are filled with SKIP.");
    lines.add("Existing tiles keep their current LevelElement.");
    return lines;
  }

  private List<String> buildShiftLevelModeLines() {
    List<String> lines = new ArrayList<>();

    currentDungeonLevel()
      .ifPresentOrElse(
        level -> {
          lines.add("Current size: " + level.layout()[0].length + "x" + level.layout().length);
          lines.add("Named points: " + level.namedPoints().size());
        },
        () -> lines.add("Current size: <no dungeon level>"));

    lines.add("E/Q: shift level up / down");
    lines.add("C/Z: shift level right / left");
    lines.add("Blocked if a non-SKIP border tile would be overwritten.");
    lines.add("Also shifts named points and entities with PositionComponent.");
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

  private enum SnapMode {
    OnGrid("OnGrid"),
    QuarterGrid("QuarterGrid"),
    PixelGrid("PixelGrid"),
    OffGrid("OffGrid"),
    CheckerGridEven("CheckerGridEven"),
    CheckerGridOdd("CheckerGridOdd");

    private final String displayName;

    SnapMode(String displayName) {
      this.displayName = displayName;
    }

    public String displayName() {
      return displayName;
    }

    public SnapMode previousMode() {
      return values()[(this.ordinal() - 1 + values().length) % values().length];
    }

    public SnapMode nextMode() {
      return values()[(this.ordinal() + 1) % values().length];
    }

    public Point getPosition(Point position) {
      return switch (this) {
        case OnGrid ->
          new Point((float) Math.floor(position.x()), (float) Math.floor(position.y()));
        case QuarterGrid ->
          new Point(
            (float) Math.floor(position.x() * 4) / 4.0f,
            (float) Math.floor(position.y() * 4) / 4.0f);
        case PixelGrid ->
          new Point(
            (float) Math.floor(position.x() * 16) / 16.0f,
            (float) Math.floor(position.y() * 16) / 16.0f);
        case CheckerGridEven, CheckerGridOdd -> {
          int parity = (this == CheckerGridEven) ? 0 : 1;

          float px = position.x() - 0.5f;
          float py = position.y() - 0.5f;

          float gx = (float) Math.floor(px);
          float gy = (float) Math.floor(py);

          float bestX = gx;
          float bestY = gy;
          float bestDist = Float.MAX_VALUE;

          for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
              float cx = gx + dx;
              float cy = gy + dy;
              if (((int) (cx + cy)) % 2 == parity) {
                float dist = (px - cx) * (px - cx) + (py - cy) * (py - cy);
                if (dist < bestDist) {
                  bestDist = dist;
                  bestX = cx;
                  bestY = cy;
                }
              }
            }
          }

          yield new Point(bestX, bestY);
        }
        case OffGrid -> position;
      };
    }

    public boolean checkBlocked() {
      return this == OnGrid
        || this == QuarterGrid
        || this == CheckerGridEven
        || this == CheckerGridOdd;
    }
  }

  private void executePointsMode() {
    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      pointSnapMode = pointSnapMode.nextMode();
      showFeedback("Snap mode: " + pointSnapMode.displayName(), Color.WHITE);
    }

    Point cursorPos = Platform.cursor().world();
    Point snapPos = currentPointSnapPosition();

    if (InputManager.isButtonJustPressed(MouseButtons.LEFT)) {
      if (heldPointName != null) {
        currentDungeonLevel().ifPresent(level -> level.addNamedPoint(heldPointName, snapPos));
        showFeedback("Placed point: " + heldPointName, new Color(120, 220, 120));
        heldPointName = null;
      } else {
        openAddNamedPointDialog(snapPos);
      }
      return;
    }

    if (InputManager.isButtonJustPressed(MouseButtons.RIGHT)) {
      Optional<String> clickedPoint = findNamedPointAt(cursorPos);
      clickedPoint.ifPresent(point -> heldPointName = point);

      if (heldPointName == null) {
        showFeedback("No point to pick up on coordinate!", new Color(255, 220, 120));
      } else if (clickedPoint.isEmpty()) {
        currentDungeonLevel().ifPresent(
          level -> {
            String baseName = heldPointName.replaceAll("\\d+$", "");
            String newPointName = baseName + (level.getHighestPointNumber(baseName) + 1);
            level.addNamedPoint(newPointName, snapPos);
            showFeedback("Cloned point: " + newPointName, new Color(120, 220, 120));
          });
      } else {
        showFeedback("Picked point: " + heldPointName, new Color(120, 220, 120));
      }
      return;
    }

    if (InputManager.isKeyPressed(TERTIARY)) {
      findNamedPointAt(cursorPos)
        .ifPresent(
          pointName ->
            currentDungeonLevel().ifPresent(
              level -> {
                level.removeNamedPoint(pointName);
                showFeedback("Removed point: " + pointName, new Color(255, 180, 180));
              }));
    }
  }

  private void openAddNamedPointDialog(Point snapPos) {
    final Point dialogSnapPos = snapPos;

    UIComponent dialogUI =
      DialogFactory.show(
        DialogContext.builder()
          .type(DialogType.DefaultTypes.FREE_INPUT)
          .put(DialogContextKeys.TITLE, "Add Named Point")
          .put(DialogContextKeys.QUESTION, "Name of new point")
          .build());

    dialogUI.registerCallback(
      DialogContextKeys.INPUT_CALLBACK,
      data -> {
        if (data instanceof String string) {
          String pointName = string.trim();
          if (!pointName.isEmpty()) {
            currentDungeonLevel().ifPresent(level -> level.addNamedPoint(pointName, dialogSnapPos));
            showFeedback("Added point: " + pointName, new Color(120, 220, 120));
          }
        }
        UIUtils.closeDialog(dialogUI, true);
      });

    dialogUI.registerCallback(
      DialogContextKeys.ON_CANCEL,
      data -> UIUtils.closeDialog(dialogUI, true));
  }

  private Optional<DungeonLevel> currentDungeonLevel() {
    return Game.currentLevel()
      .filter(DungeonLevel.class::isInstance)
      .map(DungeonLevel.class::cast);
  }

  private Point currentPointSnapPosition() {
    return pointSnapMode.getPosition(Platform.cursor().world());
  }

  private Optional<String> findNamedPointAt(Point worldPos) {
    if (worldPos == null) {
      return Optional.empty();
    }

    Coordinate toCheck = worldPos.toCoordinate();
    return currentDungeonLevel().flatMap(level -> level.namedPoints().entrySet().stream()
      .filter(entry -> entry.getValue().toCoordinate().equals(toCheck))
      .map(Map.Entry::getKey)
      .findFirst());
  }

  private void renderPointMarkers(Graphics2D g) {
    LitiengineCameraViews.View view = LitiengineCameraViews.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    int levelHeight =
      view.levelHeight() > 0
        ? view.levelHeight()
        : currentDungeonLevel().map(level -> level.layout().length).orElse(0);

    int markerSize = Math.clamp(view.tilePx() / 3, POINT_MARKER_MIN_PX, POINT_MARKER_MAX_PX);

    Graphics2D g2 = (Graphics2D) g.create();
    try {
      currentDungeonLevel()
        .ifPresent(
          level ->
            level.namedPoints().forEach(
              (name, pos) -> drawNamedPointMarker(g2, name, pos, levelHeight, view, markerSize)));

      if (heldPointName != null) {
        drawHeldPointGhost(
          g2, heldPointName, currentPointSnapPosition(), levelHeight, view, markerSize);
      }
    } finally {
      g2.dispose();
    }
  }

  private void drawNamedPointMarker(
    Graphics2D g,
    String name,
    Point pointPos,
    int levelHeight,
    LitiengineCameraViews.View view,
    int markerSize) {

    int tilePx = view.tilePx();
    int screenX = (int) Math.round(pointPos.x() * tilePx + view.offsetX() + tilePx * 0.5f);

    float screenTileY =
      levelHeight > 0 ? (levelHeight - 1 - pointPos.y()) * tilePx : pointPos.y() * tilePx;
    int screenY = (int) Math.round(screenTileY + view.offsetY() + tilePx * 0.5f);

    int radius = markerSize / 2;
    boolean heldPoint = name != null && name.equals(heldPointName);

    g.setColor(heldPoint ? HELD_POINT_MARKER_COLOR : POINT_MARKER_COLOR);
    g.fillOval(screenX - radius, screenY - radius, markerSize, markerSize);

    g.setColor(Color.BLACK);
    g.drawOval(screenX - radius, screenY - radius, markerSize, markerSize);

    g.setColor(POINT_LABEL_COLOR);
    g.drawString(name, screenX + radius + 4, screenY - 4);
  }

  private void drawHeldPointGhost(
    Graphics2D g,
    String name,
    Point pointPos,
    int levelHeight,
    LitiengineCameraViews.View view,
    int markerSize) {

    int tilePx = view.tilePx();
    int screenX = (int) Math.round(pointPos.x() * tilePx + view.offsetX() + tilePx * 0.5f);

    float screenTileY =
      levelHeight > 0 ? (levelHeight - 1 - pointPos.y()) * tilePx : pointPos.y() * tilePx;
    int screenY = (int) Math.round(screenTileY + view.offsetY() + tilePx * 0.5f);

    int radius = markerSize / 2;

    g.setColor(HELD_POINT_MARKER_COLOR);
    g.fillOval(screenX - radius, screenY - radius, markerSize, markerSize);

    g.setColor(Color.BLACK);
    g.drawOval(screenX - radius, screenY - radius, markerSize, markerSize);

    g.setColor(POINT_LABEL_COLOR);
    g.drawString(name + " (held)", screenX + radius + 4, screenY - 4);
  }

  private void executeLevelBoundsMode() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      resizeLevelBounds(0, 1);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      resizeLevelBounds(0, -1);
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      resizeLevelBounds(1, 0);
    } else if (InputManager.isKeyJustPressed(SECONDARY_DOWN)) {
      resizeLevelBounds(-1, 0);
    }
  }

  private void resizeLevelBounds(int addX, int addY) {
    currentDungeonLevel()
      .ifPresent(
        level -> {
          Tile[][] layout = level.layout();
          int rows = layout.length;
          int cols = layout[0].length;

          int newRows = rows + addY;
          int newCols = cols + addX;

          if (newRows < 1 || newCols < 1) {
            showFeedback("Level size must stay at least 1x1.", new Color(255, 220, 120));
            return;
          }

          LevelElement[][] newLayout = new LevelElement[newRows][newCols];

          for (int y = 0; y < newRows; y++) {
            for (int x = 0; x < newCols; x++) {
              if (y >= rows || x >= cols) {
                newLayout[y][x] = LevelElement.SKIP;
              } else {
                newLayout[y][x] = layout[y][x].levelElement();
              }
            }
          }

          level.setLayout(newLayout);
          showFeedback(
            "Resized level to " + newCols + "x" + newRows + " (" + addX + ", " + addY + ")",
            Color.WHITE);
        });
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

  private void executeShiftLevelMode() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      shiftLevel(0, 1);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      shiftLevel(0, -1);
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      shiftLevel(1, 0);
    } else if (InputManager.isKeyJustPressed(SECONDARY_DOWN)) {
      shiftLevel(-1, 0);
    }
  }

  private void shiftLevel(int x, int y) {
    if (x == 0 && y == 0) {
      return;
    }

    String directionName = x == 1 ? "RIGHT" : x == -1 ? "LEFT" : y == 1 ? "UP" : "DOWN";
    showFeedback("Shifting level " + directionName, Color.WHITE);

    currentDungeonLevel()
      .ifPresent(
        level -> {
          Tile[][] layout = level.layout();

          if (!canShift(layout, x, y)) {
            showFeedback("Cannot shift level: overwriting non-SKIP tiles!", Color.RED);
            return;
          }

          int rows = layout.length;
          int cols = layout[0].length;

          LevelElement[][] newLayout = new LevelElement[rows][cols];

          for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
              int newI = i - y;
              int newJ = j - x;

              if (newI >= 0 && newI < rows && newJ >= 0 && newJ < cols) {
                newLayout[i][j] = layout[newI][newJ].levelElement();
              } else {
                newLayout[i][j] = LevelElement.SKIP;
              }
            }
          }

          for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
              level.changeTileElementType(layout[i][j], newLayout[i][j]);
            }
          }

          // Same second pass as in the old ShiftLevelMode to refresh resulting tile visuals again.
          for (Tile[] tiles : layout) {
            for (int j = 0; j < cols; j++) {
              level.changeTileElementType(tiles[j], tiles[j].levelElement());
            }
          }

          level.namedPoints().replaceAll((name, point) -> new Point(point.x() + x, point.y() + y));

          Game.levelEntities(Set.of(PositionComponent.class))
            .forEach(
              entity -> {
                PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
                Point old = pc.position();
                pc.position(new Point(old.x() + x, old.y() + y));
                PositionSync.syncPosition(entity);
              });
        });
  }

  private boolean canShift(Tile[][] layout, int x, int y) {
    int rows = layout.length;
    int cols = layout[0].length;

    if (x == 1) {
      for (Tile[] tiles : layout) {
        if (tiles[cols - 1].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    } else if (x == -1) {
      for (Tile[] tiles : layout) {
        if (tiles[0].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    }

    if (y == 1) {
      for (int j = 0; j < cols; j++) {
        if (layout[rows - 1][j].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    } else if (y == -1) {
      for (int j = 0; j < cols; j++) {
        if (layout[0][j].levelElement() != LevelElement.SKIP) {
          return false;
        }
      }
    }

    return true;
  }

  private void executeStartTilesMode() {
    int maxIndex =
      currentDungeonLevel()
        .map(level -> level.startTiles().size())
        .orElse(0);

    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      currentStartTileIndex = (currentStartTileIndex + 1) % (maxIndex + 1);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      currentStartTileIndex = Math.floorMod(currentStartTileIndex - 1, maxIndex + 1);
    }

    if (InputManager.isButtonJustPressed(MouseButtons.LEFT)) {
      setStartTileAtCursor();
    } else if (InputManager.isButtonJustPressed(MouseButtons.RIGHT)) {
      removeStartTileAtCursor();
    }
  }

  private void setStartTileAtCursor() {
    Point cursorPos = snappedCursorTile();

    currentDungeonLevel()
      .ifPresent(
        level -> {
          Tile tile = level.tileAt(cursorPos).orElse(null);
          if (tile == null || tile.levelElement() != LevelElement.FLOOR) {
            showFeedback(
              "Start tile must be within the level bounds and on a FLOOR tile!",
              Color.RED);
            return;
          }

          if (currentStartTileIndex == level.startTiles().size()) {
            level.startTiles().add(tile);
            showFeedback(
              "Added start tile " + (currentStartTileIndex + 1),
              START_TILE_COLORS[currentStartTileIndex % START_TILE_COLORS.length]);
          } else {
            level.startTiles().set(currentStartTileIndex, tile);
            showFeedback(
              "Updated start tile " + (currentStartTileIndex + 1),
              START_TILE_COLORS[currentStartTileIndex % START_TILE_COLORS.length]);
          }
        });
  }

  private void removeStartTileAtCursor() {
    currentDungeonLevel()
      .ifPresent(
        level -> {
          int maxIndex = level.startTiles().size();
          Point cursorPos = snappedCursorTile();

          if (maxIndex <= 1) {
            showFeedback("Cannot remove the last start tile.", Color.YELLOW);
            return;
          }

          Tile tile = level.tileAt(cursorPos).orElse(null);
          if (tile == null) {
            showFeedback("No start tile found under cursor.", Color.YELLOW);
            return;
          }

          boolean removed = level.startTiles().remove(tile);
          if (!removed) {
            showFeedback("No start tile found under cursor.", Color.YELLOW);
            return;
          }

          if (currentStartTileIndex > level.startTiles().size()) {
            currentStartTileIndex = Math.max(0, level.startTiles().size());
          }

          showFeedback("Removed start tile.", Color.YELLOW);
        });
  }

  private void renderStartTileMarkers(Graphics2D g) {
    LitiengineCameraViews.View view = LitiengineCameraViews.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    currentDungeonLevel()
      .ifPresent(
        level -> {
          Tile[][] layout = level.layout();
          int levelHeight = layout.length;
          int tilePx = view.tilePx();

          Graphics2D g2 = (Graphics2D) g.create();
          try {
            g2.setStroke(new BasicStroke(2.0f));

            for (int i = 0; i < level.startTiles().size(); i++) {
              Tile tile = level.startTiles().get(i);
              Point pos = tile.position();

              int drawX = (int) Math.round(pos.x() * tilePx + view.offsetX());
              int drawY =
                (int)
                  Math.round((levelHeight - 1 - pos.y()) * tilePx + view.offsetY());

              Color color = START_TILE_COLORS[i % START_TILE_COLORS.length];
              g2.setColor(color);
              g2.drawRect(drawX, drawY, Math.max(0, tilePx - 1), Math.max(0, tilePx - 1));

              g2.drawString(
                "Start: " + (i + 1),
                drawX + 4,
                drawY + Math.max(14, tilePx / 2));
            }
          } finally {
            g2.dispose();
          }
        });
  }

  private List<String> buildStartTilesModeLines() {
    List<String> lines = new ArrayList<>();

    currentDungeonLevel()
      .ifPresentOrElse(
        level -> {
          int size = level.startTiles().size();
          int shownIndex = Math.min(currentStartTileIndex, size);

          lines.add("Selected start tile slot: " + (shownIndex + 1));
          lines.add("Existing start tiles: " + size);

          if (currentStartTileIndex < size) {
            Tile selected = level.startTiles().get(currentStartTileIndex);
            Point pos = selected.position();
            lines.add(
              "Selected position: (" + (int) pos.x() + ", " + (int) pos.y() + ")");
          } else {
            lines.add("Selected slot: <new start tile>");
          }
        },
        () -> lines.add("No dungeon level loaded."));

    lines.add("E/Q: next/prev start tile slot");
    lines.add("LMB: place or replace selected start tile");
    lines.add("RMB: delete start tile on cursor");
    lines.add("Placement is only valid on FLOOR tiles.");
    return lines;
  }

  private void onModeEnter(Mode mode) {
    if (Objects.requireNonNull(mode) == Mode.START_TILES) {
      currentStartTileIndex =
        currentDungeonLevel()
          .map(level -> Math.min(currentStartTileIndex, level.startTiles().size()))
          .orElse(0);
    }
  }

  private void onModeExit(Mode mode) {
    switch (mode) {
      case POINTS -> heldPointName = null;
      case DECOS -> hoveredDecoEntity = null;
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
}
