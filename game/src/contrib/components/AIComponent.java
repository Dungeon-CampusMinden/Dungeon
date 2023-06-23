package contrib.components;

import contrib.systems.AISystem;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;

import core.Component;
import core.Entity;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Define the behavior of AI-controlled entities.
 *
 * <p>The {@link AISystem} will use {@link #shouldFight} to check if the idle or combat behavior
 * should be executed.
 *
 * <p>The {@link #idleBehavior} defines the behavior in Idle-State, e.g. walking on a specific path
 * {@link contrib.utils.components.ai.idle.PatrouilleWalk}.
 *
 * <p>The {@link #shouldFight} defines when the entity goes into fight mode, e.g. if the player is
 * too close to the entity {@link RangeTransition}.
 *
 * <p>The {@link #fightBehavior} defines the combat behavior, e.g. attacking with a fireball skill
 * {@link contrib.utils.components.ai.fight.RangeAI}.
 *
 * @see AISystem
 */
@DSLType(name = "ai_component")
public final class AIComponent extends Component {
    private final Consumer<Entity> fightBehavior;
    private final Consumer<Entity> idleBehavior;
    private final Function<Entity, Boolean> shouldFight;

    /**
     * Create an AIComponent with the given behavior and add it to the associated entity.
     *
     * @param entity The associated entity.
     * @param fightBehavior The combat behavior.
     * @param idleBehavior The idle behavior.
     * @param shouldFight Determines when to fight.
     */
    public AIComponent(
            final Entity entity,
            final Consumer<Entity> fightBehavior,
            final Consumer<Entity> idleBehavior,
            final Function<Entity, Boolean> shouldFight) {
        super(entity);
        this.fightBehavior = fightBehavior;
        this.idleBehavior = idleBehavior;
        this.shouldFight = shouldFight;
    }

    /**
     * Create an AIComponent with default behavior and add it to the associated entity.
     *
     * <p>The default behavior uses {@link RadiusWalk} as the idle behavior, {@link RangeTransition}
     * as the transition function, and {@link CollideAI} as the fight behavior.
     *
     * @param entity The associated entity.
     */
    public AIComponent(@DSLContextMember(name = "entity") final Entity entity) {
        this(entity, new CollideAI(2f), new RadiusWalk(5, 2), new RangeTransition(5f));
    }

    /**
     * Exceute AI behavior.
     *
     * <p>Uses {@link #shouldFight} to check if the entity is in idle mode or in fight mode and
     * execute the corresponding behavior
     */
    public void execute() {
        if (shouldFight.apply(entity)) fightBehavior.accept(entity);
        else idleBehavior.accept(entity);
    }
}
