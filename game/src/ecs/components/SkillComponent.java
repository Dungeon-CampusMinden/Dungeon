package ecs.components;

import ecs.entities.Entity;
import graphic.Animation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SkillComponent extends Component {

    public static String name = "SkillComponent";

    private Method method;
    private boolean active;
    private Animation animation;

    /**
     * @param entity associated entity
     * @param method Method with the logic of this skill
     * @param animation Animation of this skill
     */
    public SkillComponent(Entity entity, Method method, Animation animation) {
        super(entity);
        this.method = method;
        this.animation = animation;
        active = false;
    }

    /**
     * Execute the method of this skill
     *
     * @param args List with arguments
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void execute(Object... args) throws InvocationTargetException, IllegalAccessException {
        method.invoke(getEntity(), args);
        active = true;
    }

    /** Set the active state of this skill to false */
    public void deActivate() {
        active = false;
    }

    /**
     * @return if this skill is currently activ or not
     */
    public boolean getActive() {
        return active;
    }

    public Animation getAnimation() {
        return animation;
    }
}
