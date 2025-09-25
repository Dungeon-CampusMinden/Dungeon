package contrib.components;

import contrib.utils.components.health.DamageType;
import core.Component;
import core.Entity;
import core.utils.Point;

/**
 * Component that marks an entity as reacting to explosions.
 *
 * <p>Attach this component to an entity to define custom behavior when an explosion occurs within
 * its area of effect. The provided {@link Handler} is invoked by explosion logic with information
 * such as the explosion center, radius and damage parameters.
 */
public record ExplosableComponent(Handler onExplosionHit) implements Component {

  /**
   * Functional callback executed when an entity is affected by an explosion.
   *
   * @param self The entity that owns the {@link ExplosableComponent}.
   * @param center The world position of the explosion center.
   * @param radius The explosion radius in world units.
   * @param dmgType The type of damage dealt by the explosion.
   * @param dmgAmount The base damage amount of the explosion.
   * @param source The source entity responsible for the explosion (e.g., the bomb or skill caster).
   */
  @FunctionalInterface
  public interface Handler {
    void onExplosionHit(
        Entity self, Point center, float radius, DamageType dmgType, int dmgAmount, Entity source);
  }
}
