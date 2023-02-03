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
    private static List<String> missingTexture = List.of("animation/missingTexture.png");
    private @DSLTypeMember(name = "idle_left") Animation idleLeft;
    private @DSLTypeMember(name = "idle_right") Animation idleRight;
    private @DSLTypeMember(name = "current_animation") Animation currentAnimation;

    /**
     * @param entity associated entity
     * @param idleLeft Idleanimation faced left
     * @param idleRight Idleanimation faced right
     */
    public AnimationComponent(Entity entity, Animation idleLeft, Animation idleRight) {
        super(entity, AnimationComponent.class);
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
        super(entity, AnimationComponent.class);
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
