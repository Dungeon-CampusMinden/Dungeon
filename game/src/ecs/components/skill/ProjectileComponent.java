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

    public Point getGoalLocation() {
        return goalLocation;
    }

    public Point getStartPosition() {
        return startPosition;
    }
}
