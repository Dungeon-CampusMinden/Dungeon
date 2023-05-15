package contrib.components;

import core.Component;
import core.Entity;
import core.utils.Point;

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
