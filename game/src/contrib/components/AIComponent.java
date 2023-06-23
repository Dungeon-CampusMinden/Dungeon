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
 * Defines the behavior of AI-controlled entities.
 *
 * <p>The {@link #idleAI} defines the behavior in Idle-State, e.g. walking on a specific path {@link
 * contrib.utils.components.ai.idle.PatrouilleWalk}.
 *
 * <p>The {@link #transitionAI} defines when the entity goes into fight mode, e.g. if the player is
 * too close to the entity {@link RangeTransition}.
 *
 * <p>The {@link #fightAI} defines the combat behavior, e.g. attacking with a fireball skill {@link
 * contrib.utils.components.ai.fight.RangeAI}.
 *
 * <p>The {@link AISystem} will use the {@link #transitionAI} to check if the idle or combat
 * behavior should be executed.
 *
 * @see AISystem
 */
@DSLType(name = "ai_component")
public final class AIComponent extends Component {
    private Consumer<Entity> fightAI;
    private Consumer<Entity> idleAI;
    private Function<Entity, Boolean> transitionAI;

    /**
     * Create an AIComponent with the given behavior and add it to the associated entity.
     *
     * @param entity The associated entity.
     * @param fightAI The combat behavior.
     * @param idleAI The idle behavior.
     * @param transition Determines when to fight.
     */
    public AIComponent(
            final Entity entity,
            final Consumer<Entity> fightAI,
            final Consumer<Entity> idleAI,
            final Function<Entity, Boolean> transition) {
        super(entity);
        this.fightAI = fightAI;
        this.idleAI = idleAI;
        this.transitionAI = transition;
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
     * Check if the entity is in idle mode or in fight mode and execute the corresponding behavior
     */
    public void execute() {
        if (transitionAI.apply(entity)) fightAI.accept(entity);
        else idleAI.accept(entity);
    }
}
