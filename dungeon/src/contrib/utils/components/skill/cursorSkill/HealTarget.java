package contrib.utils.components.skill.cursorSkill;

import contrib.components.HealthComponent;
import contrib.utils.components.skill.Resource;
import core.Entity;
import core.Game;
import core.utils.Point;
import core.utils.Tuple;
import java.util.function.BiConsumer;

public class HealTarget extends CursorSkill {

  public static final String NAME = "HEAL_ON_CURSOR";
  private int healAmount;

  private BiConsumer<Entity, Point> heal =
      (entity, point) -> Game.entityAtPoint(point).findFirst().ifPresent(e -> heal(e));

  public HealTarget(long cooldown, int healAmount, Tuple<Resource, Integer>... resourceCost) {
    super(NAME, cooldown, resourceCost);
    this.healAmount = healAmount;
    this.executeOnCursor(heal);
  }

  private void heal(Entity entity) {
    entity.fetch(HealthComponent.class).ifPresent(hc -> hc.restoreHealthpoints(healAmount));
  }

  public void healAmount(int healAmount) {
    this.healAmount = healAmount;
  }

  public int healAmount() {
    return healAmount;
  }
}
