package ecs.entities;

/**
 * This Npc is used to spawn a Ghost.
 * <p>
 * The Npc is an Entity that is used to create a Tombstone.
 */
public abstract class Npc extends Entity {
    /**
     * This Methode is used to set up the PositionComponent
     */
    protected abstract void setupPositionComponent();

    /**
     * This Methode is used to set up the VelocityComponent
     */

    protected abstract void setupVelocityComponent();

    /**
     * This Methode is used to set up the AnimationComponent
     */

    protected abstract void setupAnimationComponent();

    /**
     * This Methode is used to set up the AIComponent
     */
    protected abstract void setupAIComponent();
}
