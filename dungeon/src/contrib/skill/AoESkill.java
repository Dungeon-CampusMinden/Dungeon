package contrib.skill;

import core.Entity;

public class AoESkill extends Skill{

    private float radius;

    public AoESkill(String name, long cooldown) {
        super(name, cooldown);
    }

    @Override
    protected void executeSkill(Entity caster) {

    }
}
