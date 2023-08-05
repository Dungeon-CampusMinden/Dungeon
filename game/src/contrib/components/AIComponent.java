package contrib.components;

import contrib.systems.AISystem;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;

import core.Component;
import core.Entity;

import semanticanalysis.types.DSLType;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Define the behavior of AI-controlled entities.
 *
 * <p>An AI-controlled entity can have two different states which define the behaviour of the
 * entity. The "idle state" describes the default behaviour of the entity, like walking around in
 * the level. The "combat state" describes the fighting behaviour, like throwing fireballs at the
 * hero. The {@link AISystem} will trigger {@link #execute()} which uses {@link #shouldFight} to
 * check if the idle or combat behaviour should be executed.
 *
 * <p>The {@link #idleBehavior} defines the behaviour in idle state, e.g. walking on a specific path
 * {@link contrib.utils.components.ai.idle.PatrouilleWalk}.
 *
 * <p>The {@link #fightBehavior} defines the combat behaviour, e.g. attacking with a fireball skill
 * {@link contrib.utils.components.ai.fight.RangeAI}.
 *
 * <p>The {@link #shouldFight} defines when the entity goes into fight mode, e.g. if the player is
 * too close to the entity {@link RangeTransition}.
 *
 * @see AISystem
 */
@DSLType(name = "ai_component")
public final class AIComponent implements Component {
    private final Consumer<Entity> fightBehavior;
    private final Consumer<Entity> idleBehavior;
    private final Function<Entity, Boolean> shouldFight;

    /**
     * Create an AIComponent with the given behavior and add it to the associated entity.
     *
     * @param fightBehavior The combat behavior.
     * @param idleBehavior The idle behavior.
     * @param shouldFight Determines when to fight.
     */
    public AIComponent(
            final Consumer<Entity> fightBehavior,
            final Consumer<Entity> idleBehavior,
            final Function<Entity, Boolean> shouldFight) {
        this.fightBehavior = fightBehavior;
        this.idleBehavior = idleBehavior;
        this.shouldFight = shouldFight;
    }

    /**
     * Create an AIComponent with default behavior and add it to the associated entity.
     *
     * <p>The default behavior uses {@link RadiusWalk} as the idle behavior, {@link RangeTransition}
     * as the transition function, and {@link CollideAI} as the fight behavior.
     */
    public AIComponent() {
        this(new CollideAI(2f), new RadiusWalk(5, 2), new RangeTransition(5f));
    }

    /**
     * Execute AI behavior.
     *
     * <p>Uses {@link #shouldFight} to check if the entity is in idle mode or in fight mode and
     * execute the corresponding behavior
     */
    public void execute(Entity entity) {
        if (shouldFight.apply(entity)) fightBehavior.accept(entity);
        else idleBehavior.accept(entity);
    }
}
