package contrib.skill;

import core.Entity;
import core.utils.Point;

import java.util.function.BiConsumer;

public class CursorSkill extends Skill {

    private BiConsumer<Entity,Point> executeOnCursor;

    public CursorSkill(String name, long cooldown,BiConsumer<Entity,Point> executeOnCursor) {
        super(name, cooldown);
        this.executeOnCursor=executeOnCursor;
    }

    @Override
    protected void executeSkill(Entity caster) {
        executeOnCursor.accept(caster,SkillTools.cursorPositionAsPoint());
    }

}
