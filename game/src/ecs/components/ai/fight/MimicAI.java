package ecs.components.ai.fight;

import ecs.components.skill.Skill;
import ecs.entities.Entity;
import ecs.entities.Mimic;

/**
 * Stationary attack mainly used by the {@link Mimic} class
 * 
 * @see MimicWalk
 */
public class MimicAI implements IFightAI {

    private final Skill skill;

    /**
     * Creates a new instance of MimicAI
     * 
     * @param skill the skill used to fight by the wielder
     */
    public MimicAI(Skill skill) {
        this.skill = skill;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Stationary fight without movement
     */
    @Override
    public void fight(Entity entity) {
        skill.execute(entity);
    }

}
