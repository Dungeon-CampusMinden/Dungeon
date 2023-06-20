package ecs.components.ai.idle;

import ecs.entities.Entity;

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
