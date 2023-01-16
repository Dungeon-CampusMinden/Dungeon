package ecs.systems;

import ecs.components.Component;
import ecs.components.ComponentStore;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entitys.Entity;
import java.util.Map;
import tools.Point;

/** MovementSystem is a system that updates the position of entities */
public class MovementSystem implements System {

    private final ComponentStore positionStore;
    private final ComponentStore velocityStore;

    public MovementSystem(ComponentStore positionStore, ComponentStore velocityStore) {
        this.positionStore = positionStore;
        this.velocityStore = velocityStore;
    }

    /** Updates the position of all entities based on their velocity */
    public void update() {
        for (Map.Entry<Entity, Component> entry : positionStore.getStore().entrySet()) {
            Entity entity = entry.getKey();
            PositionComponent position = (PositionComponent) entry.getValue();
            VelocityComponent velocity = (VelocityComponent) velocityStore.getComponent(entity);

            // Update the position based on the velocity
            float newX = position.getPosition().x + velocity.getX();
            float newY = position.getPosition().y + velocity.getY();

            // todo check if TIle is Accessible else do not update
            Point newPosition = new Point(newX, newY);
            position.setPosition(newPosition);
        }
    }
}
