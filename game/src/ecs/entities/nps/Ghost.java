package ecs.entities.nps;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.GhostIdle;
import ecs.components.ai.idle.IIdleAI;
import ecs.components.ai.idle.WanderingWalk;
import ecs.components.ai.transition.RangeTransition;
import ecs.entities.Entity;
import graphic.Animation;

public class Ghost extends Entity {

    private final float xSpeed = 0.3f;
    private final float ySpeed = 0.3f;

    private final String pathToIdleLeft = "Ghost/idleLeft";
    private final String pathToIdleRight = "Ghost/idleRight";
    private final String pathToRunLeft = "Ghost/idleLeft";
    private final String pathToRunRight = "Ghost/idleLeft";

    public Ghost() {
        super();
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupAIComponent();
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
        float followRadius = 1.3f;
        float maxDistance = 3.0f;
        WanderingWalk wanderingWalk = new WanderingWalk(5.0f, 3, 2000);
        CollideAI collideAI = new CollideAI(followRadius);
        RangeTransition rangeTransition = new RangeTransition(maxDistance);
        new AIComponent(this, collideAI, wanderingWalk, rangeTransition);
    }



}
