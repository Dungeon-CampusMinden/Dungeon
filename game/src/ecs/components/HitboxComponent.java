package ecs.components;

import ecs.entities.Entity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import tools.Point;

public class HitboxComponent extends Component {
    public static final String NAME = "HitboxComponent";
    private Point offset;
    private Point size;
    private Method method;

    public HitboxComponent(Entity entity, Point offset, Point size, Method method) {
        super(entity);
        this.offset = offset;
        this.size = size;
        this.method = method;
    }

    public void collide(Object... args) throws InvocationTargetException, IllegalAccessException {
        method.invoke(this, args);
    }

    public Point getBottomLeft() {
        return new Point(offset.x, offset.y);
    }

    public Point getTopRight() {
        return new Point(offset.x + size.x, offset.y + size.y);
    }

    public Point getCenter() {
        return new Point(offset.x + size.x / 2, offset.y + size.y / 2);
    }
}
