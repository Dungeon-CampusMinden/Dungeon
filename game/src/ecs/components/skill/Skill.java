package ecs.components.skill;

import ecs.entities.Entity;
import graphic.Animation;

public class Skill {

    private boolean active;
    private Animation animation;
    private ISkillFunction skillFunction;

    /**
     * @param animation Animation of this skill
     * @param skillFunction Function of this skill
     */
    public Skill(Animation animation, ISkillFunction skillFunction) {
        this.animation = animation;
        this.skillFunction = skillFunction;
        active = true;
    }

    /**
     * Execute the method of this skill
     *
     * @param entity entity which uses the skill
     */
    public void execute(Entity entity) {
        if (active) {
            skillFunction.execute(entity);
        }
    }

    /** Toggle the active state of this skill */
    public void toggleActive() {
        active = !active;
    }

    /**
     * @return if this skill is currently active or not
     */
    public boolean getActive() {
        return active;
    }

    public Animation getAnimation() {
        return animation;
    }
}
