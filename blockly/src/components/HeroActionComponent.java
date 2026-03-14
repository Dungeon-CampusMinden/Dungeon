package components;

import client.Client;
import contrib.components.AIComponent;
import contrib.components.BlockComponent;
import contrib.modules.interaction.InteractionComponent;
import contrib.systems.EventScheduler;
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
import entities.MiscFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import systems.FireballScheduler;

import java.util.List;

import static coderunner.BlocklyCommands.DISABLE_SHOOT_AT_HERO;
import static coderunner.BlocklyCommands.MAGIC_OFFSET;

/** Base component for all hero actions, such as moving, interaction, etc. */
public sealed interface HeroActionComponent extends Component {
  String MOVEMENT_FORCE_ID = "Movement";

  /** String identifier for the breadcrumb item. */
  String BREADCRUMB = "Brotkrumen";

  /** String identifier for the clover item. */
  String CLOVER = "Kleeblatt";

  /** String identifier for the boss entity. */
  String BLOCKLY_BLACK_KNIGHT = "Blockly Black Knight";

  void tick();

  /**
   * Returns the completion callback for this action, or {@code null} if none is registered.
   *
   * <p>The default implementation returns {@code null}. Inner classes that support VM-thread
   * blocking override this to return the {@link Runnable} supplied at construction time.
   */
  default @Nullable Runnable getOnComplete() {
    return null;
  }

  /**
   * Removes this component from the hero and notifies the global execution lock that the hero
   * stopped his action. Also fires the {@link #getOnComplete()} callback if one is registered.
   */
  default void endAction() {
    Game.player().ifPresent(hero -> hero.remove(this.getClass()));
    Runnable cb = getOnComplete();
    if (cb != null) cb.run();
  }

  final class MoveBase {
    /** The distance threshold to determine when the hero has reached the target tile. */
    public static final double distanceThreshold = 0.01;

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
            Game.tileAt(positionC.position().translate(MAGIC_OFFSET), movementDirection)
                .orElse(null);

        // Calculate the total distance to the target tile to determine when we have reached it
        // (with
        // some threshold to avoid rounding errors)
        final @NotNull Point startPosition = positionC.position();
        final double totalDistance =
            startPosition.distance(
                targetTile != null ? targetTile.coordinate().toPoint() : startPosition);

        final double distanceThreshold = 0.01;
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
        onFinish.run();
        return;
      }

      entityData.velocityC.clearForces();
      entityData.velocityC.currentVelocity(Vector2.ZERO);
      entityData.velocityC.applyForce(
          MOVEMENT_FORCE_ID, entityData.direction.scale(Client.MOVEMENT_FORCE.x()));
      // Check if we reached our destination
      if (entityData.velocityC.maxSpeed() > 0
          && entityData.startPosition.distance(entityData.positionC.position())
              >= entityData.totalDistance - distanceThreshold) {
        // Snap the hero to the target tile
        entityData.positionC.position(entityData.targetTile);
        onFinish.run();
      }
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
  final class Move implements HeroActionComponent {

    private final @Nullable Runnable onComplete;

    @NotNull
    MoveBase.MovementData hero =
        MoveBase.MovementData.fromEntity(Game.player().orElseThrow(MissingPlayerException::new));

    @Nullable
    MoveBase.MovementData blackKnight =
        Game.levelEntities()
            .filter(entity -> entity.name().equals(BLOCKLY_BLACK_KNIGHT))
            .findFirst()
            .map(MoveBase.MovementData::fromEntity)
            .orElse(null);

    public Move() {
      this(null);
    }

    public Move(@Nullable Runnable onComplete) {
      this.onComplete = onComplete;
    }

    @Override
    public @Nullable Runnable getOnComplete() {
      return onComplete;
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
      MoveBase.moveEntity(hero, this::endAction);
      if (blackKnight != null) {
        MoveBase.moveEntity(blackKnight, () -> {});
      }
    }
  }

  final class MovePushable implements HeroActionComponent {
    final @Nullable MoveBase.MovementData hero;
    final @NotNull List<MoveBase.@NotNull MovementData> entitesToMove;
    private @Nullable Runnable onComplete;

