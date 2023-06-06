package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.GhostWalk;
import ecs.components.ai.transition.RangeTransition;
import graphic.Animation;

import java.util.logging.Logger;

/**
 * This Ghost is used to spawn a Ghost.
 * <p>
 * The Ghost is an Entity that is used to create a Tombstone.
 */
public class Ghost extends Npc{
    private transient final Logger npcLogger = Logger.getLogger(this.getClass().getName());
    private final float xSpeed = 0.4f;
    private final float ySpeed = 0.2f;

    private final String pathToIdleLeft = "monster/ghost/idleLeft";
    private final String pathToIdleRight = "monster/ghost/idleRight";
    private final String pathToRunLeft = "monster/ghost/runLeft";
    private final String pathToRunRight = "monster/ghost/runRight";

    /**
     * Npc constructor is used to set up the Entity.
     */
    public Ghost(){
        super();
        setupPositionComponent();
        setupVelocityComponent();
        setupAnimationComponent();
        setupAIComponent();
        npcLogger.info("Npc was created");
    }

    /**
     * This Methode is used to set up the PositionComponent
     */
    protected void setupPositionComponent(){
        new PositionComponent(this);
    }

    /**
     * This Methode is used to set up the VelocityComponent
     */
    protected void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    /**
     * This Methode is used to set up the AnimationComponent
     */
    protected void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
    }

    /**
     * This Methode is used to set up the AIComponent
     */
    protected void setupAIComponent() {
        new AIComponent(this, new CollideAI(0), new GhostWalk(), new RangeTransition(0));
    }
}

