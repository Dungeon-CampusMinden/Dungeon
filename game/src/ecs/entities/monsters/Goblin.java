package ecs.entities.monsters;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.WanderingWalk;
import ecs.components.ai.transition.RangeTransition;
import graphic.Animation;


public class Goblin extends BasicMonster {


    public Goblin() {
        super(0.2f, 0.2f, 7, "monster/goblin/idleLeft", "monster/goblin/idleRight", "monster/goblin/runLeft", "monster/goblin/runRight");
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupAIComponent();
        setupHitboxComponent();
        setupHealthComponent((int) hp);
    }

    @Override
    public void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    @Override
    public void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
    }

    @Override
    public void setupHitboxComponent() {
        new HitboxComponent(this, HitboxComponent.DEFAULT_COLLIDER, HitboxComponent.DEFAULT_COLLIDER);
    }

    @Override
    public void setupHealthComponent(int maxHealthPoints) {
        // Funktion, die aufgerufen wird, wenn das Monster stirbt
        IOnDeathFunction onDeathFunction = entity -> {
            // Logik für das, was passieren soll, wenn das Monster stirbt
            System.out.println("Das Monster ist gestorben!");
        };

        // Animationen für das Monster, wenn es Schaden erleidet oder stirbt
        String pathToHitAnimation = "monster/goblin/hitAnimation";
        String pathToDieAnimation = "monster/goblin/idleLeft";
        Animation hitAnimation = AnimationBuilder.buildAnimation(pathToHitAnimation);
        Animation dieAnimation = AnimationBuilder.buildAnimation(pathToDieAnimation);

        // Erstelle das HealthComponent für das Monster
        new HealthComponent(this, maxHealthPoints, onDeathFunction, hitAnimation, dieAnimation);
    }

    @Override
    public void setupAIComponent() {
        WanderingWalk wanderingWalk = new WanderingWalk(5.0f, 2, 2000);
        float rushRange = 0.3f;
        CollideAI collideAI = new CollideAI(rushRange);
        float transitionRange = 2.0f;
        RangeTransition rangeTransition = new RangeTransition(transitionRange);
        new AIComponent(this, collideAI, wanderingWalk, rangeTransition);

    }

}
