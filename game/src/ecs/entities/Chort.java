package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.skill.*;
import ecs.components.xp.ILevelUp;
import ecs.components.xp.XPComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import graphic.Animation;
import ecs.components.OnDeathFunctions.EndGame;
import ecs.components.ai.AIComponent;
import ecs.components.ai.AITools;
import ecs.components.ai.fight.IFightAI;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.idle.IIdleAI;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.ai.transition.ITransition;
import ecs.components.ai.transition.RangeTransition;
import starter.Game;
import tools.Point;

import java.lang.Math;

/**
 * The Chort is a hostile mob(npc). It's entity in the ECS. This class helps to
 * setup chorts with all its components and attributes .
 */
public class Chort extends Monster {

    private final float xSpeed = 0.2f;
    private final float ySpeed = 0.2f;
    private final int maxHealth = 20;
    private final float attackRange = 0.5f;
    private int level;
    private int attackCooldown = 1;

    private final String pathToIdleLeft = "monster/chort/idleLeft";
    private final String pathToIdleRight = "monster/chort/idleRight";
    private final String pathToRunLeft = "monster/chort/runLeft";
    private final String pathToRunRight = "monster/chort/runRight";
    private final String pathToGetHit = "monster/chort/getHit";
    private final String pathToDie = "monster/chort/die";

    private XPComponent xPComponent;

    /** Entity with Components */
    public Chort(int level) {
        super(level);
        this.level = level;
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupHealthComponent();
        setupAIComponent();
        setupXPComponent();
        setupDamageComponent();
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
                (you, other, direction) -> attack(other),
                (you, other, direction) -> System.out.println("monsterCollisionLeave"));
    }

    private void setupHealthComponent() {
        Animation getHit = AnimationBuilder.buildAnimation(pathToGetHit);
        Animation die = AnimationBuilder.buildAnimation(pathToDie);
        IOnDeathFunction iOnDeathFunction = new IOnDeathFunction() {
            public void onDeath(Entity entity) {
                // Drop loot or something
            }
        };
        new HealthComponent(this, maxHealth, iOnDeathFunction, getHit, die);
    }

    private IFightAI setupFightStrategy() {
        return new CollideAI(3);
    }

    private void setupAIComponent() {
        new AIComponent(this, setupFightStrategy(), setupIdleStrategy(), setupTransition());
    }

    private void attack(Entity entity) {
        Damage damage = new Damage(calcDamage(), DamageType.PHYSICAL, this);
        if (entity.getComponent(HealthComponent.class).isPresent()) {
            ((HealthComponent) entity.getComponent(HealthComponent.class).get()).receiveHit(damage);
        }
    }

    private int calcDamage() {
        return 2 + (int) Math.sqrt(4 * level);
    }

    private void setupDamageComponent() {
        new DamageComponent(this, calcDamage());
    }

    private void setupXPComponent() {
        xPComponent = new XPComponent(this, new ILevelUp() {

            @Override
            public void onLevelUp(long nexLevel) {
                HealthComponent health = (HealthComponent) getComponent(HealthComponent.class).get();
                health.setMaximalHealthpoints((int) (health.getMaximalHealthpoints() * 1.01f));
                health.setCurrentHealthpoints(health.getMaximalHealthpoints());
                xPComponent.setLootXP(30 * (nexLevel >> 1));
                ((DamageComponent) getComponent(DamageComponent.class).get()).setDamage(calcDamage());
            }

        }, 30 * (level >> 1));
        xPComponent.setCurrentLevel(level);
        ((HealthComponent) getComponent(HealthComponent.class).get())
                .setMaximalHealthpoints(maxHealth * (int) Math.pow(1.01f, xPComponent.getCurrentLevel()));
        ((HealthComponent) getComponent(HealthComponent.class).get())
                .setCurrentHealthpoints(maxHealth * (int) Math.pow(1.01f, xPComponent.getCurrentLevel()));
    }

}
