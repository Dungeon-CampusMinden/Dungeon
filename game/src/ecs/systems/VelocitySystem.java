package ecs.systems;

import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import mydungeon.ECS;
import tools.Point;

/** MovementSystem is a system that updates the position of entities */
public class VelocitySystem extends ECS_System {

    /** Updates the position of all entities based on their velocity */
    public void update() {
        for (Entity entity : ECS.entities) {

            VelocityComponent velocity =
                    (VelocityComponent) entity.getComponent(VelocityComponent.name);
            if (velocity != null) {
                PositionComponent position =
                        (PositionComponent) entity.getComponent(PositionComponent.name);
                if (position != null) {

                    // Update the position based on the velocity
                    float newX = position.getPosition().x + velocity.getX();
                    float newY = position.getPosition().y + velocity.getY();

                    Point newPosition = new Point(newX, newY);
                    if (ECS.currentLevel.getTileAt(newPosition.toCoordinate()).isAccessible()) {
                        position.setPosition(newPosition);
                    }
                }
            }
        }
    }
}
