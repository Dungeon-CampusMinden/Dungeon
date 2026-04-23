package contrib.editor.level.mode;

import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.editor.level.LevelEditorSystem;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.systems.PositionSync;
import contrib.utils.components.collide.Collider;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.input.InputLabel.InputCode;
import core.input.MouseButtons;
import core.platform.Platform;
import core.utils.InputManager;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import java.awt.Color;
import java.util.*;

/**
 * A level editor mode for placing, managing, and manipulating decorations (deco) in a dungeon level.
 *
 * <p>DecoMode provides comprehensive functionality for decorating a level with various deco types.
 * It supports the following operations:
 * <ul>
 *   <li>Placing new decorations at cursor positions
 *   <li>Picking up and moving existing decorations
 *   <li>Deleting decorations from the level
 *   <li>Pipetting deco types from existing decorations
 *   <li>Previewing deco placements with visual feedback
 *   <li>Switching between different deco types and snap modes
 * </ul>
 *
 * <p>The mode features intelligent placement validation, showing blocked (red) vs. valid (white)
 * placements through color-coded visual indicators. It supports multiple snap modes for flexible
 * positioning and remembers original entity tint colors for proper restoration.
 */
public final class DecoMode extends LevelEditorMode {

  private static final int DECO_PREVIEW_TINT = 0xFFFFFF80;
  private static final int DECO_PREVIEW_BLOCKED_TINT = 0xFF6B6B99;
  private static final int DECO_DEFAULT_TINT = -1;
  private static final int DECO_HOVER_TINT = 0xFFFF00FF;
  private static final double DECO_CURSOR_DISTANCE = 0.75d;

  private int selectedDecoIndex = 0;
  private EditorSnapMode decoSnapMode = EditorSnapMode.ON_GRID;
  private Entity decoPreviewEntity = null;
  private Entity heldDecoEntity = null;
  private Entity hoveredDecoEntity = null;
  private final Map<Entity, Integer> rememberedEditorTints = new IdentityHashMap<>();

  /**
   * Constructs a new instance of DecoMode, representing a specific editing mode
   * within the level editor.
   *
   * @param system the LevelEditorSystem instance that manages the level editor
   *               and provides the necessary context and functionality for this mode
   */
  public DecoMode(LevelEditorSystem system) {
    super(system, "Deco Mode");
  }

  @Override
  protected void execute() {
    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      selectedDecoIndex = Math.floorMod(selectedDecoIndex + 1, Deco.values().length);
      if (heldDecoEntity == null) {
        previewDecoEntityChanged();
      }
      system().showModeFeedback("Selected deco: " + selectedDeco().name(), Color.WHITE);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      selectedDecoIndex = Math.floorMod(selectedDecoIndex - 1, Deco.values().length);
      if (heldDecoEntity == null) {
        previewDecoEntityChanged();
      }
      system().showModeFeedback("Selected deco: " + selectedDeco().name(), Color.WHITE);
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      decoSnapMode = decoSnapMode.nextMode();
      if (heldDecoEntity == null) {
        previewDecoEntityChanged();
      }
      system().showModeFeedback("Snap mode: " + decoSnapMode.displayName(), Color.WHITE);
    }

    Point cursorPos = Platform.cursor().world();
    Point snapPos = currentDecoSnapPosition();

