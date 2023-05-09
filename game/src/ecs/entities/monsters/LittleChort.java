package ecs.entities.monsters;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.ai.transition.RangeTransition;
import graphic.Animation;

public class LittleChort extends BasicMonster {
    public LittleChort() {
        super(0.3f, 0.3f, 5, "monster/imp/idleLeft", "monster/imp/idleRight", "monster/imp/runLeft", "monster/imp/runRight");
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


    public void setupHitboxComponent() {
        new HitboxComponent(this, HitboxComponent.DEFAULT_COLLIDER, HitboxComponent.DEFAULT_COLLIDER);
    }

    public void setupHealthComponent(int maxHealthPoints) {
        // Maximale Gesundheitspunkte für das Monster


        // Funktion, die aufgerufen wird, wenn das Monster stirbt
        IOnDeathFunction onDeathFunction = entity -> {
            // Logik für das, was passieren soll, wenn das Monster stirbt
            System.out.println("Das Monster ist gestorben!");
        };

        // Animationen für das Monster, wenn es Schaden erleidet oder stirbt
        String pathToHitAnimation = "monster/imp/hitAnimation";
        String pathToDieAnimation = "monster/imp/dieAnimation";
        Animation hitAnimation = AnimationBuilder.buildAnimation(pathToHitAnimation);
        Animation dieAnimation = AnimationBuilder.buildAnimation(pathToDieAnimation);

        // Erstelle das HealthComponent für das Monster
        new HealthComponent(this, maxHealthPoints, onDeathFunction, hitAnimation, dieAnimation);
    }

    @Override
    public void setupAIComponent() {
        float radius = 5.0f;
        int numberCheckpoints = 2;
        int pauseTime = 2000;
        PatrouilleWalk.MODE mode = PatrouilleWalk.MODE.RANDOM;
        PatrouilleWalk patrouilleWalk = new PatrouilleWalk(radius, numberCheckpoints, pauseTime, mode);
        float rushRange = 0.3f;
        CollideAI collideAI = new CollideAI(rushRange);
        float transitionRange = 2.0f;
        RangeTransition rangeTransition = new RangeTransition(transitionRange);
        new AIComponent(this, collideAI, patrouilleWalk, rangeTransition);
    }
}
