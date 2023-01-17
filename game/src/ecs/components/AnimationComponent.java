package ecs.components;

import ecs.entities.Entity;
import graphic.Animation;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

/**
 * AnimationComponent is a component that stores the possible animations and the current animation
 * of an entity
 */
@DSLType
public class AnimationComponent extends Component {

    public static String name = "AnimationComponent";
    private @DSLTypeMember Animation idleLeft;
    private @DSLTypeMember Animation idleRight;
    private @DSLTypeMember Animation currentAnimation;

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
    public AnimationComponent(Entity entity) {
        super(entity);
        this.idleLeft = null;
        this.idleRight = null;
        this.currentAnimation = null;
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
