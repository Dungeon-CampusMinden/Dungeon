package feature.audience;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.Tile;
import engine.utils.Direction;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.collision.CollisionUtils;
import feature.components.CollideComponent;
import feature.interaction.InteractionComponent;
import feature.systems.PositionSync;
import java.util.Comparator;
import java.util.Optional;

/**
 * Moves the hero exactly one tile per voted step and interacts with the tile in front of it.
 *
 * <p>{@link #tick()} must run before the {@code VelocitySystem} in every frame: it steers the hero
 * toward the destination through its velocity, so the walking animation plays, and settles it
 * exactly on the destination tile.
 */
final class TileStepper {
  /** Tiles per second while stepping. */
  private static final float SPEED = 4f;

  private final Entity hero;
  private Point target;

  TileStepper(Entity hero) {
    this.hero = hero;
  }

  /**
   * @return whether the hero is still walking to the destination of the last step
   */
  boolean moving() {
    return target != null;
  }

  /**
   * Turns the hero toward {@code direction} and starts walking one tile when that tile is free.
   *
   * @param direction voted direction
   */
  void step(Direction direction) {
    PositionComponent position = hero.fetch(PositionComponent.class).orElseThrow();
    position.viewDirection(direction);
    Optional<Tile> current = Game.tileAtEntity(hero);
    if (current.isEmpty()) return;
    Point start = current.get().position();
    Optional<Tile> next = Game.tileAt(start, direction);
    if (next.isEmpty() || !next.get().isAccessible()) return;
    Point destination = next.get().position();
    if (hero.isPresent(CollideComponent.class)
        && CollisionUtils.isCollidingWithOtherSolids(hero, destination)) {
      return;
    }
    // Start from the tile origin so every step ends exactly on the next tile.
    position.position(start);
    PositionSync.syncPosition(hero);
    target = destination;
  }

  /**
   * Interacts with the nearest interactable entity on the tile the hero faces. Nothing happens when
   * there is none.
   */
  void interact() {
    PositionComponent position = hero.fetch(PositionComponent.class).orElseThrow();
    Optional<Tile> current = Game.tileAtEntity(hero);
    if (current.isEmpty()) return;
    Optional<Tile> front = Game.tileAt(current.get().position(), position.viewDirection());
    if (front.isEmpty()) return;
    Point heroPosition = position.position();
    Optional<Entity> target =
        Game.entityAtTile(front.get())
            .filter(entity -> entity.isPresent(InteractionComponent.class))
            .min(
                Comparator.comparingDouble(
                    entity ->
                        entity
                            .fetch(PositionComponent.class)
                            .map(other -> heroPosition.distanceSquared(other.position()))
                            .orElse(Double.MAX_VALUE)));
    target.ifPresent(
        entity ->
            entity
                .fetch(InteractionComponent.class)
                .orElseThrow()
                .triggerInteraction(entity, hero));
  }

  /** Keeps the hero standing during a pause; the step goes on with the next {@link #tick()}. */
  void hold() {
    if (target == null) return;
    VelocityComponent velocity = hero.fetch(VelocityComponent.class).orElseThrow();
    velocity.clearForces();
    velocity.currentVelocity(Vector2.ZERO);
  }

  /** Steers the hero toward the destination and settles it there; call once per frame. */
  void tick() {
    if (target == null) return;
    VelocityComponent velocity = hero.fetch(VelocityComponent.class).orElseThrow();
    PositionComponent position = hero.fetch(PositionComponent.class).orElseThrow();
    velocity.clearForces();
    Point from = position.position();
    float delta = 1f / Game.frameRate();
    float distance = Point.calculateDistance(from, target);
    if (distance <= SPEED * delta) {
      velocity.currentVelocity(Vector2.ZERO);
      position.position(target);
      PositionSync.syncPosition(hero);
      target = null;
      return;
    }
    velocity.currentVelocity(from.vectorTo(target).normalize().scale(SPEED));
  }
}
