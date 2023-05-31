package core.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import core.Entity;
import core.Game;
import core.System;
import core.components.CameraComponent;
import core.components.PositionComponent;
import core.utils.Constants;
import core.utils.Point;

public class CameraSystem extends System {

    public static final OrthographicCamera CAMERA =
            new OrthographicCamera(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

    public CameraSystem() {
        super(CameraComponent.class, PositionComponent.class);
    }

    @Override
    public void execute() {
        if (getEntityStream().findAny().isEmpty()) focus();
        else getEntityStream().forEach(this::focus);
        CAMERA.update();
    }

    private void focus() {
        Point focusPoint;
        if (Game.currentLevel == null) focusPoint = new Point(0, 0);
        else focusPoint = Game.currentLevel.getStartTile().getCoordinateAsPoint();
        focus(focusPoint);
    }

    private void focus(Entity entity) {
        PositionComponent pc =
                (PositionComponent) entity.getComponent(PositionComponent.class).get();
        focus(pc.getPosition());
    }

    private void focus(Point point) {
        CAMERA.position.set(point.x, point.y, 0);
    }

    /**
     * Checks if point (x,y) is probably visible on screen. Points that are not visible should not
     * be rendered.
     */
    public static boolean isPointInFrustum(float x, float y) {
        final float OFFSET = 1f;
        BoundingBox bounds =
                new BoundingBox(
                        new Vector3(x - OFFSET, y - OFFSET, 0),
                        new Vector3(x + OFFSET, y + OFFSET, 0));
        return CAMERA.frustum.boundsInFrustum(bounds);
    }
}
