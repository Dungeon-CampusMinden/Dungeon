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
import ecs.components.skill.StabSkill;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import graphic.Animation;
import level.LevelAPI;
import starter.Game;
import tools.Point;

public class Boss extends Monster {
    private final float xSpeed = 0.2f;
    private final float ySpeed = 0.2f;
    private final int maxHealth = 200;
    private final float attackRange = 0.5f;
    private int level;
    private int attackCooldown = 1;

    private final String pathToIdleLeft = "monster/Boss/idleLeft";
    private final String pathToIdleRight = "monster/Boss/idleRight";
    private final String pathToRunLeft = "monster/Boss/runLeft";
    private final String pathToRunRight = "monster/Boss/runRight";
    private final String pathToGetHit = "monster/Boss/getHit";
    private final String pathToDie = "monster/Boss/die";

    /** Entity with Components */
    public Boss(int level) {
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

    private void setupAIComponent() {
        FireballSkill s1 = new FireballSkill(new ITargetSelection() {
            @Override
            public Point selectTargetPoint() {
                return entityPosition();
            }
        });

        StabSkill s2 = new StabSkill(new ITargetSelection() {
            @Override
            public Point selectTargetPoint() {
                return entityPosition();
            }
        });
        AIComponent a = new AIComponent(this);
        a.setIdleAI(new BossWalk());
        a.setTransitionAI(new RangeTransition(7f));
        a.setFightAI(new BossAI(new Skill(s1, 4), new Skill(s2, 4)));

    }

    private Point entityPosition() {
        if (Game.getHero().isPresent()) {
            return ((PositionComponent) Game.getHero().get().getComponent(PositionComponent.class)
                    .orElseThrow(
                            () -> new MissingComponentException(
                                    "PositionComponent")))
                    .getPosition();
        }
        return null;
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
