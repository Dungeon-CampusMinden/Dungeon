package ecs.systems;

import ecs.components.AnimationComponent;
import ecs.components.VelocityComponent;
import ecs.entitys.Entity;
import java.util.Map;
import mydungeon.ECS;

public class AnimationSystem extends ECS_System {
    @Override
    public void update() {
        for (Map.Entry<Entity, AnimationComponent> entry : ECS.animationComponentMap.entrySet()) {
            Entity entity = entry.getKey();
            AnimationComponent ac = ECS.animationComponentMap.get(entity);
            if (ECS.velocityComponentMap.get(entity) != null) {
                VelocityComponent vc = ECS.velocityComponentMap.get(entity);
                float x = vc.getX();
                if (x > 0) ac.setCurrentAnimation(ac.getAnimationList().getMoveRight());
                else if (x < 0) ac.setCurrentAnimation(ac.getAnimationList().getMoveLeft());
                else ac.setCurrentAnimation(ac.getAnimationList().getIdleLeft());
            }
        }
    }
}
