package graphic;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import ecs.components.PositionComponent;
import tools.Point;

/** Sauron's eye. */
public class DungeonCamera extends OrthographicCamera {
    private PositionComponent follows;
    private Point focusPoint;

    /**
     * Creates a new camera.
     *
     * @param follows the PositionComponent the camera should follow, <code>null</code> for default
     *     coordinates
     * @param vw viewport width
     * @param vh viewport height
     */
    public DungeonCamera(PositionComponent follows, float vw, float vh) {
        super(vw, vh);
        if (follows != null) {
            this.follows = follows;
        }
    }

    /** Updates camera position. */
    public void update() {
        if (follows != null) {
            Point fp = getFollowedObject().getPosition();
            position.set(fp.x, fp.y, 0);
        } else {
            if (focusPoint == null) {
                focusPoint = new Point(0, 0);
            }
            position.set(focusPoint.x, focusPoint.y, 0);
        }
        super.update();
    }

    /**
     * Sets the entity to follow.
     *
     * @param follows PositionComponent to follow
     */
    public void follow(PositionComponent follows) {
        this.follows = follows;
    }

    /**
     * @return the PositionComponent the camera currently follows
     */
    public PositionComponent getFollowedObject() {
        return follows;
    }

    /**
     * Stops following and set the camera on a fix position.
     *
     * @param focusPoint <code>Point</code> to set the camera on
     */
    public void setFocusPoint(Point focusPoint) {
        follows = null;
        this.focusPoint = focusPoint;
    }

    /**
     * Checks if point (x,y) is probably visible on screen. Points that are not visible should not
     * be rendered.
     */
    public boolean isPointInFrustum(float x, float y) {
        final float OFFSET = 1f;
        BoundingBox bounds =
                new BoundingBox(
                        new Vector3(x - OFFSET, y - OFFSET, 0),
                        new Vector3(x + OFFSET, y + OFFSET, 0));
        return frustum.boundsInFrustum(bounds);
    }
}
