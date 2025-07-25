package contrib.components;

import core.Component;
import core.Entity;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.logging.CustomLogLevel;
import java.util.logging.Logger;

/**
 * Allow an entity to collide with other entities that have a {@link CollideComponent}.
 *
 * <p>The component creates a hitbox (an invisible rectangle) around the associated entity. The
 * default size is {@link #DEFAULT_SIZE}, but you can configure the size of the hitbox in the
 * constructor parameter.
 *
 * <p>The {@link contrib.systems.CollisionSystem} will check if the hitbox collides with another
 * hitbox of a different entity. The system can detect two different types of collisions. The first
 * one is a new collision, which occurs if the collision between the two hitboxes was not present at
 * the last check. If a new collision is detected, the {@link contrib.systems.CollisionSystem} will
 * call {@link #onEnter(Entity, Entity, Direction)}. The second type of collision is a leaving
 * collision, which occurs if a collision that was present in the last check is no longer present in
 * the current check. If a leaving collision is detected, the {@link
 * contrib.systems.CollisionSystem} will call {@link #collideLeave(TriConsumer)}.
 *
 * <p>Example use cases for a collision are pushing an object like a chest or getting damaged by a
 * spiky monster.
 *
 * <p>The {@link #collideEnter} and {@link #collideLeave} are {@link TriConsumer} that will be
 * executed at {@link #onEnter(Entity, Entity, Direction)} or {@link #onLeave(Entity, Entity,
 * Direction)} respectively. The first parameter is the entity of this component, the second
 * parameter is the entity with which the collision is happening, and the third parameter defines
 * the direction from where the collision is happening.
 *
 * @see contrib.systems.CollisionSystem
 */
public final class CollideComponent implements Component {
  /** The default offset of the hit box. */
  public static final Vector2 DEFAULT_OFFSET = Vector2.of(0.25f, 0.25f);

  /** The default size of the hit box. */
  public static final Vector2 DEFAULT_SIZE = Vector2.of(0.5f, 0.5f);

  /** The default collision behaviour. */
  public static final TriConsumer<Entity, Entity, Direction> DEFAULT_COLLIDER = (a, b, c) -> {};

  private final Vector2 offset;
  private final Vector2 size;
  private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
  private TriConsumer<Entity, Entity, Direction> collideEnter;
  private TriConsumer<Entity, Entity, Direction> collideLeave;

  /**
   * Create a new CollisionComponent.
   *
   * @param offset the offset for the hitbox to the position; use {@link #DEFAULT_OFFSET} for the
   *     default offset,
   * @param size the size for the hitbox Use {@link #DEFAULT_SIZE} for the default size.
   * @param collideEnter behaviour if a collision started; use {@link #DEFAULT_COLLIDER} for an
   *     empty function.
   * @param collideLeave behaviour if a collision stopped; use {@link #DEFAULT_COLLIDER} for an
   *     empty function.
   */
  public CollideComponent(
      final Vector2 offset,
      final Vector2 size,
      final TriConsumer<Entity, Entity, Direction> collideEnter,
      final TriConsumer<Entity, Entity, Direction> collideLeave) {
    this.offset = offset;
    this.size = size;
    this.collideEnter = collideEnter;
    this.collideLeave = collideLeave;
  }

  /**
   * Create a new CollisionComponent with a default offset of 0.25f x 0.25f and a default size of
   * 0.5f x 0.5f.
   *
   * @param collideEnter behaviour if a collision started; use {@link #DEFAULT_COLLIDER} for an
   *     empty function.
   * @param collideLeave behaviour if a collision stopped; use {@link #DEFAULT_COLLIDER} for an
   *     empty function.
   */
  public CollideComponent(
      final TriConsumer<Entity, Entity, Direction> collideEnter,
      final TriConsumer<Entity, Entity, Direction> collideLeave) {
    this(DEFAULT_OFFSET, DEFAULT_SIZE, collideEnter, collideLeave);
  }

  /**
   * Create a new CollisionComponent with a default offset of 0.25f x 0.25f and a default size of
   * 0.5f x 0.5f and empty collide functions.
   */
  public CollideComponent() {
    this(CollideComponent.DEFAULT_COLLIDER, CollideComponent.DEFAULT_COLLIDER);
  }

  /**
   * Function to be executed at the beginning of a collision.
   *
   * @param entity associated entity of this component.
   * @param other Component of the colliding entity
   * @param direction Direction in which the collision happens
   */
  public void onEnter(final Entity entity, final Entity other, final Direction direction) {
    if (collideEnter != null) collideEnter.accept(entity, other, direction);
  }

  /**
   * Function to be executed at the end of a collision.
   *
   * @param entity associated entity of this component.
   * @param other Component of the colliding entity
   * @param direction Direction in which the collision happens
   */
  public void onLeave(final Entity entity, final Entity other, final Direction direction) {
    if (collideLeave != null) {
      LOGGER.log(
          CustomLogLevel.DEBUG,
          this.getClass().getSimpleName()
              + " is processing collision between entities '"
              + entity.getClass().getSimpleName()
              + "' and '"
              + other.getClass().getSimpleName()
              + "'.");
      collideLeave.accept(entity, other, direction);
    }
  }

  /**
   * Get the bottom-left point of the hitbox.
   *
   * @param entity associated entity of this component.
   * @return Bottom-left point of the entity's hitbox
   */
  public Point bottomLeft(final Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    return pc.position().translate(offset);
  }

  /**
   * Get the top-right point of the hitbox.
   *
   * @param entity associated entity of this component.
   * @return Top-right point of the entity's hitbox
   */
  public Point topRight(final Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    return pc.position().translate(offset).translate(size);
  }

  /**
   * Get the center point of the hitbox.
   *
   * @param entity associated entity of this component.
   * @return Center point of the entity's hitbox
   */
  public Point center(final Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    return pc.position().translate(offset).translate(size.scale(0.5f));
  }

  /**
   * Set function to execute at start of a collision.
   *
   * @param collideEnter new collideMethod of the associated entity
   */
  public void collideEnter(TriConsumer<Entity, Entity, Direction> collideEnter) {
    this.collideEnter = collideEnter;
  }

  /**
   * Set function to execute at end of a collision.
   *
   * @param collideLeave new collideMethod of the associated entity
   */
  public void collideLeave(TriConsumer<Entity, Entity, Direction> collideLeave) {
    this.collideLeave = collideLeave;
  }

  /**
   * Get the size of the hitbox.
   *
   * @return the size of the component
   */
  public Vector2 size() {
    return Vector2.of(size);
  }
}
