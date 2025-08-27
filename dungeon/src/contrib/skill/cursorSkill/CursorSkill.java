package contrib.skill.cursorSkill;

import contrib.skill.Skill;
import contrib.skill.SkillTools;
import core.Entity;
import core.utils.Point;
import java.util.function.BiConsumer;

public class CursorSkill extends Skill {

  private BiConsumer<Entity, Point> executeOnCursor;

  public CursorSkill(String name, long cooldown, BiConsumer<Entity, Point> executeOnCursor) {
    super(name, cooldown);
    this.executeOnCursor = executeOnCursor;
  }

  public CursorSkill(String name, long cooldown) {
    super(name, cooldown);
  }

  public BiConsumer<Entity, Point> executeOnCursor() {
    return executeOnCursor;
  }

  public void executeOnCursor(BiConsumer<Entity, Point> executeOnCursor) {
    this.executeOnCursor = executeOnCursor;
  }

  @Override
  protected void executeSkill(Entity caster) {

    if (executeOnCursor != null) executeOnCursor.accept(caster, SkillTools.cursorPositionAsPoint());
  }
}
