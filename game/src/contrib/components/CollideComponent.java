package contrib.components;

import core.Component;
import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.MissingComponentException;
import core.utils.logging.CustomLogLevel;
import core.utils.logging.LoggerConfig;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;

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
 * call {@link #onEnter(CollideComponent, Tile.Direction)}. The second type of collision is a
 * leaving collision, which occurs if a collision that was present in the last check is no longer
 * present in the current check. If a leaving collision is detected, the {@link
 * contrib.systems.CollisionSystem} will call {@link #collideLeave(TriConsumer)}.
 *
 * <p>Example use cases for a collision are pushing an object like a chest or getting damaged by a
 * spiky monster.
 *
 * <p>The {@link #collideEnter} and {@link #collideLeave} are {@link TriConsumer} that will be
 * executed at {@link #onEnter(CollideComponent, Tile.Direction)} or {@link
 * #onLeave(CollideComponent, Tile.Direction)} respectively. The first parameter is the entity of
 * this component, the second parameter is the entity with which the collision is happening, and the
 * third parameter defines the direction from where the collision is happening.
 *
 * @see contrib.systems.CollisionSystem
 */
@DSLType(name = "hitbox_component")
public final class CollideComponent extends Component {
    public static final Point DEFAULT_OFFSET = new Point(0.25f, 0.25f);
    public static final Point DEFAULT_SIZE = new Point(0.5f, 0.5f);
    public static final TriConsumer<Entity, Entity, Tile.Direction> DEFAULT_COLLIDER =
            (a, b, c) -> {};

    private final Point offset;
    private final Point size;
    private TriConsumer<Entity, Entity, Tile.Direction> collideEnter;
    private TriConsumer<Entity, Entity, Tile.Direction> collideLeave;
    private final Logger LOGGER = LoggerConfig.getLogger(this.getClass().getName());

    /**
     * Create a new CollisionComponent and add it to the associated entity.
     *
     * @param entity associated entity
     * @param offset the offset for the hitbox to the position; use {@link #DEFAULT_OFFSET} for the
     *     default offset,
     * @param size the size for the hitboxUse {@link #DEFAULT_SIZE} for the default size.
     * @param collideEnter behaviour if a collision started; use {@link #DEFAULT_COLLIDER} for an
     *     empty function.
     * @param collideLeave behaviour if a collision stopped; use {@link #DEFAULT_COLLIDER} for an
     *     empty function.
     */
    public CollideComponent(
            final Entity entity,
            final Point offset,
            final Point size,
            final TriConsumer<Entity, Entity, Tile.Direction> collideEnter,
            final TriConsumer<Entity, Entity, Tile.Direction> collideLeave) {
        super(entity);
        this.offset = offset;
        this.size = size;
        this.collideEnter = collideEnter;
        this.collideLeave = collideLeave;
    }

    /**
     * Create a new CollisionComponent with a default offset of 0.25f x 0.25f and a default size of
     * 0.5f x 0.5f and add it to the associated entity.
     *
     * @param entity associated entity
     * @param collideEnter behaviour if a collision started; use {@link #DEFAULT_COLLIDER} for an
     *     empty function.
     * @param collideLeave behaviour if a collision stopped; use {@link #DEFAULT_COLLIDER} for an
     *     empty function.
     */
    public CollideComponent(
            final Entity entity,
            final TriConsumer<Entity, Entity, Tile.Direction> collideEnter,
            final TriConsumer<Entity, Entity, Tile.Direction> collideLeave) {
        this(entity, DEFAULT_OFFSET, DEFAULT_SIZE, collideEnter, collideLeave);
    }

    /**
     * Create a new CollisionComponent with a default offset of 0.25f x 0.25f and a default size of
     * 0.5f x 0.5f and empty collide functions.
     *
     * @param entity associated entity
     */
    public CollideComponent(@DSLContextMember(name = "entity") final Entity entity) {
        this(entity, CollideComponent.DEFAULT_COLLIDER, CollideComponent.DEFAULT_COLLIDER);
    }

    /**
     * Function to be executed at the beginning of a collision.
     *
     * @param other Component of the colliding entity
     * @param direction Direction in which the collision happens
     */
    public void onEnter(final CollideComponent other, final Tile.Direction direction) {
        if (collideEnter != null) collideEnter.accept(this.entity, other.entity, direction);
    }

    /**
     * Function to be executed at the end of a collision.
     *
     * @param other Component of the colliding entity
     * @param direction Direction in which the collision happens
     */
    public void onLeave(final CollideComponent other, final Tile.Direction direction) {
        if (collideLeave != null) {
            LOGGER.log(
                    CustomLogLevel.DEBUG,
                    this.getClass().getSimpleName()
                            + " is processing collision between entities '"
                            + entity.getClass().getSimpleName()
                            + "' and '"
                            + other.getClass().getSimpleName()
                            + "'.");
            collideLeave.accept(this.entity, other.entity, direction);
        }
    }

    /**
     * Get the bottom-left point of the hitbox.
     *
     * @return Bottom-left point of the entity's hitbox
     */
    public Point bottomLeft() {
        PositionComponent pc =
                entity().fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity(), PositionComponent.class));
        return new Point(pc.position().x + offset.x, pc.position().y + offset.y);
    }

    /**
     * Get the top-right point of the hitbox.
     *
     * @return Top-right point of the entity's hitbox
     */
    public Point topRight() {
        PositionComponent pc =
                entity().fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity(), PositionComponent.class));
        return new Point(pc.position().x + offset.x + size.x, pc.position().y + offset.y + size.y);
    }

    /**
     * Get the center point of the hitbox.
     *
     * @return Center point of the entity's hitbox
     */
    public Point center() {
        PositionComponent pc =
                entity().fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity(), PositionComponent.class));
        return new Point(
                pc.position().x + offset.x + size.x / 2, pc.position().y + offset.y + size.y / 2);
    }

    /**
     * Set function to execute at start of a collision.
     *
     * @param collideEnter new collideMethod of the associated entity
     */
    public void collideEnter(TriConsumer<Entity, Entity, Tile.Direction> collideEnter) {
        this.collideEnter = collideEnter;
    }

    /**
     * Set function to execute at end of a collision.
     *
     * @param collideLeave new collideMethod of the associated entity
     */
    public void collideLeave(TriConsumer<Entity, Entity, Tile.Direction> collideLeave) {
        this.collideLeave = collideLeave;
    }
}
