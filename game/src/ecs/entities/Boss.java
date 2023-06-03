package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.AITools;
import ecs.components.ai.fight.BossAI;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.fight.IFightAI;
import ecs.components.ai.idle.BossWalk;
import ecs.components.ai.transition.RangeTransition;
import ecs.components.skill.FireballSkill;
import ecs.components.skill.ITargetSelection;
import ecs.components.skill.Skill;
import ecs.components.skill.SkillComponent;
import ecs.components.skill.StabSkill;
import ecs.components.xp.ILevelUp;
import ecs.components.xp.XPComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import graphic.Animation;
import level.LevelAPI;
import starter.Game;
import tools.Point;

import java.util.logging.Logger;

/**
 * The Boss is the final boss. It is entity is in the ECS. This class helps to
 * setup the
 * boss with all its components and attributes .
 * It extends the Monster class. The Boss is a Monster.
 */

public class Boss extends Monster {
    private transient final Logger bossLogger = Logger.getLogger(this.getClass().getName());
    private final float xSpeed = 0.2f;
    private final float ySpeed = 0.2f;
    private final int maxHealth = 200;
    private final float attackRange = 4f;
    private int level;
    private int attackCooldown = 2;

    private final String pathToIdleLeft = "monster/Boss/idleLeft";
    private final String pathToIdleRight = "monster/Boss/idleRight";
    private final String pathToRunLeft = "monster/Boss/runLeft";
    private final String pathToRunRight = "monster/Boss/runRight";
    private final String pathToGetHit = "monster/Boss/getHit";
    private final String pathToDie = "monster/Boss/die";

    /**
     * Creates a Boss with all its components and attributes.
     * 
     * @param level The level of the Boss.
     *              The level determines the Boss's attributes.
     *              The higher the level, the stronger the Boss.
     *              The level also determines the Boss's AI.
     */
    public Boss(int level) {
        super(level);
        this.level = level;
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupHealthComponent();
        setupAIComponent();
        setupXPComponent();
        bossLogger.info("Boss des Levels:" + level + " created");
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

    /**
     * to set up the two Sills, the BossWalk, the transition range and the BossAI.
     */
    private void setupAIComponent() {
        FireballSkill s1 = new FireballSkill(new ITargetSelection() {
            @Override
            public Point selectTargetPoint() {
                return entityPosition();
            }
        },
                this);

        StabSkill s2 = new StabSkill(new ITargetSelection() {
            @Override
            public Point selectTargetPoint() {
                return entityPosition();
            }
        },
                this);
        SkillComponent sc = new SkillComponent(this);
        Skill skill1 = new Skill(s1, attackCooldown);
        Skill skill2 = new Skill(s2, attackCooldown);
        sc.addSkill(skill1);
        sc.addSkill(skill2);
        AIComponent a = new AIComponent(this);
        a.setIdleAI(new BossWalk());
        a.setTransitionAI(new RangeTransition(7f));
        a.setFightAI(new BossAI(skill1, skill2, attackRange));

    }

    private Point entityPosition() {
        bossLogger.info("Boss position requested");
        if (!Game.getHero().isPresent())
            return null;
        return ((PositionComponent) Game.getHero().get().getComponent(PositionComponent.class)
                .orElseThrow(
                        () -> new MissingComponentException(
                                "PositionComponent")))
                .getPosition();

    }

    private void attack(Entity entity) {
        bossLogger.info(entity + " attacked");
        Damage damage = new Damage(calcDamage(), DamageType.PHYSICAL, this);
        if (entity.getComponent(HealthComponent.class).isPresent()) {
            ((HealthComponent) entity.getComponent(HealthComponent.class).get()).receiveHit(damage);
        }
    }

    private int calcDamage() {
        return 2 + (int) Math.sqrt(4 * level);
    }

    private void setupXPComponent() {
        new XPComponent(this, new ILevelUp() {

            @Override
            public void onLevelUp(long nexLevel) {
                HealthComponent health = (HealthComponent) getComponent(HealthComponent.class).get();
                health.setMaximalHealthpoints((int) (health.getMaximalHealthpoints() * 1.01f));
                health.setCurrentHealthpoints(health.getMaximalHealthpoints());
            }

        }, 50 * level);
    }
}
