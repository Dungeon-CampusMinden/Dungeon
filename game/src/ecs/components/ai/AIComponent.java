package ecs.components.ai;

import ecs.components.Component;
import ecs.components.ai.fight.FightAI;
import ecs.components.ai.idle.IdleAI;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.transition.RangeTransition;
import ecs.components.ai.transition.Transition;
import ecs.entities.Entity;

public class AIComponent extends Component {

    public static String name = "AIComponent";
    private FightAI fightAI;
    private IdleAI idleAI;
    private Transition transition;

    public AIComponent(Entity entity, FightAI fightAI, IdleAI idleAI, Transition transition) {
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
                new FightAI() {
                    @Override
                    public void fight(Entity entity) {
                        System.out.println("TIME TO FIGHT!");
                        // todo replace with melee skill
                    }
                };
    }

    public void execute() {
        if (transition.goFightMode(entity)) fightAI.fight(entity);
        else idleAI.idle(entity);
    }
}
