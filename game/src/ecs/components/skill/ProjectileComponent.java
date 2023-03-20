package ecs.components.skill;

import ecs.components.Component;
import ecs.entities.Entity;
import tools.Point;

public class ProjectileComponent extends Component {

    private Point goalLocation;
    private Point startPosition;

    public ProjectileComponent(Entity entity, Point startPosition, Point goalLocation) {
        super(entity);
        this.goalLocation = goalLocation;
        this.startPosition = startPosition;
    }

    /**
     * get gol location
     *
     * @return goal location
     */
    public Point getGoalLocation() {
        return goalLocation;
    }

    /**
     * get start position
     *
     * @return start position
     */
    public Point getStartPosition() {
        return startPosition;
    }
}
