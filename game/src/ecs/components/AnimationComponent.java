package ecs.components;

import ecs.entitys.Entity;
import graphic.Animation;
import java.util.List;
import mydungeon.ECS;

/**
 * AnimationComponent is a component that stores the possible animations and the current animation
 * of an entity
 */
public class AnimationComponent implements Component {

    private List<Animation> animationList;
    private Animation currentAnimation;

    /**
     * @param entity associated entity
     * @param animations list of all possible animations of the entity
     * @param currentAnimation current animation of the entity
     */
    public AnimationComponent(
            Entity entity, List<Animation> animations, Animation currentAnimation) {
        ECS.animationComponentMap.put(entity, this);
        this.animationList = animations;
        this.currentAnimation = currentAnimation;
    }

    /**
     * @param animation new animation of the entity
     */
    public void setCurrentAnimation(Animation animation) {
        this.currentAnimation = animation;
    }

    /**
     * @return current animation of the entity
     */
    public Animation getCurrentAnimation() {
        return currentAnimation;
    }

    /**
     * @param animationList list of animations that are to be used
     * @param currentAnimation set the current animation
     */
    public void setAnimationList(List<Animation> animationList, Animation currentAnimation) {
        this.animationList = animationList;
        setCurrentAnimation(currentAnimation);
    }

    /**
     * @return the animation List of the entity
     */
    public List<Animation> getAnimationList() {
        return animationList;
    }
}
