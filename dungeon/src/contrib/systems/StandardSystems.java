package contrib.systems;

import core.System;
import java.util.Set;

/** A collection of standard systems. */
public class StandardSystems {
  /**
   * Gets a set of standard systems to use with {@link core.Game#add(Set)}.
   *
   * @return a set of standard systems
   * @see core.Game#add(Set)
   * @see System
   */
  public static Set<System> standardSystems() {
    return Set.of(
        new CollisionSystem(),
        new AISystem(),
        new HealthSystem(),
        new PathSystem(),
        new ProjectileSystem(),
        new HealthBarSystem(),
        new HudSystem(),
        new SpikeSystem(),
        new IdleSoundSystem());
  }
}
