package ecs.components.ai.idle;

import ecs.components.ai.fight.MimicAI;
import ecs.entities.Entity;
import ecs.entities.Mimic;

/**
 * Idle behaviour of doing nothing mainly used by the {@link Mimic} class
 *
 * @see MimicAI
 */
public class MimicWalk implements IIdleAI {

    /**
     * {@inheritDoc}
     * <p/>
     * Idle behaviour of doing nothing
     */
    @Override
    public void idle(Entity entity) {

    }
}
