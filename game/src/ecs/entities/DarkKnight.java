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
import ecs.components.ai.fight.MeleeAI;
import ecs.components.ai.idle.IIdleAI;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.ai.transition.ITransition;
import ecs.components.ai.transition.RangeTransition;
import starter.Game;
import tools.Point;

import java.lang.Math;

/**
 * The DarkKnight is a hostile mob(npc). It's entity in the ECS. This class helps to
 * setup darkknights with all its components and attributes.
 * 
 * I'm Batman
 */

public class DarkKnight extends Monster {

    private final float xSpeed = 0.1f;
    private final float ySpeed = 0.1f;
    private final int maxHealth = 100;
    private final float attackRange = 1f;
    private int level;
    private int attackCooldown = 2;

    private final String pathToIdleLeft = "monster/darkKnight/idleLeft";
    private final String pathToIdleRight = "monster/darkKnight/idleRight";
    private final String pathToRunLeft = "monster/darkKnight/runLeft";
    private final String pathToRunRight = "monster/darkKnight/runRight";
    private final String pathToGetHit = "monster/darkKnight/getHit";
    private final String pathToDie = "monster/darkKnight/die";

    private Skill attack;

    /** Entity with Components */
    public DarkKnight(int level) {
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

    private void setupSkillComponent() {
        Point start = ((PositionComponent) this.getComponent(PositionComponent.class).get()).getPosition();
        Point end = ((PositionComponent) Game.getHero().get().getComponent(PositionComponent.class).get())
                .getPosition();
        attack = new Skill(
                new StabSkill(() -> SkillTools.calculateLastPositionInRange(start, end, attackRange)),
                attackCooldown);
        new SkillComponent(this).addSkill(attack);
    }

    private IFightAI setupFightStrategy() {
        return new MeleeAI(attackRange, attack);
    }

    private void setupAIComponent() {
        setupSkillComponent();
        new AIComponent(this, setupFightStrategy(), setupIdleStrategy(), setupTransition());
    }

    private void attack(Entity entity) {
        Damage damage = new Damage(calcDamage(), DamageType.PHYSICAL, this);
        if (entity.getComponent(HealthComponent.class).isPresent()) {
            ((HealthComponent) entity.getComponent(HealthComponent.class).get()).receiveHit(damage);
        }
    }

    private int calcDamage() {
        return 5 + (int) Math.sqrt(10 * level);
    }

}
