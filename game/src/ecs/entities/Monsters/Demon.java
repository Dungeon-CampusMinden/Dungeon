package ecs.entities.Monsters;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.transition.FriendlyTransition;
import ecs.components.ai.transition.SelfDefendTransition;
import ecs.entities.Monster;
import ecs.graphic.Animation;


import java.util.Random;

/**
 * The Demon is an enemy monster which inherits from the Monster class.
 *
 */

public class Demon extends Monster {

    private boolean fightInRange = false;

    private final String pathToIdleLeft = "character/monster/chort/idleLeft";
    private final String pathToIdleRight = "character/monster/chort/idleRight";
    private final String pathToRunLeft = "character/monster/chort/runLeft";
    private final String pathToRunRight = "character/monster/chort/idleRight";

    private float xSpeed = 0.1f;
    private float ySpeed = 0.1f;
    private int dmg = 3;
    private int maxHealthpoint = 4;


    /** Entity with Components
     *
     * @param lvlFactor - the factor by which damage and health is increased
     * */
    public Demon(int lvlFactor){
        super();
        new PositionComponent(this);
        new AIComponent(this,new CollideAI(5f),new RadiusWalk(5,2),new FriendlyTransition());
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        if(lvlFactor == 0) lvlFactor++;
        this.dmg = this.dmg * lvlFactor;
        this.maxHealthpoint = this.maxHealthpoint * lvlFactor;
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

    private void setupHitboxComponent() {
        new HitboxComponent(
            this,
            (you, other, direction) -> System.out.print(""),
            (you, other, direction) -> System.out.print(""));
    }
}
