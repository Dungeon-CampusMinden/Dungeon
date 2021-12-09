package graphic;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import interfaces.IEntity;
import tools.Point;

/** Sauron's eye. */
public class DungeonCamera extends OrthographicCamera {
    private IEntity follows;
    private Point focusPoint;

    /**
     * Creates a new camera.
     *
     * @param follows the entity the camera should follow, <code>null</code> for default coordinates
     * @param vw virtual width
     * @param vh virtual height
     */
    public DungeonCamera(IEntity follows, float vw, float vh) {
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
     * @param follows entity to follow
     */
    public void follow(IEntity follows) {
        this.follows = follows;
    }

    /** @return the entity the camera currently follows */
    public IEntity getFollowedObject() {
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
     * Checks if the point (x,y) is probably been seen on the screen. Otherwise, don't redender this
     * point.
     */
    public boolean isPointInFrustum(float x, float y) {
        final float OFFSET = 1f;
        BoundingBox bounds =
                new BoundingBox(
                        new Vector3(x - OFFSET, y - OFFSET, 0),
                        new Vector3(x + OFFSET, y + OFFSET, 0));
        return frustum.boundsInFrustum(bounds);
    }

    /** @return the camara frustum */
    public Frustum getFrustum() {
        return frustum;
    }
}
