package ecs.components.ai;

import ecs.components.Component;
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
    private /*@DSLTypeMember*/ IFightAI fightAI;
    private /*@DSLTypeMember*/ IIdleAI idleAI;
    private /*@DSLTypeMember*/ ITransition transition;

    /**
     * @param entity associated entity
     * @param fightAI combat behavior
     * @param idleAI idle behavior
     * @param transition Determines when to fight
     */
    public AIComponent(Entity entity, IFightAI fightAI, IIdleAI idleAI, ITransition transition) {
        super(entity, name);
        this.fightAI = fightAI;
        this.idleAI = idleAI;
        this.transition = transition;
    }

    /**
     * @param entity associated entity
     */
    public AIComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity, name);
        System.out.println("DEBUG AI");
        idleAI = new RadiusWalk(5);
        transition = new RangeTransition(1.5f);
        fightAI =
                entity1 -> {
                    System.out.println("TIME TO FIGHT!");
                    // todo replace with melee skill
                };
    }

    /** Excecute the ai behavior */
    public void execute() {
        if (transition.isInFightMode(entity)) fightAI.fight(entity);
        else idleAI.idle(entity);
    }
}
