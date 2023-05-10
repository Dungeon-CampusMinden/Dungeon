package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.fight.IFightAI;
import ecs.components.ai.idle.IIdleAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.transition.ITransition;
import ecs.components.ai.transition.RangeTransition;
import ecs.components.skill.*;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import graphic.Animation;
import starter.Game;
import tools.Point;

public class DragonP2 extends BossMonster{
    private static final float X_SPEED = 0.1f;
    private static final float Y_SPEED = 0.1f;
    private static final int MAX_HEALTH = 10;
    private static final float ATTACK_RANGE = 3f;
    private static final float TRANSITION_RANGE = 3f;
    private static final String PATH_TO_IDLE_LEFT = "character/monster/Dragon/Phase2/idle_left/";
    private static final String PATH_TO_IDLE_RIGHT = "character/monster/Dragon/Phase2/idle_right/";
    private static final String PATH_TO_RUN_LEFT = "character/monster/Dragon/Phase2/run_left/";
    private static final String PATH_TO_RUN_RIGHT = "character/monster/Dragon/Phase2/run_right/";
    private Skill skill;

    /**
     * creates a dragon which is in its second phase
     *
     * @param position position to be created at
     */
    public DragonP2 (Point position) {
        super(X_SPEED,
            Y_SPEED,
            MAX_HEALTH,
            ATTACK_RANGE,
            PATH_TO_IDLE_LEFT,
            PATH_TO_IDLE_RIGHT,
            PATH_TO_RUN_LEFT,
            PATH_TO_RUN_RIGHT,
            position
        );
        spawnMonsters();
        setupSkill();
        setupHealthComponent();
        setupHitboxComponent();
        setupAIComponent();
    }

    // deal 2 physical damage on collision with hero
    private void action1(Entity entity) {
        System.out.println("Dragon1 attack1" + entity.getClass());
        Damage damage = new Damage(2, DamageType.PHYSICAL, this);
        if (entity instanceof Hero ) {
            Game.getHero().stream()
                .flatMap(e -> e.getComponent(HealthComponent.class).stream())
                .map(HealthComponent.class::cast)
                .forEach(healthComponent -> {healthComponent.receiveHit(damage);});
        }
    }

    // create a fireballSkill with a 1.5-second cooldown
    private void setupSkill() {
        skill = new Skill(new FireballSkill(SkillTools::getHeroPositionAsPoint), 1.5f, 0);
        new SkillComponent(this).addSkill(skill);
        ManaComponent mc = new ManaComponent(this);
        mc.setMaxMana(100f);
        mc.setManaRegenRate(1f);
    }

    // HealthComponents onDeath function: grant the hero 100 xp, call method to unlock the exit
    private void setupHealthComponent() {
        Animation monsterHit = AnimationBuilder.buildAnimation("animation/");
        Animation monsterDeath = AnimationBuilder.buildAnimation("animation/");
        new HealthComponent(this, MAX_HEALTH,
            a -> {Game.getHero().ifPresent(h -> ((Hero) h).addXP(100));
                    Game.setDragonExistsFalse();},
            monsterHit, monsterDeath);
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
            this,
            (you, other, direction) -> action1(other),
            (you, other, direction) -> System.out.println("monsterCollision")
        );
    }

    private void setupAIComponent() {
        new AIComponent(this, setupFightStrategy(),
            setupIdleStrategy(), setupTransitionStrategy());
    }

    private IFightAI setupFightStrategy() {
        return new CollideAI(ATTACK_RANGE);
    }

    private IIdleAI setupIdleStrategy() {
        return new RadiusWalk(3, 2);
    }

    private ITransition setupTransitionStrategy() {
        return new RangeTransition(TRANSITION_RANGE);
    }

    // create other monsters to fight alongside itself
    private void spawnMonsters() {
        // TODO
    }
}
