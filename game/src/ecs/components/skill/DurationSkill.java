package ecs.components.skill;

import ecs.entities.Entity;
import tools.Constants;

public class DurationSkill extends Skill {

    private float durationInFrames, currentDurationInFrames = 0.0f;

    /**
     * Creates a new DurationSkill
     * 
     * @implNote Obviously {@code IDurationSkillFunction} should be used instead of
     *           {@code ISkillFunction}
     * 
     * @param skillFunction     The function of the DurationSkill
     * @param coolDownInSeconds the number of seconds to wait before the skill can
     *                          be used again
     * @param durationInSeconds the number of seconds the skill is active
     * 
     * @see IDurationSkillFunction
     */
    public DurationSkill(ISkillFunction skillFunction, float coolDownInSeconds, float durationInSeconds) {
        super(skillFunction, coolDownInSeconds);
        this.durationInFrames = durationInSeconds * Constants.FRAME_RATE;
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
            getSkillFunction().execute(entity);
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
        return currentDurationInFrames > 0;
    }

    /**
     * Modified method to also activate the duration of the skill
     * <p/>
     * {@inheritDoc}
     * 
     * @see Skill#activateCoolDown()
     */
    @Override
    public void activateCoolDown() {
        super.activateCoolDown();
        currentDurationInFrames = durationInFrames;
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
            currentDurationInFrames--;
        else
            super.reduceCoolDown();
    }

}
