package contrib.skill.cursorSkill;

import contrib.components.HealthComponent;
import core.Entity;
import core.Game;
import core.utils.Point;
import java.util.function.BiConsumer;

public class HealTarget extends CursorSkill {

  public static final String NAME = "HEAL_ON_CURSOR";
  private int healAmount;

  private BiConsumer<Entity, Point> heal =
      (entity, point) -> Game.entityAtPoint(point).findFirst().ifPresent(e -> heal(e));

  public HealTarget(long cooldown, int healAmount) {
    super(NAME, cooldown);
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
