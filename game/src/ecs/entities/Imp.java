package ecs.entities;

import ecs.components.ai.idle.IIdleAI;
import level.tools.LevelElement;
import tools.Point;
import starter.Game;
import ecs.components.ai.idle.*;
import ecs.components.ai.fight.*;
import ecs.components.ai.transition.*;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.skill.*;
import ecs.components.ai.AIComponent;
import ecs.components.PositionComponent;

public class Imp extends Monster {
    
    private static final float X_SPEED = 0.15f;
    private static final float Y_SPEED = 0.15f;
    private static final int MAX_HEALTH = 2;
    private static final float ATTACK_RANGE = 4f;
    private static final float TRANSITION_RANGE = 6f;
    private static float MAX_MANA = 10f;
    private static float MANA_REGEN = 2f;

    private static final String PATH_TO_IDLE_LEFT = "monster/imp/idleLeft";
    private static final String PATH_TO_IDLE_RIGHT = "monster/imp/idleRight";
    private static final String PATH_TO_RUN_LEFT = "monster/imp/runLeft";
    private static final String PATH_TO_RUN_RIGHT = "monster/imp/runRight";

    private Skill skill;

    /**
     * Creates a new Imp at the given position.
     * The imp will try to hurt the hero.
     * @param position
     */
    public Imp(Point position) {
        super(
            X_SPEED,
            Y_SPEED,
            MAX_HEALTH,
            ATTACK_RANGE,
            PATH_TO_IDLE_LEFT,
            PATH_TO_IDLE_RIGHT,
            PATH_TO_RUN_LEFT,
            PATH_TO_RUN_RIGHT,
            position
            );

        setupHitboxComponent();
        setupSkillComponent();
        setupAIComponent();
        setupManaComponent();
    }

    /**
     * Creates a new Imp at a random position on the current level.
     * 
     * @return a new Imp Monster
     */
    public static Imp createNewImp() {
        return new Imp(
            Game.currentLevel.getRandomTile(LevelElement.FLOOR).getCoordinate().toPoint());
    }

    private void action1(Entity entity) {
        System.out.println("Imp attack" + entity.getClass());
        Damage damage = new Damage(2, DamageType.PHYSICAL, this);
        if (entity instanceof Hero ) {
            Game.getHero().stream()
            .flatMap(e -> e.getComponent(HealthComponent.class).stream())
            .map(HealthComponent.class::cast)
            .forEach(healthComponent -> {healthComponent.receiveHit(damage);});
        }
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> action1(other),
                (you, other, direction) -> System.out.println("monsterCollision")
        );
    }

    private void setupManaComponent() {
        ManaComponent mc = new ManaComponent(this);
        mc.setMaxMana(MAX_MANA);
        mc.adjustCurrentMana(MANA_REGEN);
    }

    private void setupSkillComponent() {
        skill = new Skill(
                new FireballSkill(SkillTools::getHeroPositionAsPoint), 3f, 0f);
        new SkillComponent(this).addSkill(skill);
    }

    private void setupAIComponent() {
        new AIComponent(this, setupFightStrategy(),
                        setupIdleStrategy(), setupTransitionStrategy());
    }

    private IFightAI setupFightStrategy() {
        return new MeleeAI(ATTACK_RANGE, skill);
    }

    private IIdleAI setupIdleStrategy() {
        return new RadiusWalk(3, 2);
    }

    private ITransition setupTransitionStrategy() {
        return new RangeTransition(TRANSITION_RANGE);
    }
}
