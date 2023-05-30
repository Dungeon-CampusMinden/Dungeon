package contrib.components;

import core.utils.TriConsumer;

import core.Component;
import core.Entity;
import core.components.PositionComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.logging.CustomLogLevel;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;

import java.util.logging.Logger;

@DSLType(name = "hitbox_component")
public class CollideComponent extends Component {
    public static final Point DEFAULT_OFFSET = new Point(0.25f, 0.25f);
    public static final Point DEFAULT_SIZE = new Point(0.5f, 0.5f);
    public static final TriConsumer<Entity, Entity, Tile.Direction> DEFAULT_COLLIDER =
            (a, b, c) -> System.out.println("Collide");
    private /*@DSLTypeMember(name="offset")*/ Point offset;
    private /*@DSLTypeMember(name="size")*/ Point size;
    private TriConsumer<Entity, Entity, Tile.Direction> collideEnter;
    private TriConsumer<Entity, Entity, Tile.Direction> collideLeave;
    private final Logger hitboxLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates A Hitbox
     *
     * @param entity associated entity
     * @param offset the offset for the hitbox to the position
     * @param size the size for the hitbox
     * @param collideEnter behaviour if a collision started
     * @param collideLeave behaviour if a collision stopped
     */
    public CollideComponent(
            Entity entity,
            Point offset,
            Point size,
            TriConsumer<Entity, Entity, Tile.Direction> collideEnter,
            TriConsumer<Entity, Entity, Tile.Direction> collideLeave) {
        super(entity);
        this.offset = offset;
        this.size = size;
        this.collideEnter = collideEnter;
        this.collideLeave = collideLeave;
    }

    /**
     * Creates A Hitbox with a default offset of 0.25f x 0.25f and a default size of 0.5f x 0.5f
     *
     * @param entity associated entity
     * @param collideEnter behaviour if a collision started
     * @param collideLeave behaviour if a collision stopped
     */
    public CollideComponent(
            Entity entity,
            TriConsumer<Entity, Entity, Tile.Direction> collideEnter,
            TriConsumer<Entity, Entity, Tile.Direction> collideLeave) {
        this(entity, DEFAULT_OFFSET, DEFAULT_SIZE, collideEnter, collideLeave);
    }

    /**
     * Creates A Hitbox with a default offset of 0.25f x 0.25f and a default size of 0.5f x 0.5f and
     * defaultCollideMethods
     *
     * @param entity associated entity
     */
    public CollideComponent(@DSLContextMember(name = "entity") Entity entity) {
        this(entity, CollideComponent.DEFAULT_COLLIDER, CollideComponent.DEFAULT_COLLIDER);
    }

    /**
     * @param other hitbox of another entity
     * @param direction direction in which the collision happens
     */
    public void onEnter(CollideComponent other, Tile.Direction direction) {
        if (collideEnter != null) collideEnter.accept(this.entity, other.entity, direction);
    }

    /**
     * @param other hitbox of another entity
     * @param direction direction in which the collision happens
     */
    public void onLeave(CollideComponent other, Tile.Direction direction) {
        if (collideLeave != null) {
            hitboxLogger.log(
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
     * @return bottom left point of entity's hitbox
     */
    public Point getBottomLeft() {
        PositionComponent pc =
                (PositionComponent)
                        getEntity()
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        CollideComponent::getMissingPositionComponentException);
        return new Point(pc.getPosition().x + offset.x, pc.getPosition().y + offset.y);
    }

    /**
     * @return top right point of entity's hitbox
     */
    public Point getTopRight() {
        PositionComponent pc =
                (PositionComponent)
                        getEntity()
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        CollideComponent::getMissingPositionComponentException);
        return new Point(
                pc.getPosition().x + offset.x + size.x, pc.getPosition().y + offset.y + size.y);
    }

    /**
     * @return center point of entity's hitbox
     */
    public Point getCenter() {
        PositionComponent pc =
                (PositionComponent)
                        getEntity()
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        CollideComponent::getMissingPositionComponentException);
        return new Point(
                pc.getPosition().x + offset.x + size.x / 2,
                pc.getPosition().y + offset.y + size.y / 2);
    }

    /**
     * @param collideEnter new collideMethod of the associated entity
     */
    public void setCollideEnter(TriConsumer<Entity, Entity, Tile.Direction> collideEnter) {
        this.collideEnter = collideEnter;
    }

    /**
     * @param collideLeave new collideMethod of the associated entity
     */
    public void setCollideLeave(TriConsumer<Entity, Entity, Tile.Direction> collideLeave) {
        this.collideLeave = collideLeave;
    }

    private static MissingComponentException getMissingPositionComponentException() {
        return new MissingComponentException(
                PositionComponent.class.getName() + " in " + CollideComponent.class.getName());
    }
}
