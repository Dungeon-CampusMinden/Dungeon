package ecs.components.ai;

import ecs.components.Component;
import ecs.components.ai.fight.IFightAI;
import ecs.components.ai.idle.IIdleAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.transition.ITransition;
import ecs.components.ai.transition.RangeTransition;
import ecs.entities.Entity;

/** AIComponent is a component that stores the idle and combat behavior of AI controlled entities */
public class AIComponent extends Component {

    public static String name = "AIComponent";
    private final IFightAI fightAI;
    private final IIdleAI idleAI;
    private final ITransition transition;

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
        this.transition = transition;
    }

    /**
     * @param entity associated entity
     */
    public AIComponent(Entity entity) {
        super(entity);
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