    public MovePushable(boolean push) {
      this(push, null);
    }

    public MovePushable(boolean push, @Nullable Runnable onComplete) {
      this.onComplete = onComplete;
      var heroTemp =
          MoveBase.MovementData.fromEntity(Game.player().orElseThrow(MissingPlayerException::new));

      // Check if the player is frozen
      if (heroTemp.velocityC.maxSpeed() == 0) {
        hero = null;
        entitesToMove = List.of();
        return;
      }

      DISABLE_SHOOT_AT_HERO = true;

      // We cant do anything here
      if (heroTemp.targetTile == null) {
        hero = null;
        entitesToMove = List.of();
        return;
      }

      // If we want to push, we move in the direction we are facing, otherwise we move in the
      // opposite direction
      if (push) {
        hero =
            new MoveBase.MovementData(
                heroTemp.entity,
                heroTemp.direction,
                heroTemp.positionC,
                heroTemp.velocityC,
                heroTemp.startPosition,
                heroTemp.totalDistance,
                heroTemp.targetTile);
      } else {
        hero =
            new MoveBase.MovementData(
                heroTemp.entity,
                heroTemp.direction.opposite(),
                heroTemp.positionC,
                heroTemp.velocityC,
                heroTemp.startPosition,
                heroTemp.totalDistance,
                heroTemp.targetTile);
      }

      // Check if the target tile is accessible and not a pit
      if (hero.targetTile == null
          || hero.targetTile.isAccessible()
          || Game.entityAtTile(hero.targetTile).anyMatch(e -> e.isPresent(BlockComponent.class))
          || Game.entityAtTile(hero.targetTile).anyMatch(e -> e.isPresent(AIComponent.class))) {
        entitesToMove = List.of();
        return;
      }

      entitesToMove =
          Game.entityAtTile(hero.targetTile)
              // Get all pushable entities on the target tile
              .filter(e -> e.isPresent(PushableComponent.class))
              // Remove the BlockComponent to avoid blocking the player while moving simultaneously
              .peek(e -> e.remove(BlockComponent.class))
              // Create a MovementData for each pushable entity
              .map(MoveBase.MovementData::fromEntity)
              .toList();
    }

    @Override
    public @Nullable Runnable getOnComplete() {
      return onComplete;
    }

    @Override
    public void endAction() {
      HeroActionComponent.super.endAction();
      DISABLE_SHOOT_AT_HERO = false;
    }

    @Override
    public void tick() {
      if (hero == null) {
        endAction();
        return;
      }

      // Move the hero; call endAction once the hero has reached the target tile
      MoveBase.moveEntity(
          hero,
          () -> {
            for (MoveBase.MovementData entityData : entitesToMove) {
              entityData.entity.add(new BlockComponent());
              Rotate.turnEntity(entityData.entity, hero.direction);
            }
            endAction();
          });
      // Move pushable entities in parallel (no-op callback – endAction fires via hero callback)
      MoveBase.moveEntities(() -> {}, entitesToMove.toArray(MoveBase.MovementData[]::new));
    }
  }

  /**
   * Rotates the hero in a given direction relative to the current facing direction. For example, if
   * the hero is currently facing up and the rotation direction is left, the new facing direction
   * will be left. If the hero is currently facing right and the rotation direction is right, the
   * new facing direction will be down.
   */
  final class Rotate implements HeroActionComponent {
    // Fetch all the necessary components
    private final @NotNull Entity hero = Game.player().orElseThrow(MissingPlayerException::new);
    private final @NotNull Direction newDirection;
    private final @Nullable Runnable onComplete;

    public Rotate(final @NotNull Direction direction) {
      this(direction, null);
    }

    public Rotate(final @NotNull Direction direction, @Nullable Runnable onComplete) {
      this.onComplete = onComplete;
      if (direction == Direction.NONE || direction == Direction.UP || direction == Direction.DOWN) {
        throw new IllegalArgumentException("Invalid rotation direction: " + direction);
      }
      newDirection = EntityUtils.getViewDirection(hero).applyRelative(direction);
    }

