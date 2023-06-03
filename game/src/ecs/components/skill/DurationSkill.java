package ecs.components.skill;

import ecs.entities.Entity;

public class DurationSkill extends Skill {

    private IDurationSkillFunction function;

    /**
     * Creates a new DurationSkill
     * 
     * @param skillFunction     The function of the DurationSkill
     * @param coolDownInSeconds the number of seconds to wait before the skill can
     *                          be used again
     * 
     * @see IDurationSkillFunction
     */
    public DurationSkill(IDurationSkillFunction skillFunction, float coolDownInSeconds) {
        super(skillFunction, coolDownInSeconds);
        function = skillFunction;
    }

    /**
     * Exact copy of the original method
     * <p/>
     * {@inheritDoc}
     * 
     * @see Skill#execute(Entity)
     */
    @Override
    public void execute(Entity entity) {
        if (!isOnCoolDown()) {
            function.execute(entity);
            activateCoolDown();
        }
    }

    /**
     * Returns {@code true} if {@code currentDurationInFrames} is greater than
     * {@code 0}
     * 
     * @return {@code true} if {@code currentDurationInFrames} is greater than
     *         {@code 0}
     */
    public boolean isActive() {
        return function.isActive();
    }

    /**
     * Modified method to also reduce the duration of the skill
     * <p/>
     * {@inheritDoc}
     * 
     * @see Skill#reduceCoolDown()
     */
    @Override
    public void reduceCoolDown() {
        if (isActive())
            function.reduceDuration();
        else
            super.reduceCoolDown();
    }

}
