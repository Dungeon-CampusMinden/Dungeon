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
 * <p>The {@link #collideHold} will be executed for ongoing collisions.
 *
 * @see contrib.systems.CollisionSystem
 */
public final class CollideComponent implements Component {
  /** The default offset of the hit box. */
  public static final Vector2 DEFAULT_OFFSET = Vector2.of(0.1f, 0.1f);

  /** The default size of the hit box. */
  public static final Vector2 DEFAULT_SIZE = Vector2.of(0.8f, 0.8f);

  /**
   * The default collision behavior for projectiles.
   *
   * <p>This handler does nothing and can be used as a placeholder or default value for collision
   * events (enter, leave, or hold).
   *
   * <p>Parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   */
  public static final TriConsumer<Entity, Entity, Direction> DEFAULT_COLLIDER = (a, b, c) -> {};

  private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

  private final Vector2 offset;
  private final Vector2 size;
  private boolean isSolid = true;

  /**
   * Handler invoked when the entity first collides with another entity (collision enter).
   *
   * <p>Parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   */
  private TriConsumer<Entity, Entity, Direction> collideEnter;

  /**
   * Handler invoked when the entity stops colliding with another entity (collision leave).
   *
   * <p>Parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   */
  private TriConsumer<Entity, Entity, Direction> collideLeave;

  /**
   * Handler invoked while the entity remains in collision with another entity (collision hold).
   *
   * <p>Parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   */
  private TriConsumer<Entity, Entity, Direction> collideHold;

  /**
   * Creates a new {@code CollideComponent}.
   *
   * <p>This component handles collisions for an entity using a hitbox defined by {@code offset} and
   * {@code size}, and custom behavior for collision events.
   *
   * <p>The collision handlers use a {@link TriConsumer} with three parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   *
   * @param offset the offset of the hitbox relative to the entity's position; use {@link
   *     #DEFAULT_OFFSET} for the default offset
   * @param size the size of the hitbox; use {@link #DEFAULT_SIZE} for the default size handler
   */
  public CollideComponent(final Vector2 offset, final Vector2 size) {
    this.offset = offset;
    this.size = size;
    this.collideEnter = DEFAULT_COLLIDER;
    this.collideLeave = DEFAULT_COLLIDER;
    this.collideHold = DEFAULT_COLLIDER;
  }

  /**
   * Creates a new {@code CollideComponent}.
   *
   * <p>This component handles collisions for an entity using a hitbox defined by {@code offset} and
   * {@code size}, and custom behavior for collision events.
   *
   * <p>The collision handlers use a {@link TriConsumer} with three parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   *
   * @param offset the offset of the hitbox relative to the entity's position; use {@link
   *     #DEFAULT_OFFSET} for the default offset
   * @param size the size of the hitbox; use {@link #DEFAULT_SIZE} for the default size
   * @param collideEnter behavior when a collision starts; use {@link #DEFAULT_COLLIDER} for a no-op
   *     handler
   * @param collideLeave behavior when a collision ends; use {@link #DEFAULT_COLLIDER} for a no-op
   *     handler
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
    this.collideHold = DEFAULT_COLLIDER;
  }

  /**
   * Creates a new {@code CollideComponent} with a default offset of 0.25f x 0.25f and a default
   * size of 0.5f x 0.5f.
   *
   * <p>The collision handlers use a {@link TriConsumer} with three parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   *
   * @param collideEnter behavior when a collision starts; use {@link #DEFAULT_COLLIDER} for a no-op
   *     handler
   * @param collideLeave behavior when a collision ends; use {@link #DEFAULT_COLLIDER} for a no-op
   *     handler
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
   * Function to be executed at ongoing collisions.
   *
   * @param entity associated entity of this component.
   * @param other Component of the colliding entity
   * @param direction Direction in which the collision happens
   */
  public void onHold(final Entity entity, final Entity other, final Direction direction) {
    if (collideHold != null) {
      LOGGER.log(
          CustomLogLevel.DEBUG,
          this.getClass().getSimpleName()
              + " is processing collision hold between entities '"
              + entity.getClass().getSimpleName()
              + "' and '"
              + other.getClass().getSimpleName()
              + "'.");
      collideHold.accept(entity, other, direction);
    }
  }

  /**
   * Sets the callback function for ongoing collisions (collision hold).
   *
   * <p>The collision handler uses a {@link TriConsumer} with three parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   *
   * @param collideHold the new callback function to handle collisions while they are ongoing
   */
  public void onHold(TriConsumer<Entity, Entity, Direction> collideHold) {
    this.collideHold = collideHold;
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
   * Sets the callback function to execute at the start of a collision (collision enter).
   *
   * <p>The collision handler uses a {@link TriConsumer} with three parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   *
   * @param collideEnter the new callback function to handle collision start events
   */
  public void collideEnter(TriConsumer<Entity, Entity, Direction> collideEnter) {
    this.collideEnter = collideEnter;
  }

  /**
   * Sets the callback function to execute at the end of a collision (collision leave).
   *
   * <p>The collision handler uses a {@link TriConsumer} with three parameters:
   *
   * <ul>
   *   <li>the first entity: the entity that holds this {@code CollideComponent}
   *   <li>the second entity: the entity it collides with
   *   <li>the third parameter: the collision direction, relative to the first entity
   * </ul>
   *
   * @param collideLeave the new callback function to handle collision end events
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

  /**
   * Get the offset of the hitbox.
   *
   * @return the offset of the component
   */
  public Vector2 offset() {
    return Vector2.of(offset);
  }

  /**
   * Get the solid state of the hitbox. Solid entities will not be able to pass through each other.
   *
   * @return true if the hitbox is solid, false otherwise
   */
  public boolean isSolid() {
    return isSolid;
  }

  /**
   * Set the solid state of the hitbox. Solid entities will not be able to pass through each other.
   *
   * @param isSolid true if the hitbox should be solid, false otherwise
   * @return this component for chaining
   */
  public CollideComponent isSolid(boolean isSolid) {
    this.isSolid = isSolid;
    return this;
  }
}