    @Override
    public @Nullable Runnable getOnComplete() {
      return onComplete;
    }

    @Override
    public void tick() {
      turnEntity(hero, newDirection);
      Game.levelEntities()
          .filter(entity -> entity.name().equals(BLOCKLY_BLACK_KNIGHT))
          .findFirst()
          .flatMap(
              boss ->
                  boss.fetch(VelocityComponent.class)
                      .filter(vc -> vc.maxSpeed() > 0)
                      .map(vc -> boss))
          .ifPresent(boss -> turnEntity(boss, newDirection.opposite()));
      endAction();
    }

    public static void turnEntity(Entity entity, Direction direction) {
      entity.fetch(PositionComponent.class).ifPresent(pc -> pc.viewDirection(direction));
    }
  }

  final class Drop implements HeroActionComponent {
    private final @NotNull String item;
    private final @Nullable Runnable onComplete;

    public Drop(String item) {
      this(item, null);
    }

    public Drop(String item, @Nullable Runnable onComplete) {
      this.item = item;
      this.onComplete = onComplete;
    }

    @Override
    public @Nullable Runnable getOnComplete() {
      return onComplete;
    }

    @Override
    public void tick() {
      Point heroPos =
          Game.player()
              .flatMap(hero -> hero.fetch(PositionComponent.class))
              .map(PositionComponent::position)
              .map(pos -> pos.translate(MAGIC_OFFSET))
              .orElse(null);

      switch (item) {
        case BREADCRUMB -> Game.add(MiscFactory.breadcrumb(heroPos));
        case CLOVER -> Game.add(MiscFactory.clover(heroPos));
        default ->
            throw new IllegalArgumentException("Can not convert " + item + " to droppable Item.");
      }
      endAction();
    }
  }

  final class ShootFireball implements HeroActionComponent {
    private final @Nullable Runnable onComplete;

    public ShootFireball() {
      this(null);
    }

    public ShootFireball(@Nullable Runnable onComplete) {
      this.onComplete = onComplete;
      FireballScheduler.shoot();
      EventScheduler.scheduleAction(this::endAction, 10000);
    }

    @Override
    public @Nullable Runnable getOnComplete() {
      return onComplete;
    }

    @Override
    public void tick() {}
  }

  final class Pickup implements HeroActionComponent {
    private final @Nullable Runnable onComplete;

    public Pickup() {
      this(null);
    }

    public Pickup(@Nullable Runnable onComplete) {
      this.onComplete = onComplete;
    }

    @Override
    public @Nullable Runnable getOnComplete() {
      return onComplete;
    }

    @Override
    public void tick() {
      // Get the hero
      Game.player()
          .ifPresent(
              hero ->
                  // Get the tile at the position of the hero
                  hero.fetch(PositionComponent.class)
                      .map(PositionComponent::position)
                      .map(pos -> pos.translate(MAGIC_OFFSET))
                      .flatMap(Game::tileAt)
                      .map(Game::entityAtTile)
                      // Filter the entities to only include entities that are items
                      .ifPresent(
                          stream ->
                              stream
                                  .filter(e -> e.isPresent(BlocklyItemComponent.class))
                                  // Trigger the interaction of each item with the hero, which
                                  // should result in the item being picked up and added to the
                                  // inventory
                                  .forEach(
                                      item ->
                                          item.fetch(InteractionComponent.class)
                                              .ifPresent(
                                                  ic -> ic.triggerInteraction(item, hero)))));
      endAction();
    }
  }

  final class Rest implements HeroActionComponent {
    private final @Nullable Runnable onComplete;

    /**
     * Let the player do nothing for a short moment.
     *
     * @param duration Duration in seconds
     */
    public Rest(float duration) {
      this(duration, null);
    }

    public Rest(float duration, @Nullable Runnable onComplete) {
      this.onComplete = onComplete;
      EventScheduler.scheduleAction(this::endAction, (long) (1000 * duration));
    }

    @Override
    public @Nullable Runnable getOnComplete() {
      return onComplete;
    }

    @Override
    public void tick() {}
  }
}
