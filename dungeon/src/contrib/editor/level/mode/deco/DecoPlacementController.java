package contrib.editor.level.mode.deco;

import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.editor.level.LevelEditorSystem;
import contrib.editor.level.mode.EditorSnapMode;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.utils.components.collide.Collider;
import contrib.utils.components.collide.ColliderSync;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import java.util.List;
import java.util.Optional;

/**
 * The DecoPlacementController is responsible for managing the placement, movement, preview, and
 * deletion of decorative entities (Deco) within a level editor system.
 *
 * <p>It provides functionality such as maintaining a preview decoration, handling held decorations,
 * and ensuring proper alignment and collision check during placement operations.
 *
 * <p>This controller interacts with the level editor system, the deco tint controller, and the game
 * world to ensure that decorations are placed or managed correctly within the level's coordinate
 * and collision constraints.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Handles decoration preview entity creation, removal, and updates.
 *   <li>Manages a held decoration entity for placement or manipulation.
 *   <li>Ensures correct alignment of decorations based on snapping positions.
 *   <li>Detects and applies collision rules for decoration placement.
 *   <li>Supports deletion of decoration and retrieval operations.
 *   <li>Synchronizes placed decorations with the current dungeon level.
 *   <li>Provides utility methods for querying decoration entities.
 * </ul>
 */
final class DecoPlacementController {
  private static final double DECO_CURSOR_DISTANCE = 0.75d;

  private final LevelEditorSystem system;

  private Entity decoPreviewEntity;
  private Entity heldDecoEntity;

  DecoPlacementController(LevelEditorSystem system) {
    this.system = system;
  }

  boolean hasHeldDeco() {
    return heldDecoEntity != null;
  }

  Entity indicatorEntity() {
    return heldDecoEntity != null ? heldDecoEntity : decoPreviewEntity;
  }

  void ensurePreviewEntity(Deco selectedDeco, Point snapPos) {
    if (decoPreviewEntity != null) {
      return;
    }

    setupDecoPreviewEntity(snapPos, selectedDeco);
  }

  void refreshPreviewEntity(
      Deco selectedDeco, Point currentSnapPos, DecoTintController tintController) {
    Point previewPos = currentSnapPos;

    if (decoPreviewEntity != null) {
      previewPos = currentPreviewPosition(currentSnapPos);
    }

    removeDecoPreviewEntity(tintController);
    setupDecoPreviewEntity(previewPos, selectedDeco);
  }

  void removeDecoPreviewEntity(DecoTintController tintController) {
    if (decoPreviewEntity == null) {
      return;
    }

    tintController.restoreEditorTint(decoPreviewEntity);
    Game.remove(decoPreviewEntity);
    decoPreviewEntity = null;
  }

  void updateDecoPreviewPosition(Point snapPos) {
    updateEditorDecoPosition(decoPreviewEntity, snapPos);
  }

  void updateHeldDecoPosition(Point snapPos) {
    updateEditorDecoPosition(heldDecoEntity, snapPos);
  }

  Optional<String> pickupDecoAt(Point worldPos, Point snapPos, DecoTintController tintController) {
    Optional<Entity> placedDeco = findPlacedDecoNear(worldPos);
    if (placedDeco.isEmpty()) {
      return Optional.empty();
    }

    heldDecoEntity = placedDeco.get();
    removeDecoPreviewEntity(tintController);
    updateHeldDecoPosition(snapPos);
    return Optional.of(decoName(heldDecoEntity));
  }

  String placeHeldDeco(Point snapPos, DecoTintController tintController) {
    updateEditorDecoPosition(heldDecoEntity, snapPos);
    Entity placedEntity = heldDecoEntity;
    String placedName = decoName(placedEntity);

    tintController.restoreEditorTint(placedEntity);
    syncPlacedDecos();
    heldDecoEntity = null;
    return placedName;
  }

  void placeSelectedDeco(Deco selectedDeco, Point snapPos) {
    Point placementPos =
        decoPreviewEntity != null ? alignedDecoPosition(decoPreviewEntity, snapPos) : snapPos;
    Entity placedDeco = DecoFactory.createDeco(placementPos, selectedDeco);
    Game.add(placedDeco);
    syncPlacedDecos();
  }

  Optional<String> deleteDecoAt(Point worldPos) {
    Optional<Entity> placedDeco = findPlacedDecoNear(worldPos);
    if (placedDeco.isEmpty()) {
      return Optional.empty();
    }

    String removedName = decoName(placedDeco.get());
    Game.remove(placedDeco.get());
    syncPlacedDecos();
    return Optional.of(removedName);
  }

  Optional<Deco> pipetteDecoAt(Point worldPos) {
    return findPlacedDecoNear(worldPos)
        .flatMap(entity -> entity.fetch(DecoComponent.class).map(DecoComponent::type));
  }

  void releaseHeldDecoIfNecessary(DecoTintController tintController) {
    if (heldDecoEntity == null) {
      return;
    }

    tintController.restoreEditorTint(heldDecoEntity);
    syncPlacedDecos();
    heldDecoEntity = null;
  }

  Optional<Entity> findHoverableDecoNear(Point worldPos) {
    return findPlacedDecoNear(worldPos).filter(entity -> !entity.equals(heldDecoEntity));
  }

  boolean isCurrentDecoPlacementBlocked(EditorSnapMode snapMode, Point currentSnapPos) {
    if (!snapMode.checkBlocked()) {
      return false;
    }

    if (heldDecoEntity != null) {
      Point placementPos =
          heldDecoEntity
              .fetch(PositionComponent.class)
              .map(PositionComponent::position)
              .orElse(currentSnapPos);
      return isDecoPlacementBlocked(heldDecoEntity, placementPos);
    }

    if (decoPreviewEntity != null) {
      return isDecoPlacementBlocked(decoPreviewEntity, currentPreviewPosition(currentSnapPos));
    }

    return false;
  }

  private void setupDecoPreviewEntity(Point pos, Deco selectedDeco) {
    Entity preview = DecoFactory.createDeco(pos, selectedDeco);

    preview.fetch(CollideComponent.class).ifPresent(cc -> cc.isSolid(false));

    Game.add(preview);
    decoPreviewEntity = preview;
    updateDecoPreviewPosition(pos);
  }

  private void updateEditorDecoPosition(Entity entity, Point snapPos) {
    if (entity == null || snapPos == null) {
      return;
    }

    Point actualPos = alignedDecoPosition(entity, snapPos);
    entity.fetch(PositionComponent.class).ifPresent(pc -> pc.position(actualPos));
    ColliderSync.sync(entity);
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

  private Point currentPreviewPosition(Point fallback) {
    if (decoPreviewEntity == null) {
      return fallback;
    }

    return decoPreviewEntity
        .fetch(PositionComponent.class)
        .map(PositionComponent::position)
        .orElse(fallback);
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
      return findPlacedDecoNear(placementPos)
          .filter(entity -> !entity.equals(movingDeco))
          .isPresent();
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
          .anyMatch(
              otherCollide -> movingCollide.get().collider().collide(otherCollide.collider()));
    } finally {
      movingCollide.get().collider().position(oldColliderPos);
    }
  }

  private void syncPlacedDecos() {
    system
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

  private String decoName(Entity entity) {
    return entity
        .fetch(DecoComponent.class)
        .map(DecoComponent::type)
        .map(Enum::name)
        .orElse("Deco");
  }
}
