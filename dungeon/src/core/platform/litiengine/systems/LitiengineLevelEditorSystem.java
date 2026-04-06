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
import core.components.PositionComponent;
import core.input.Keys;
import core.input.MouseButtons;
import core.level.Tile;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private Entity decoPreviewEntity = null;
  private Entity heldDecoEntity = null;
  private Entity hoveredDecoEntity = null;

  private String feedbackMessage = "";
  private Color feedbackColor = Color.WHITE;
  private long feedbackUntilMs = 0L;

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

    handleModeHotkeys();
    executeCurrentMode();
    syncOverlay();
  }

  @Override
  public void render(float deltaSeconds) {
    if (!this.active) {
      return;
    }

    if (this.currentMode != Mode.TILES) {
      return;
    }

    Graphics2D g = LitiengineGraphicsContext.get();
    if (g == null) {
      return;
    }

    renderTileBrushPreview(g);
  }

  /** Returns whether the editor is currently active. */
  public boolean active() {
    return this.active;
  }

  private void executeCurrentMode() {
    switch (currentMode) {
      case TILES -> executeTilesMode();
      case DECOS -> executeDecosMode();
      case POINTS, LEVEL_BOUNDS, SHIFT_LEVEL, START_TILES, SAVE_LEVEL -> {
        // intentionally not ported yet
      }
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

    Point cursorPos = Platform.cursor().world();
    Point snapPos = snappedCursorTile();

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
    Point currentPos = snappedCursorTile();

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
    if (heldDecoEntity != null) {
      Point placementPos =
        heldDecoEntity
          .fetch(PositionComponent.class)
          .map(PositionComponent::position)
          .orElse(snappedCursorTile());
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

    if (isDecoPlacementBlocked(heldDecoEntity, placementPos)) {
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

    if (decoPreviewEntity != null && isDecoPlacementBlocked(decoPreviewEntity, placementPos)) {
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

      overlay.visible(true);
      if (!LitiengineUiOverlayRegistry.contains(overlay)) {
        LitiengineUiOverlayRegistry.add(overlay);
      }

      if (this.currentMode == Mode.DECOS) {
        ensureDecoPreviewEntity();
        updateDecoPlacementIndicator();
      }

      showFeedback("LITIENGINE level editor active", new Color(120, 220, 120));
      syncOverlay();
      LOGGER.info("Activated LITIENGINE level editor.");
      return;
    }

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
      ensureDecoPreviewEntity();
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
      overlay.height(currentMode == Mode.TILES || currentMode == Mode.DECOS ? 320 : 230);
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
    lines.add("Modes: " + modeSelectionText());
    lines.add("");

    if (currentMode == Mode.TILES) {
      lines.addAll(buildTilesModeLines());
    } else if (currentMode == Mode.DECOS) {
      lines.addAll(buildDecosModeLines());
    } else {
      lines.add("This mode is not ported yet on the LITIENGINE path.");
      lines.add("Tiles and Decos preview are the first implemented editor steps so far.");
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
    lines.add("Placement: " + (isCurrentDecoPlacementBlocked() ? "blocked" : "valid"));
    lines.add("Hover: " + currentHoveredDecoName());
    lines.add("Preview tint: white = valid, red = blocked");
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
}
