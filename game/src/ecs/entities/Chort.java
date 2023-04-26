package game.src.ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.skill.*;
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

    public Chort(int level) {
        super(level);
        this.level = level;
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupHealthComponent();
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

}
