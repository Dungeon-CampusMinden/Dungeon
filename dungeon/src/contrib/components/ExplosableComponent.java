package contrib.components;

import contrib.utils.components.health.DamageType;
import core.Component;
import core.Entity;
import core.utils.Point;

public record ExplosableComponent(Handler onExplosionHit) implements Component {
  @FunctionalInterface
  public interface Handler {
    void onExplosionHit(
        Entity self, Point center, float radius, DamageType dmgType, int dmgAmount, Entity source);
  }
}
