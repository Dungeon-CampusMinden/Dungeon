package components;

import client.Client;
import contrib.components.AIComponent;
import contrib.components.BlockComponent;
import contrib.utils.EntityUtils;
import core.Component;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.PitTile;
import core.utils.Direction;
import core.utils.MissingPlayerException;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import entities.monster.BlocklyMonster;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/** Base component for all hero actions, such as moving, interaction, etc. */
public sealed interface HeroActionComponent extends Component {
  String MOVEMENT_FORCE_ID = "Movement";

  void tick();

  /**
   * Returns the completion callback for this action, or {@code null} if none is registered.
   *
   * <p>The default implementation returns {@code null}. Inner classes that support VM-thread
   * blocking override this to return the {@link Runnable} supplied at construction time.
   */
  default @Nullable Runnable onComplete() {
    return null;
  }

  /**
   * Removes this component from the hero and notifies the global execution lock that the hero
   * stopped his action. Also fires the {@link #onComplete()} callback if one is registered.
   */
  default void endAction() {
    Game.player().ifPresent(hero -> hero.remove(this.getClass()));
    Runnable cb = onComplete();
    if (cb != null) cb.run();
  }

  final class MoveUtils {
    /** The distance threshold to determine when the hero has reached the target tile. */
    public static final double distanceThreshold = 0.01;

    private static void clearMovementForce(final @NotNull MovementData entityData) {
      entityData.velocityC.clearForces();
      entityData.velocityC.currentVelocity(Vector2.ZERO);
    }

    /**
     * Data class to hold the necessary components for movement.
     *
     * @param entity The entity to move.
     * @param direction The direction to move in.
     * @param positionC The position component of the entity.
     * @param velocityC The velocity component of the entity.
     * @param startPosition The starting position of the entity.
     * @param totalDistance The total distance to the target tile.
     * @param targetTile The target tile to move to.
     */
    public record MovementData(
        @NotNull Entity entity,
        @NotNull Direction direction,
        @NotNull PositionComponent positionC,
        @NotNull VelocityComponent velocityC,
        @NotNull Point startPosition,
        double totalDistance,
        @Nullable Tile targetTile) {
      public static MovementData fromEntity(Entity entity) {
        // Fetch all the necessary components
        final @NotNull Direction movementDirection = EntityUtils.getViewDirection(entity);

        final @NotNull PositionComponent positionC =
            entity
                .fetch(PositionComponent.class)
                .orElseThrow(
                    () -> MissingComponentException.build(entity, PositionComponent.class));
        final @NotNull VelocityComponent velocityC =
            entity
                .fetch(VelocityComponent.class)
                .orElseThrow(
                    () -> MissingComponentException.build(entity, VelocityComponent.class));
        final @Nullable Tile targetTile =
            Game.tileAt(positionC.position().translate(0.5f, 0.5f), movementDirection).orElse(null);

        // Calculate the total distance to the target tile to determine when we have reached it
        // (with
        // some threshold to avoid rounding errors)
        final @NotNull Point startPosition = positionC.position();
        final double totalDistance =
            startPosition.distance(
                targetTile != null ? targetTile.coordinate().toPoint() : startPosition);

        return new MovementData(
            entity,
            movementDirection,
            positionC,
            velocityC,
            startPosition,
            totalDistance,
            targetTile);
      }
    }

    /**
     * Moves the given entity in a specific direction.
     *
     * @param entityData The data of the entity to move, including its current position, velocity,
     *     and target tile.
     * @param onFinish A callback that will be executed when the movement is finished.
     */
    private static void moveEntity(
        final @NotNull MovementData entityData, final @NotNull Runnable onFinish) {
      // Check if the target tile is accessible and not a pit
      if (entityData.targetTile == null
          || (!entityData.targetTile.isAccessible() && !(entityData.targetTile instanceof PitTile))
          || Game.entityAtTile(entityData.targetTile)
              .anyMatch(e -> e.isPresent(BlockComponent.class))) {
        clearMovementForce(entityData);
        onFinish.run();
        return;
      }
      entityData.velocityC.maxSpeed(10000);

      // Check if we reached our destination
      if (entityData.velocityC.maxSpeed() > 0
          && entityData.startPosition.distance(entityData.positionC.position())
              >= entityData.totalDistance - distanceThreshold) {
        // Snap the hero to the target tile
        clearMovementForce(entityData);
        entityData.positionC.position(entityData.targetTile);
        onFinish.run();
        return;
      }
      entityData.velocityC.applyForce(
          MOVEMENT_FORCE_ID, entityData.direction.scale(Client.MOVEMENT_FORCE));
    }

    private static void moveEntities(
        final @NotNull Runnable onFinish, final MovementData... entities) {
      for (MovementData entityData : entities) {
        moveEntity(entityData, onFinish);
      }
    }
  }

