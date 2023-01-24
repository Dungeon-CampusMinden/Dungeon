package ecs.components;

import ecs.entities.Entity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import tools.Point;

public class HitboxComponent extends Component {
    public static final String name = "HitboxComponent";
    private Point offset;
    private Point size;
    private Method method;

    public HitboxComponent(Entity entity, Point offset, Point size, Method method) {
        super(entity, name);
        this.offset = offset;
        this.size = size;
        this.method = method;
    }

    /**
     * Creates A Hitbox with a default offset of 0.25f x 0.25f and a default size of 0.5f x 0.5f
     *
     * @param entity
     * @param method
     */
    public HitboxComponent(Entity entity, Method method) {
        this(entity, new Point(0.25f, 0.25f), new Point(0.5f, 0.5f), method);
    }

    public HitboxComponent(Entity entity) {
        super(entity, name);
        offset = new Point(0.25f, 0.25f);
        size = new Point(0.5f, 0.5f);
        try {
            method = HitboxComponent.class.getMethod("dummyHitboxMethod");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        ;
    }

    public void collide(Object... args) throws InvocationTargetException, IllegalAccessException {
        method.invoke(this, args);
    }

    public Point getBottomLeft() {
        PositionComponent pc = (PositionComponent) getEntity().getComponent("PositionComponent");
        return new Point(pc.getPosition().x + offset.x, pc.getPosition().y + offset.y);
    }

    public Point getTopRight() {
        PositionComponent pc = (PositionComponent) getEntity().getComponent("PositionComponent");
        return new Point(
                pc.getPosition().x + offset.x + size.x, pc.getPosition().y + offset.y + size.y);
    }

    public Point getCenter() {
        PositionComponent pc = (PositionComponent) getEntity().getComponent("PositionComponent");
        return new Point(
                pc.getPosition().x + offset.x + size.x / 2,
                pc.getPosition().y + offset.y + size.y / 2);
    }

    public static void dummyHitboxMethod() {
        System.out.println("COLIDE");
    }
}
