package ecs.entities.Monsters;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.transition.FriendlyTransition;
import ecs.components.ai.transition.SelfDefendTransition;
import ecs.entities.Entity;
import ecs.entities.Monster;
import graphic.Animation;

/**
 * The Slime is an enemy monster which inherits from the Monster class.
 *
 */

public class Slime extends Monster {

    public String pathToIdleLeft = null;
    public String pathToIdleRight = null;
    public String pathToRunLeft = null;
    public String pathToRunRight = null;

    private float xSpeed = 0.05f;
    private float ySpeed = 0.05f;
    public int dmg = 1;
    private final int maxHealthpoint = 8;



    /** Entity with Components */
    public Slime(){
        super();
        new PositionComponent(this);
        new AIComponent(this,new CollideAI(3f),new RadiusWalk(2,8),new FriendlyTransition());
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
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
            (you, other, direction) -> System.out.println("monsterCollisionEnter"),
            (you, other, direction) -> System.out.println("monsterCollisionLeave"));
    }
}
