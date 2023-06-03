package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.skill.*;
import ecs.damage.Damage;
import ecs.entities.Entity;
import ecs.components.skill.PiercingArrowSkill;
import ecs.components.xp.ILevelUp;
import ecs.components.xp.XPComponent;
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
 * The Imp is a hostile mob(npc). It's entity in the ECS. This class helps to
 * setup imps with all its components and attributes .
 */
public class Imp extends Monster {

    private final float xSpeed = 0.5f;
    private final float ySpeed = 0.5f;
    private final int maxHealth = 20;
    private final float attackRange = 0.5f;
    private int level;
    private int attackCooldown = 1;

    private final String pathToIdleLeft = "monster/imp/idleLeft";
    private final String pathToIdleRight = "monster/imp/idleRight";
    private final String pathToRunLeft = "monster/imp/runLeft";
    private final String pathToRunRight = "monster/imp/runRight";
    private final String pathToGetHit = "monster/imp/getHit";
    private final String pathToDie = "monster/imp/die";

    private Skill attack;

    /** Entity with Components */
    public Imp(int level) {
        super(level);
        this.level = level;
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupHealthComponent();
        setupAIComponent();
        setupXPComponent();
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
                new PiercingArrowSkill(() -> SkillTools.calculateLastPositionInRange(start, end, attackRange), this),
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

    private int calcDamage() {
        return 3 + (int) Math.sqrt(3 * level);
    }

    private void setupXPComponent() {
        new XPComponent(this, new ILevelUp() {

            @Override
            public void onLevelUp(long nexLevel) {
                HealthComponent health = (HealthComponent) getComponent(HealthComponent.class).get();
                health.setMaximalHealthpoints((int) (health.getMaximalHealthpoints() * 1.01f));
                health.setCurrentHealthpoints(health.getMaximalHealthpoints());
            }

        }, 10 * level);
    }

}