    if (InputManager.isKeyJustPressed(QUATERNARY)) {
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

  @Override
  public void onEnter() {
    if (heldDecoEntity == null) {
      ensureDecoPreviewEntity();
      updateDecoPreviewPosition(currentDecoSnapPosition());
      updateDecoPlacementIndicator();
    }
  }

  @Override
  public void onExit() {
    clearHoveredDecoIndicator();
    releaseHeldDecoIfNecessary();
    removeDecoPreviewEntity();
    restoreAllRememberedEditorTints();
  }

  @Override
  protected List<String> getStatusLines() {
    Point cursor = system().snappedCursorTileForModes();
    Deco currentDeco = selectedDeco();

    return List.of(
      "Cursor tile: (" + (int) cursor.x() + ", " + (int) cursor.y() + ")",
      "Current deco: "
        + (Math.floorMod(selectedDecoIndex, Deco.values().length) + 1)
        + "/"
        + Deco.values().length
        + " ("
        + currentDeco.name()
        + ")",
      "Snap mode: " + decoSnapMode.displayName(),
      "Placement: " + (isCurrentDecoPlacementBlocked() ? "blocked" : "valid"),
      "Hover: " + currentHoveredDecoName(),
      "Preview tint: white = valid, red = blocked",
      heldDecoEntity != null ? "State: holding placed deco" : "State: preview ghost active");
  }

  @Override
  protected Map<InputCode, String> getControls() {
    Map<InputCode, String> controls = new LinkedHashMap<>();
    controls.put(key(PRIMARY_UP), "Next deco");
    controls.put(key(PRIMARY_DOWN), "Previous deco");
    controls.put(key(SECONDARY_UP), "Next snap mode");
    controls.put(mouseButton(MouseButtons.LEFT), "Place new deco or place held deco");
    controls.put(mouseButton(MouseButtons.RIGHT), "Pick up placed deco near cursor");
    controls.put(key(TERTIARY), "Delete placed deco near cursor");
    controls.put(key(QUATERNARY), "Pipette deco type near cursor");
    return controls;
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

    setupDecoPreviewEntity(system().snappedCursorTileForModes());
  }

  private void setupDecoPreviewEntity(Point pos) {
    Entity preview = DecoFactory.createDeco(pos, selectedDeco());

    preview.fetch(DrawComponent.class).ifPresent(dc -> dc.tintColor(DECO_PREVIEW_TINT));
    preview.fetch(CollideComponent.class).ifPresent(cc -> cc.isSolid(false));

    Game.add(preview);
    decoPreviewEntity = preview;
    updateDecoPreviewPosition(pos);
  }

  private void removeDecoPreviewEntity() {
    if (decoPreviewEntity == null) {
      return;
    }

    restoreEditorTint(decoPreviewEntity);
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
      .filter(entity -> !entity.equals(heldDecoEntity));
  }

  private void applyHoveredDecoTint(Entity entity) {
    applyEditorTint(entity, DECO_HOVER_TINT);
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
    applyEditorTint(entity, blocked ? DECO_PREVIEW_BLOCKED_TINT : DECO_PREVIEW_TINT);
  }

  private void resetDecoTint(Entity entity) {
    restoreEditorTint(entity);
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

    updateHeldDecoPosition(system().snappedCursorTileForModes());
    system().showModeFeedback("Picked up deco: " + pickedName, new Color(120, 220, 120));
  }

  private void placeHeldDeco(Point snapPos) {
    if (heldDecoEntity == null) {
      return;
    }

    Point placementPos = alignedDecoPosition(heldDecoEntity, snapPos);

    if (decoSnapMode.checkBlocked() && isDecoPlacementBlocked(heldDecoEntity, placementPos)) {
      system().showModeFeedback("Cannot place held deco: target blocked", new Color(255, 210, 120));
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

    restoreEditorTint(placedEntity);
    syncPlacedDecos();

    heldDecoEntity = null;
    ensureDecoPreviewEntity();
    updateDecoPreviewPosition(snapPos);
    updateDecoPlacementIndicator();

    system().showModeFeedback("Placed held deco: " + placedName, new Color(120, 220, 120));
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
        system().showModeFeedback("Picked deco type: " + decoType.get().name(), Color.WHITE);
        return;
      }
    }
  }

  private void releaseHeldDecoIfNecessary() {
    if (heldDecoEntity == null) {
      return;
    }

    restoreEditorTint(heldDecoEntity);
    syncPlacedDecos();
    heldDecoEntity = null;
  }

  private void placeSelectedDeco(Point snapPos) {
    Point placementPos =
      decoPreviewEntity != null ? alignedDecoPosition(decoPreviewEntity, snapPos) : snapPos;

    if (decoSnapMode.checkBlocked()
      && decoPreviewEntity != null
      && isDecoPlacementBlocked(decoPreviewEntity, placementPos)) {
      system().showModeFeedback("Cannot place deco: target blocked", new Color(255, 210, 120));
      return;
    }

    Entity placedDeco = DecoFactory.createDeco(placementPos, selectedDeco());
    Game.add(placedDeco);
    syncPlacedDecos();

    system().showModeFeedback("Placed deco: " + selectedDeco().name(), new Color(120, 220, 120));
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

    system().showModeFeedback("Removed deco: " + removedName, new Color(255, 180, 180));
  }

  private Point currentDecoPreviewPosition() {
    if (decoPreviewEntity == null) {
      return system().snappedCursorTileForModes();
    }

    return decoPreviewEntity
      .fetch(PositionComponent.class)
      .map(PositionComponent::position)
      .orElse(system().snappedCursorTileForModes());
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
    system()
      .currentDungeonLevelForModes()
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

  private void applyEditorTint(Entity entity, int tint) {
    if (entity == null) {
      return;
    }

    entity.fetch(DrawComponent.class)
      .ifPresent(
        dc -> {
          rememberedEditorTints.putIfAbsent(entity, dc.tintColor());
          dc.tintColor(tint);
        });
  }

  private void restoreEditorTint(Entity entity) {
    if (entity == null) {
      return;
    }

    Integer originalTint = rememberedEditorTints.remove(entity);

    entity.fetch(DrawComponent.class)
      .ifPresent(dc -> dc.tintColor(originalTint != null ? originalTint : DECO_DEFAULT_TINT));
  }

  private void restoreAllRememberedEditorTints() {
    for (Map.Entry<Entity, Integer> entry : new IdentityHashMap<>(rememberedEditorTints).entrySet()) {
      Entity entity = entry.getKey();
      Integer tint = entry.getValue();

      if (entity != null) {
        entity.fetch(DrawComponent.class).ifPresent(dc -> dc.tintColor(tint));
      }
    }

    rememberedEditorTints.clear();
  }
}
