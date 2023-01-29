package ecs.systems;

import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import graphic.Animation;
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
                    float newX = position.getPosition().x + velocity.getCurrentXVelocity();
                    float newY = position.getPosition().y + velocity.getCurrentYVelocity();
                    Point newPosition = new Point(newX, newY);
                    if (ECS.currentLevel.getTileAt(newPosition.toCoordinate()).isAccessible()) {
                        position.setPosition(newPosition);
                        movementAnimation(entity);
                        velocity.setCurrentYVelocity(0);
                        velocity.setCurrentXVelocity(0);
                    }
                }
            }
        }
    }

    private void movementAnimation(Entity entity) {
        AnimationComponent ac = (AnimationComponent) entity.getComponent(AnimationComponent.name);
        if (ac != null) {
            boolean backup = true;
            Animation newCurrentAnimation;
            VelocityComponent vc = (VelocityComponent) entity.getComponent(VelocityComponent.name);
            float x = vc.getCurrentXVelocity();
            if (x > 0) newCurrentAnimation = vc.getMoveRightAnimation();
            else if (x < 0) newCurrentAnimation = vc.getMoveLeftAnimation();
            // idle
            else {
                if (ac.getCurrentAnimation() == ac.getIdleLeft()
                        || ac.getCurrentAnimation() == vc.getMoveLeftAnimation())
                    newCurrentAnimation = ac.getIdleLeft();
                else newCurrentAnimation = ac.getIdleRight();
            }
            ac.setCurrentAnimation(newCurrentAnimation);
        }
    }
}
