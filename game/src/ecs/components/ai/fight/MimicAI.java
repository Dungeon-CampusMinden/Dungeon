package ecs.components.ai.fight;

import ecs.components.skill.Skill;
import ecs.entities.Entity;
import ecs.entities.Mimic;

public class MimicAI implements IFightAI{

    private final Skill skill;


    public MimicAI (Skill skill){
        this.skill = skill;
    }

    @Override
    public void fight(Entity entity) {
        if (!(entity instanceof Mimic)){
            return;
        }
        if(((Mimic)entity).getAttacking())
            skill.execute(entity);
    }



}
