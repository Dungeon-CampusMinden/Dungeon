package ecs.components;

import ecs.entities.Entity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import level.elements.tile.Tile;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import tools.Point;

@DSLType(name = "hitbox_component")
public class HitboxComponent extends Component {
    public static final String name = "HitboxComponent";
    private final /*@DSLTypeMember(name="offset")*/ Point offset;
    private final /*@DSLTypeMember(name="size")*/ Point size;
    private final Method method;

    public HitboxComponent(Entity entity, Point offset, Point size, Method method) {
        super(entity, name);
        this.offset = offset;
        this.size = size;
        this.method = method;
    }

    /**
     * Creates A Hitbox with a default offset of 0.25f x 0.25f and a default size of 0.5f x 0.5f
     *
     * @param entity associated entity
     * @param method callback function in case of collision
     */
    public HitboxComponent(Entity entity, Method method) {
        this(entity, new Point(0.25f, 0.25f), new Point(0.5f, 0.5f), method);
    }

    /**
     * Creates A Hitbox with a default offset of 0.25f x 0.25f and a default size of 0.5f x 0.5f
     *
     * @param entity associated entity
     */
    public HitboxComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity, name);
        offset = new Point(0.25f, 0.25f);
        size = new Point(0.5f, 0.5f);
        try {
            Class[] cArg = new Class[2];
            cArg[0] = HitboxComponent.class;
            cArg[1] = Tile.Direction.class;
            method = HitboxComponent.class.getMethod("dummyHitboxMethod", cArg);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void collide(HitboxComponent other, Tile.Direction direction)
            throws InvocationTargetException, IllegalAccessException {
        method.invoke(this, other, direction);
    }

    public Point getBottomLeft() {
        PositionComponent pc =
                (PositionComponent)
                        getEntity()
                                .getComponent(PositionComponent.name)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        return new Point(pc.getPosition().x + offset.x, pc.getPosition().y + offset.y);
    }

    public Point getTopRight() {
        PositionComponent pc =
                (PositionComponent)
                        getEntity()
                                .getComponent(PositionComponent.name)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        return new Point(
                pc.getPosition().x + offset.x + size.x, pc.getPosition().y + offset.y + size.y);
    }

    public Point getCenter() {
        PositionComponent pc =
                (PositionComponent)
                        getEntity()
                                .getComponent(PositionComponent.name)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        return new Point(
                pc.getPosition().x + offset.x + size.x / 2,
                pc.getPosition().y + offset.y + size.y / 2);
    }

    public static void dummyHitboxMethod(HitboxComponent other, Tile.Direction from) {
        System.out.println("COLLIDE");
    }
}
