package ecs.components;

import ecs.components.collision.ICollide;
import ecs.entities.Entity;
import java.lang.reflect.InvocationTargetException;
import level.elements.tile.Tile;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import tools.Point;

@DSLType(name = "hitbox_component")
public class HitboxComponent extends Component {
    final  /*@DSLTypeMember(name="offset")*/ Point offset;
    final /*@DSLTypeMember(name="size")*/ Point size;
    private ICollide collideMethod;

    public HitboxComponent(Entity entity, Point offset, Point size, ICollide collideMethod) {
        super(entity);
        this.offset = offset;
        this.size = size;
        this.collideMethod = collideMethod;
    }

    /**
     * Creates A Hitbox with a default offset of 0.25f x 0.25f and a default size of 0.5f x 0.5f
     *
     * @param entity associated entity
     * @param collideMethod behaviour if a collision happens
     */
    public HitboxComponent(Entity entity, ICollide collideMethod) {
        this(entity, new Point(0.25f, 0.25f), new Point(0.5f, 0.5f), collideMethod);
    }

    public HitboxComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        offset = new Point(0.25f, 0.25f);
        size = new Point(0.5f, 0.5f);

        collideMethod = HitboxComponent.dummyHitboxMethod();
    }

    private static ICollide dummyHitboxMethod() {
        return (a, b, c) -> System.out.println("Collide");
    }

    /**
     * @param other hitbox of another entity
     * @param direction direction in which the collision happens
     * @throws InvocationTargetException checked exception that wraps an exception thrown by an invoked method or constructor
     * @throws IllegalAccessException .
     */
    public void collide(HitboxComponent other, Tile.Direction direction)
            throws InvocationTargetException, IllegalAccessException {
        collideMethod.onCollision(this.entity, other.entity, direction);
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
                                        () -> new MissingComponentException("PositionComponent"));
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
                                        () -> new MissingComponentException("PositionComponent"));
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
                                        () -> new MissingComponentException("PositionComponent"));
        return new Point(
                pc.getPosition().x + offset.x + size.x / 2,
                pc.getPosition().y + offset.y + size.y / 2);
    }

    /**
     * @return the collideMethod of the associated entity
     */
    public ICollide getCollideMethod() {
        return collideMethod;
    }

    /**
     * @param collideMethod new collideMethod of the associated entity
     */
    public void setCollideMethod(ICollide collideMethod) {
        this.collideMethod = collideMethod;
    }
}
