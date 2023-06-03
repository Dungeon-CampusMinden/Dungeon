package ecs.components.skill;

public interface IDurationSkillFunction extends ISkillFunction {

    /**
     * This one for sure is to be used to reduce the duration
     */
    public void reduceDuration();

    /**
     * Returns {@code true} if the skill is active
     * 
     * @return {@code true} if the skill is active
     */
    public boolean isActive();

    /**
     * Activate the skill
     * 
     */
    public void activateDuration();

}
