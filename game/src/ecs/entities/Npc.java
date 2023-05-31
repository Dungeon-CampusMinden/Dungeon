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
 * This Npc is used to spawn a Ghost.
 *
 * The Npc is an Entity that is used to create a Tombstone.
 */
public class Npc extends Entity{
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
    public Npc(){
        super();
        setupPositionComponent();
        setupVelocityComponent();
        setupAnimationComponent();
        setupAIComponent();
        npcLogger.info("Npc was created");
    }

    private void setupPositionComponent(){
        new PositionComponent(this);
    }
    private void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
    }

    private void setupAIComponent() {
        new AIComponent(this, new CollideAI(0), new GhostWalk(), new RangeTransition(0));
    }
}

