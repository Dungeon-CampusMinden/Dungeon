package ecs.systems;

import ecs.components.AnimationComponent;
import ecs.components.MissingComponentException;
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

            entity.getComponent(VelocityComponent.class)
                    .ifPresent(
                            vc -> {
                                final PositionComponent position =
                                        (PositionComponent)
                                                entity.getComponent(PositionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        new MissingComponentException(
                                                                                "PositionComponent"));

                                // Update the position based on the velocity
                                float newX =
                                        position.getPosition().x
                                                + ((VelocityComponent) vc).getCurrentXVelocity();
                                float newY =
                                        position.getPosition().y
                                                + ((VelocityComponent) vc).getCurrentYVelocity();
                                Point newPosition = new Point(newX, newY);
                                if (ECS.currentLevel
                                        .getTileAt(newPosition.toCoordinate())
                                        .isAccessible()) {
                                    position.setPosition(newPosition);
                                    movementAnimation(entity);
                                    ((VelocityComponent) vc).setCurrentYVelocity(0);
                                    ((VelocityComponent) vc).setCurrentXVelocity(0);
                                }
                            });
        }
    }

    private void movementAnimation(Entity entity) {
        AnimationComponent ac =
                (AnimationComponent)
                        entity.getComponent(AnimationComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("AnimationComponent"));
        Animation newCurrentAnimation;
        VelocityComponent vc =
                (VelocityComponent)
                        entity.getComponent(VelocityComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("VelocityComponent"));
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
