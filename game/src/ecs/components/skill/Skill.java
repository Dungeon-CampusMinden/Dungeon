package ecs.components.skill;

import ecs.entities.Entity;
import graphic.Animation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Skill {

    private Entity entity;
    private boolean active;
    private Animation animation;
    private ISkillFunction skillFunction;

    /**
     * @param entity associated entity
     * @param animation Animation of this skill
     * @param skillFunction Function of this skill
     */
    public Skill(Entity entity, Animation animation, ISkillFunction skillFunction) {
        this.entity =entity;
        this.animation = animation;
        this.skillFunction = skillFunction;
        active = true;
    }

    /**
     * Execute the method of this skill
     *
     * @param entity associated entity
     *
     */
    public void execute(Entity entity){
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
