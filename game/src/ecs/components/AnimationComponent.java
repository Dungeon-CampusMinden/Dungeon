package ecs.components;

import ecs.entities.Entity;
import graphic.Animation;
import java.util.List;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

/**
 * AnimationComponent is a component that stores the possible animations and the current animation
 * of an entity
 */
@DSLType(name = "animation_component")
public class AnimationComponent extends Component {
    private static final List<String> missingTexture = List.of("animation/missingTexture.png");
    public static String name = "AnimationComponent";

    private @DSLTypeMember(name = "idle_left") final Animation idleLeft;
    private @DSLTypeMember(name = "idle_right") final Animation idleRight;
    private @DSLTypeMember(name = "current_animation") Animation currentAnimation;

    /**
     * @param entity associated entity
     * @param idleLeft Idle animation faced left
     * @param idleRight Idle-animation faced right
     */
    public AnimationComponent(Entity entity, Animation idleLeft, Animation idleRight) {
        super(entity, name);
        this.idleRight = idleRight;
        this.idleLeft = idleLeft;
        this.currentAnimation = idleLeft;
    }

    /**
     * @param entity associated entity
     * @param idle Idle-animation
     */
    public AnimationComponent(Entity entity, Animation idle) {
        this(entity, idle, idle);
    }

    /**
     * @param entity associated entity
     */
    public AnimationComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity, name);
        this.idleLeft = new Animation(missingTexture, 100);
        this.idleRight = new Animation(missingTexture, 100);
        this.currentAnimation = new Animation(missingTexture, 100);
    }

    /**
     * @param animation new current animation of the entity
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
     * @return Idle-animation faced left
     */
    public Animation getIdleLeft() {
        return idleLeft;
    }

    /**
     * @return Idle-animation faced right
     */
    public Animation getIdleRight() {
        return idleRight;
    }
}
