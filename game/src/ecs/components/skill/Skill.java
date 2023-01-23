package ecs.components.skill;

import graphic.Animation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Skill {

    private Method method;
    private boolean active;
    private Animation animation;

    /**
     * @param method Method with the logic of this skill
     * @param animation Animation of this skill
     */
    public Skill(Method method, Animation animation) {
        this.method = method;
        this.animation = animation;
        active = true;
    }

    /**
     * Execute the method of this skill
     *
     * @param args List with arguments
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void execute(Object... args) throws InvocationTargetException, IllegalAccessException {
        if (active) {
            method.invoke(this, args);
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
