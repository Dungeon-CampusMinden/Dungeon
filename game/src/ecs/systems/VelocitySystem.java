package ecs.systems;

import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import java.util.Map;
import mydungeon.ECS;
import tools.Point;

/** MovementSystem is a system that updates the position of entities */
public class VelocitySystem extends ECS_System {

    /** Updates the position of all entities based on their velocity */
    public void update() {
        for (Map.Entry<Entity, VelocityComponent> entry : ECS.velocityComponentMap.entrySet()) {
            Entity entity = entry.getKey();
            VelocityComponent velocity = entry.getValue();
            PositionComponent position = ECS.positionComponentMap.get(entity);
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
