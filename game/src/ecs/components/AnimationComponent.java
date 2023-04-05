package ecs.components;

import ecs.entities.Entity;
import graphic.Animation;
import java.util.List;
import java.util.logging.Logger;
import logging.CustomLogLevel;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

/**
 * The AnimationComponent class represents a component that stores the possible idle animations and
 * the current animation of an entity. The AnimationComponent can have two idle animations when the
 * entity is not moving, an idle animation for when the entity is facing left and another idle
 * animation for when the entity is facing right. The current animation is the animation that is
 * currently being displayed.
 */
@DSLType(name = "animation_component")
public class AnimationComponent extends Component {
    private static List<String> missingTexture = List.of("animation/missingTexture.png");
    private @DSLTypeMember(name = "idle_left") Animation idleLeft;
    private @DSLTypeMember(name = "idle_right") Animation idleRight;
    private @DSLTypeMember(name = "current_animation") Animation currentAnimation;
    private final Logger animCompLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates an AnimationComponent object with the associated entity, idle animation faced left,
     * and idle animation faced right.
     *
     * @param entity the associated entity
     * @param idleLeft the idle animation faced left
     * @param idleRight the idle animation faced right
     */
    public AnimationComponent(Entity entity, Animation idleLeft, Animation idleRight) {
        super(entity);
        this.idleRight = idleRight;
        this.idleLeft = idleLeft;
        this.currentAnimation = idleLeft;
    }

    /**
     * Creates an AnimationComponent object with the associated entity and idle animation.
     *
     * @param entity the associated entity
     * @param idle the idle animation
     */
    public AnimationComponent(Entity entity, Animation idle) {
        this(entity, idle, idle);
    }

    /**
     * Creates an AnimationComponent object with the associated entity and default idle animations.
     *
     * @param entity the associated entity
     */
    public AnimationComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.idleLeft = new Animation(missingTexture, 100);
        this.idleRight = new Animation(missingTexture, 100);
        this.currentAnimation = new Animation(missingTexture, 100);
        animCompLogger.log(
                CustomLogLevel.ERROR,
                "The AnimationComponent for entity '"
                        + entity.getClass().getName()
                        + "' was created with default textures!");
    }

    /**
     * Sets the current animation of the entity to the given animation.
     *
     * @param animation the new current animation of the entity
     */
    public void setCurrentAnimation(Animation animation) {
        if (animation.getAnimationFrames().size() > 0) {
            if (animation.getAnimationFrames().get(0).equals(missingTexture.get(0))) {
                animCompLogger.log(
                        CustomLogLevel.ERROR,
                        "The Animation for entity '"
                                + entity.getClass().getName()
                                + "' was set to the default missing textures.");
            }
        }
        this.currentAnimation = animation;
    }

    /**
     * Returns the current animation of the entity.
     *
     * @return the current animation of the entity
     */
    public Animation getCurrentAnimation() {
        if (currentAnimation.getAnimationFrames().size() > 0) {
            animCompLogger.log(
                    CustomLogLevel.DEBUG,
                    this.getClass().getSimpleName()
                            + " fetching animation for entity '"
                            + entity.getClass().getSimpleName()
                            + "'. First path: "
                            + currentAnimation.getAnimationFrames().get(0));
        } else {
            animCompLogger.log(
                    CustomLogLevel.DEBUG,
                    this.getClass().getSimpleName()
                            + " fetching animation for entity '"
                            + entity.getClass().getSimpleName()
                            + "'. This entity has currently no animation.");
        }

        return currentAnimation;
    }

    /**
     * Returns the idle animation faced left of the entity.
     *
     * @return the idle animation faced left of the entity
     */
    public Animation getIdleLeft() {
        return idleLeft;
    }

    /**
     * Returns the idle animation faced right of the entity.
     *
     * @return the idle animation faced right of the entity
     */
    public Animation getIdleRight() {
        return idleRight;
    }
}
