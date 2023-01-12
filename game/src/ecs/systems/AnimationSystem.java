package ecs.systems;

import ecs.components.AnimationComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import graphic.Animation;
import mydungeon.ECS;

public class AnimationSystem extends ECS_System {
    /** Updates the currentAnimations of all entities */
    public void update() {
        for (Entity entity : ECS.entities) {
            AnimationComponent ac =
                    (AnimationComponent) entity.getComponent(AnimationComponent.name);
            if (ac != null) {
                Animation backup = ac.getCurrentAnimation();
                if (!dieState(entity, ac))
                    if (!hitState(entity, ac))
                        if (!skillState(entity, ac)) movementState(entity, ac);
                if (ac.getCurrentAnimation() == null) ac.setCurrentAnimation(backup);
            }
        }
    }

    /**
     * @param entity
     * @param ac
     * @return true if animation was changed
     */
    private boolean dieState(Entity entity, AnimationComponent ac) {
        return false;
    }
    /**
     * @param entity
     * @param ac
     * @return true if animation was changed
     */
    private boolean hitState(Entity entity, AnimationComponent ac) {
        return false;
    }
    /**
     * @param entity
     * @param ac
     * @return true if animation was changed
     */
    private boolean skillState(Entity entity, AnimationComponent ac) {
        return false;
    }
    /**
     * @param entity
     * @param ac
     * @return true if animation was changed
     */
    private boolean movementState(Entity entity, AnimationComponent ac) {
        if (entity.getComponent(VelocityComponent.name) != null) {
            Animation newCurrentAnimation;
            VelocityComponent vc = (VelocityComponent) entity.getComponent(VelocityComponent.name);
            float x = vc.getX();
            if (x > 0) newCurrentAnimation = ac.getAnimationList().getMoveRight();
            else if (x < 0) newCurrentAnimation = ac.getAnimationList().getMoveLeft();
            // idle
            else {
                if (ac.getCurrentAnimation() == ac.getAnimationList().getIdleLeft()
                        || ac.getCurrentAnimation() == ac.getAnimationList().getMoveLeft())
                    newCurrentAnimation = ac.getAnimationList().getIdleLeft();
                else newCurrentAnimation = ac.getAnimationList().getMoveRight();
            }
            ac.setCurrentAnimation(newCurrentAnimation);
            return true;
        }
        return false;
    }
}
