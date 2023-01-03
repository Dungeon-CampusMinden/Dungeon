package ecs.components;

import ecs.entitys.Entity;
import graphic.Animation;
import mydungeon.ECS;

/**
 * AnimationComponent is a component that stores the possible animations and the current animation
 * of an entity
 */
public class AnimationComponent implements Component {

    private AnimationList animationList;
    private Animation currentAnimation;

    /**
     * @param entity associated entity
     * @param animations all Animations of this Entity
     * @param currentAnimation current animation of the entity
     */
    public AnimationComponent(Entity entity, AnimationList animations, Animation currentAnimation) {
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
     * @param animationList list of animations
     * @param currentAnimation set the current animation
     */
    public void setAnimationList(AnimationList animationList, Animation currentAnimation) {
        this.animationList = animationList;
        setCurrentAnimation(currentAnimation);
    }

    /**
     * @return the animation List of the entity
     */
    public AnimationList getAnimationList() {
        return animationList;
    }
}
