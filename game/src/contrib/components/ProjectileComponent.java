package contrib.components;

import core.Component;
import core.Entity;
import core.utils.Point;

/**
 * ProjectileComponent saves the start position and goal location of a projectile.
 *
 * <p>The {@link #getGoalLocation()} method retrieves the goal position of the projectile. The
 * {@link #getStartPosition()} method retrieves the start position of the projectile.
 */
public class ProjectileComponent extends Component {

    private Point goalLocation;
    private Point startPosition;

    public ProjectileComponent(Entity entity, Point startPosition, Point goalLocation) {
        super(entity);
        this.goalLocation = goalLocation;
        this.startPosition = startPosition;
    }

    /**
     * gets the goal position of the projectile
     *
     * @return goal position of the projectile
     */
    public Point getGoalLocation() {
        return goalLocation;
    }

    /**
     * gets the start position of the projectile
     *
     * @return start position of the projectile
     */
    public Point getStartPosition() {
        return startPosition;
    }
}
