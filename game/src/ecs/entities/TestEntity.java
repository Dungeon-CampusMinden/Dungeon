package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.ai.transition.RangeTransition;
import graphic.Animation;

public class TestEntity extends Entity {
    public TestEntity() {
        super();
        new PositionComponent(this);

        AIComponent aiComponent = new AIComponent(this);
        aiComponent.setFightAI(new CollideAI(2f));
        aiComponent.setIdleAI(new PatrouilleWalk(5, 5, 1000, PatrouilleWalk.MODE.BACK_AND_FORTH));
        aiComponent.setTransitionAI(new RangeTransition(5f));

        setupAnimationComponent();
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation("monster/imp/idleRight");
        Animation idleLeft = AnimationBuilder.buildAnimation("monster/imp/idleLeft");
        Animation moveRight = AnimationBuilder.buildAnimation("monster/imp/runRight");
        Animation moveLeft = AnimationBuilder.buildAnimation("monster/imp/runLeft");

        new AnimationComponent(this, idleLeft, idleRight);
        new VelocityComponent(this, 0.2f, 0.2f, moveLeft, moveRight);
    }
}
