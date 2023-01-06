package ecs.systems;

import ecs.components.AnimationComponent;
import ecs.components.VelocityComponent;
import ecs.entitys.Entity;
import graphic.Animation;
import java.util.Map;
import mydungeon.ECS;

public class AnimationSystem extends ECS_System {
    /** Updates the currentAnimations of all entities */
    public void update() {
        for (Map.Entry<Entity, AnimationComponent> entry : ECS.animationComponentMap.entrySet()) {
            Entity entity = entry.getKey();
            AnimationComponent ac = ECS.animationComponentMap.get(entity);

            if (!dieState(entity, ac))
                if (!hitState(entity, ac)) if (!skillState(entity, ac)) movementState(entity, ac);
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
        if (ECS.velocityComponentMap.get(entity) != null) {
            Animation newCurrentAnimation;
            VelocityComponent vc = ECS.velocityComponentMap.get(entity);
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