  /**
   * Moves the character forwards exactly one tile. The direction of movement is determined by the
   * character's current facing direction.
   */
  record Move(
      @NotNull Runnable onComplete,
      @NotNull HeroActionComponent.MoveUtils.MovementData hero,
      @Nullable HeroActionComponent.MoveUtils.MovementData blackKnight)
      implements HeroActionComponent {
    public Move(@NotNull Runnable onComplete) {
      this(
          onComplete,
          MoveUtils.MovementData.fromEntity(Game.player().orElseThrow(MissingPlayerException::new)),
          Game.levelEntities()
              .filter(entity -> entity.name().equals(BlocklyMonster.BLACK_KNIGHT_NAME))
              .findFirst()
              .map(MoveUtils.MovementData::fromEntity)
              .orElse(null));
    }

    /**
     * Move the hero towards the target tile by applying a force in the movement direction. If the
     * hero has reached the target tile (within a certain threshold), snap the hero to the target
     * tile and remove this component from the hero. If the target tile is not accessible or has a
     * block on it, remove this component from the hero immediately since the hero cannot move in
     * that direction.
     */
    @Override
    public void tick() {
      // Check if the target tile is accessible and not a pit
      if (hero.targetTile == null
          || (!hero.targetTile.isAccessible() && !(hero.targetTile instanceof PitTile))
          || Game.entityAtTile(hero.targetTile).anyMatch(e -> e.isPresent(BlockComponent.class))) {
        endAction();
        return;
      }
      MoveUtils.moveEntity(hero, this::endAction);
      if (blackKnight != null) {
        MoveUtils.moveEntity(blackKnight, () -> {});
      }
    }
  }

  record MovePushable(
      @NotNull HeroActionComponent.MoveUtils.MovementData hero,
      @NotNull List<MoveUtils.@NotNull MovementData> entitiesToMove,
      @NotNull Runnable onComplete)
      implements HeroActionComponent {
    public static Optional<MovePushable> of(boolean push, @NotNull Runnable onComplete) {
      var heroTemp =
          MoveUtils.MovementData.fromEntity(Game.player().orElseThrow(MissingPlayerException::new));

      // Check if the player is frozen
      if (heroTemp.velocityC.maxSpeed() == 0) {
        return Optional.empty();
      }

      Client.SHOOT_AT_PLAYER = false;

      // We cant do anything here
      if (heroTemp.targetTile == null) {
        return Optional.empty();
      }

      MoveUtils.MovementData heroMovementData;
      // If we want to push, we move in the direction we are facing, otherwise we move in the
      // opposite direction
      if (push) {
        heroMovementData =
            new MoveUtils.MovementData(
                heroTemp.entity,
                heroTemp.direction,
                heroTemp.positionC,
                heroTemp.velocityC,
                heroTemp.startPosition,
                heroTemp.totalDistance,
                heroTemp.targetTile);
      } else {
        heroMovementData =
            new MoveUtils.MovementData(
                heroTemp.entity,
                heroTemp.direction.opposite(),
                heroTemp.positionC,
                heroTemp.velocityC,
                heroTemp.startPosition,
                heroTemp.totalDistance,
                heroTemp.targetTile);
      }

      // Check if the target tile is accessible and not a pit
      if (heroMovementData.targetTile == null
          || heroMovementData.targetTile.isAccessible()
          || Game.entityAtTile(heroMovementData.targetTile)
              .anyMatch(e -> e.isPresent(BlockComponent.class))
          || Game.entityAtTile(heroMovementData.targetTile)
              .anyMatch(e -> e.isPresent(AIComponent.class))) {
        return Optional.empty();
      }

      List<MoveUtils.MovementData> entitiesToMove =
          Game.entityAtTile(heroMovementData.targetTile)
              // Get all pushable entities on the target tile
              .filter(e -> e.isPresent(PushableComponent.class))
              // Remove the BlockComponent to avoid blocking the player while moving simultaneously
              .peek(e -> e.remove(BlockComponent.class))
              // Create a MovementData for each pushable entity
              .map(MoveUtils.MovementData::fromEntity)
              .toList();

      return Optional.of(new MovePushable(heroMovementData, entitiesToMove, onComplete));
    }

    @Override
    public void endAction() {
      HeroActionComponent.super.endAction();
      Client.SHOOT_AT_PLAYER = true;
    }

    @Override
    public void tick() {
      // Move the hero; call endAction once the hero has reached the target tile
      MoveUtils.moveEntity(
          hero,
          () -> {
            for (MoveUtils.MovementData entityData : entitiesToMove) {
              entityData.entity.add(new BlockComponent());
              // Turn the entity in the heros looking direction
              // TODO is this really by design?
              entityData
                  .entity
                  .fetch(PositionComponent.class)
                  .ifPresent(pc -> pc.viewDirection(hero.direction));
            }
            endAction();
          });
      // Move pushable entities in parallel (no-op callback – endAction fires via hero callback)
      MoveUtils.moveEntities(() -> {}, entitiesToMove.toArray(MoveUtils.MovementData[]::new));
    }
  }
}
