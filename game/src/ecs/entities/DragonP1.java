package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.fight.IFightAI;
import ecs.components.ai.idle.AlwaysAggro;
import ecs.components.ai.idle.IIdleAI;
import ecs.components.ai.transition.ITransition;
import ecs.components.ai.transition.RangeTransition;
import ecs.components.skill.*;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import graphic.Animation;
import level.tools.LevelElement;
import starter.Game;
import tools.Point;

public class DragonP1 extends BossMonster{
    private static final float X_SPEED = 0.1f;
    private static final float Y_SPEED = 0.1f;
    private static final int MAX_HEALTH = 10;
    private static final float ATTACK_RANGE = 3f;
    private static final float TRANSITION_RANGE = 3f;
    private static final String PATH_TO_IDLE_LEFT = "character/monster/Dragon/Phase1/idle_left/";
    private static final String PATH_TO_IDLE_RIGHT = "character/monster/Dragon/Phase1/idle_right/";
    private static final String PATH_TO_RUN_LEFT = "character/monster/Dragon/Phase1/run_left/";
    private static final String PATH_TO_RUN_RIGHT = "character/monster/Dragon/Phase1/run_right/";
    private Skill skill;

    /**
     * creates a dragon which is in its first phase
     *
     * @param position position to be created at
     */
    public DragonP1 (Point position) {
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
        setupSkill();
        setupHealthComponent();
        setupHitboxComponent();
        setupAIComponent();
    }

    /**
     * creates a dragon which is in its first phase at a random tile
     *
     * @return the created dragon
     */
    public static DragonP1 createNewDragonP1() {
        return new DragonP1(
            Game.currentLevel.getRandomTile(LevelElement.FLOOR).getCoordinate().toPoint());
    }

    // deal 1 physical damage on collision with hero
    private void action1(Entity entity) {
        Damage damage = new Damage(1, DamageType.PHYSICAL, this);
        if (entity instanceof Hero ) {
            Game.getHero().stream()
                .flatMap(e -> e.getComponent(HealthComponent.class).stream())
                .map(HealthComponent.class::cast)
                .forEach(healthComponent -> {healthComponent.receiveHit(damage);});
        }
    }

    // create a fireballSkill with a 3-second cooldown
    private void setupSkill() {
        skill = new Skill(new FireballSkill(SkillTools::getHeroPositionAsPoint), 3f, 0);
        new SkillComponent(this).addSkill(skill);
        ManaComponent mc = new ManaComponent(this);
        mc.setMaxMana(100f);
        mc.setManaRegenRate(1f);
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

    // HealthComponents onDeath function: create a dragon in its second phase
    private void setupHealthComponent() {
        Animation monsterHit = AnimationBuilder.buildAnimation("animation/");
        Animation monsterDeath = AnimationBuilder.buildAnimation("animation/");
        new HealthComponent(this, MAX_HEALTH,
            a -> Game.addEntity(new DragonP2(this.getComponent(PositionComponent.class).map(PositionComponent.class::cast).get().getPosition())),
            monsterHit, monsterDeath);
    }

    private IFightAI setupFightStrategy() {
        return new CollideAI(ATTACK_RANGE);
    }

    private IIdleAI setupIdleStrategy() {
        return new AlwaysAggro(100, 2, skill);
    }

    private ITransition setupTransitionStrategy() {
        return new RangeTransition(TRANSITION_RANGE);
    }
}
