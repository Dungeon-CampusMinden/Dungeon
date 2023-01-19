package ecs.components.ai.transition;

import ecs.components.ai.AITools;
import ecs.entities.Entity;

public class RangeTransition implements ITransition {

    private float range;

    public RangeTransition(float range) {
        this.range = range;
    }

    @Override
    public boolean goFightMode(Entity entity) {
        return AITools.playerInRange(entity, range);
    }
}
