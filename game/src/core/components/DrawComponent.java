package core.components;

import core.Component;
import core.Entity;
import core.systems.DrawSystem;
import core.systems.VelocitySystem;
import core.utils.components.draw.Animation;
import core.utils.logging.CustomLogLevel;

import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

import java.util.List;
import java.util.logging.Logger;

/**
 * The AnimationComponent associates an entity with its animations. It stores the current animation
 * and two idle animations, one for each direction (left/right). The current animation can be
 * overwritten by using the {@link #setCurrentAnimation} of this component. The {@link DrawSystem
 * DrawSystem} uses the {@link #getCurrentAnimation()} method to draw the current animation to the
 * screen.
 */
@DSLType(name = "animation_component")
public class DrawComponent extends Component {
    private static List<String> missingTexture = List.of("animation/missingTexture.png");
    private @DSLTypeMember(name = "idle_left") Animation idleLeft;
    private @DSLTypeMember(name = "idle_right") Animation idleRight;
    private @DSLTypeMember(name = "current_animation") Animation currentAnimation;
    private final Logger animCompLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Create a new AnimationComponent object
     *
     * <p>Create a new AnimationComponent with separate idle Animations for each direction.
     *
     * @param entity Entity to add this component to
     * @param idleLeft Idle-Animation faced left
     * @param idleRight Idle-Animation faced right
     */
    public DrawComponent(Entity entity, Animation idleLeft, Animation idleRight) {
        super(entity);
        this.idleRight = idleRight;
        this.idleLeft = idleLeft;
        this.currentAnimation = idleLeft;
    }

    /**
     * Create a new AnimationComponent object.
     *
     * <p>Create a new AnimationComponent object with the given animation as idle animation for both
     * directions.
     *
     * @param entity Entity to add this component to
     * @param idle Idle-Animation
     */
    public DrawComponent(Entity entity, Animation idle) {
        this(entity, idle, idle);
    }

    /**
     * Create a new AnimationComponent object with default Animations.
     *
     * <p>The default Animations are composed of a single frame with the "missingTexture" texture.
     *
     * @param entity Entity to add this component to
     */
    public DrawComponent(@DSLContextMember(name = "entity") Entity entity) {
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
     * Set the current animation displayed on the entity.
     *
     * <p>The animation passed does not have to be one of the animations stored in this component,
     * it can be any animation. If the animation passed is not displayed on the entity, there may be
     * another point in the code where the animation is overwritten on the same tick. (e.g. in
     * {@link VelocitySystem VelocitySystem}).
     *
     * @param animation new current animation.
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
     * Get the current animation being displayed on entity.
     *
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
     * Get idling animation that is used if the entity is facing left.
     *
     * @return Idle-Animation faced left
     */
    public Animation getIdleLeft() {
        return idleLeft;
    }

    /**
     * Get idle animation that is used if the entity is facing right.
     *
     * @return Animation faced right
     */
    public Animation getIdleRight() {
        return idleRight;
    }
}
