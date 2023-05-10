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
import ecs.components.ai.AIComponent;

public class Chort extends Monster {
        
    private static final float X_SPEED = 0.2f;
    private static final float Y_SPEED = 0.2f;
    private static final int MAX_HEALTH = 5;
    private static final float ATTACK_RANGE = 4f;
    private static final float TRANSITION_RANGE = 3f;
    private static final float MOVE_RADIUS = 4f;

    private static final String PATH_TO_IDLE_LEFT = "monster/chort/idleLeft";
    private static final String PATH_TO_IDLE_RIGHT = "monster/chort/idleRight";
    private static final String PATH_TO_RUN_LEFT = "monster/chort/runLeft";
    private static final String PATH_TO_RUN_RIGHT = "monster/chort/runRight";


    /**
     * Creates a new Chort at the given position.
     * The imp will try to hurt the hero.
     * @param position
     */
    public Chort(Point position) {
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
        setupAIComponent();
    }

    /**
     * Creates a new Chort at a random position on the current level.
     * 
     * @return a new Chort Monster
     */
    public static Chort createNewChort() {
        return new Chort(
            Game.currentLevel.getRandomTile(LevelElement.FLOOR).getCoordinate().toPoint());
    }

    /**
     * The action the Chort performs when it hits another entity.
     * If the entity is a hero, it will take damage.
     * @param entity
     */
    private void action1(Entity entity) {
        System.out.println("Chort attack" + entity.getClass());
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

    private void setupAIComponent() {
        new AIComponent(this, setupFightStrategy(),
                        setupIdleStrategy(), setupTransitionStrategy());
    }

    private IFightAI setupFightStrategy() {
        return new CollideAI(ATTACK_RANGE);
    }

    private IIdleAI setupIdleStrategy() {
        return new WalkToEndTile(MOVE_RADIUS);
    }

    private ITransition setupTransitionStrategy() {
        return new RangeTransition(TRANSITION_RANGE);
    }
}
