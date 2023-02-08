package ecs.components.ai;

import ecs.components.Component;
import ecs.components.ai.fight.CollideAI;
import ecs.components.ai.fight.IFightAI;
import ecs.components.ai.idle.IIdleAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.transition.ITransition;
import ecs.components.ai.transition.RangeTransition;
import ecs.entities.Entity;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;

/** AIComponent is a component that stores the idle and combat behavior of AI controlled entities */
@DSLType(name = "ai_component")
public class AIComponent extends Component {

    public static String name = "AIComponent";
    private /*@DSLTypeMember(name="fight_ai)*/ IFightAI fightAI;
    private /*@DSLTypeMember(name="idle_ai)*/ IIdleAI idleAI;
    private /*@DSLTypeMember(name="transition_ai)*/ ITransition transitionAI;

    /**
     * @param entity associated entity
     * @param fightAI combat behavior
     * @param idleAI idle behavior
     * @param transition Determines when to fight
     */
    public AIComponent(Entity entity, IFightAI fightAI, IIdleAI idleAI, ITransition transition) {
        super(entity);
        this.fightAI = fightAI;
        this.idleAI = idleAI;
        this.transitionAI = transition;
    }

    /**
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
        if (transitionAI.isInFightMode(entity)) fightAI.fight(entity);
        else idleAI.idle(entity);
    }

    /**
     * Set a new fight ai
     *
     * @param ai new fight ai
     */
    public void setFightAI(IFightAI ai) {
        this.fightAI = ai;
    }

    /**
     * Set a new idle ai
     *
     * @param ai new idle ai
     */
    public void setIdleAI(IIdleAI ai) {
        this.idleAI = ai;
    }

    /**
     * Set a new transition ai
     *
     * @param ai new transition ai
     */
    public void setTransitionAI(ITransition ai) {
        this.transitionAI = ai;
    }

    /**
     * Returns the idle AI of the AIComponent
     *
     * @return IIdleAI object representing the idle AI
     */
    public IIdleAI getIdleAI() {
        return idleAI;
    }

    /**
     * Returns the transition AI of the AIComponent
     *
     * @return ITransition object representing the transition AI
     */
    public ITransition getTransitionAI() {
        return transitionAI;
    }

    /**
     * Returns the fight AI of the AIComponent
     *
     * @return IFigthAI object representing the fight AI
     */
    public IFightAI getFightAI() {
        return fightAI;
    }
}
