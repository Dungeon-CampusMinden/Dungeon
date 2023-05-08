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
 * AnimationComponent is a component that stores the possible animations and the current animation
 * of an entity
 */
@DSLType(name = "animation_component")
public class AnimationComponent extends Component {
    private static List<String> missingTexture = List.of("animation/missingTexture.png");
    private @DSLTypeMember(name = "idle_left") Animation idleLeft;
    private @DSLTypeMember(name = "idle_right") Animation idleRight;
    private @DSLTypeMember(name = "current_animation") Animation currentAnimation;
    private transient final Logger animCompLogger = Logger.getLogger(this.getClass().getName());

    /**
     * @param entity associated entity
     * @param idleLeft Idleanimation faced left
     * @param idleRight Idleanimation faced right
     */
    public AnimationComponent(Entity entity, Animation idleLeft, Animation idleRight) {
        super(entity);
        this.idleRight = idleRight;
        this.idleLeft = idleLeft;
        this.currentAnimation = idleLeft;
    }

    /**
     * @param entity associated entity
     * @param idle Idleanimation
     */
    public AnimationComponent(Entity entity, Animation idle) {
        this(entity, idle, idle);
    }

    /**
     * @param entity associated entity
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
     * @param animation new current animation of the entity
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
     * @return current animation of the entity
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
     * @return Idleanimation faced left
     */
    public Animation getIdleLeft() {
        return idleLeft;
    }

    /**
     * @return Idleanimation faced right
     */
    public Animation getIdleRight() {
        return idleRight;
    }
}
