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
 * AIComponent is a component that stores the idle and combat behavior of AI controlled entities.
 *
 * <p>The {@link AISystem AISystem} determines which behavior is used. If the implicit constructor
 * is used the entity will have a default behavior composed of a {@link RadiusWalk}, {@link
 * RangeTransition} and {@link CollideAI}.
 */
@DSLType(name = "ai_component")
public class AIComponent extends Component {

    public static String name = "AIComponent";
    private /*@DSLTypeMember(name="fight_ai)*/ Consumer<Entity> fightAI;
    private /*@DSLTypeMember(name="idle_ai)*/ Consumer<Entity> idleAI;
    private /*@DSLTypeMember(name="transition_ai)*/ Function<Entity, Boolean> transitionAI;

    /**
     * Create AIComponent with the given behavior.
     *
     * @param entity associated entity
     * @param fightAI combat behavior
     * @param idleAI idle behavior
     * @param transition Determines when to fight
     */
    public AIComponent(
            Entity entity,
            Consumer<Entity> fightAI,
            Consumer<Entity> idleAI,
            Function<Entity, Boolean> transition) {
        super(entity);
        this.fightAI = fightAI;
        this.idleAI = idleAI;
        this.transitionAI = transition;
    }

    /**
     * Creates AIComponent with default behavior. For default behavior see class documentation of
     * AIComponent.
     *
     * @param entity associated entity
     */
    public AIComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        idleAI = new RadiusWalk(5, 2);
        transitionAI = new RangeTransition(5f);
        fightAI = new CollideAI(2f);
    }

    /** Excecute the ai behavior */
    public void execute() {
        if (transitionAI.apply(entity)) fightAI.accept(entity);
        else idleAI.accept(entity);
    }

    /**
     * Set a new fight ai
     *
     * @param ai new fight ai
     */
    public void fightAI(Consumer<Entity> ai) {
        this.fightAI = ai;
    }

    /**
     * Set a new idle ai
     *
     * @param ai new idle ai
     */
    public void idleAI(Consumer<Entity> ai) {
        this.idleAI = ai;
    }

    /**
     * Set a new transition ai
     *
     * @param ai new transition ai
     */
    public void setTransitionAI(Function<Entity, Boolean> ai) {
        this.transitionAI = ai;
    }

    /**
     * Returns the idle AI of the AIComponent
     *
     * @return IIdleAI object representing the idle AI
     */
    public Consumer<Entity> idleAI() {
        return idleAI;
    }

    /**
     * Returns the transition AI of the AIComponent
     *
     * @return ITransition object representing the transition AI
     */
    public Function<Entity, Boolean> transitionAI() {
        return transitionAI;
    }

    /**
     * Returns the fight AI of the AIComponent
     *
     * @return IFigthAI object representing the fight AI
     */
    public Consumer<Entity> fightAI() {
        return fightAI;
    }
}
