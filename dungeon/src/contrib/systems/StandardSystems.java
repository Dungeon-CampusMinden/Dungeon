package contrib.systems;

import core.System;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** This class manages a collection of standard systems. */
public class StandardSystems {
  private static final Set<Class<? extends System>> DEFAULT_SYSTEMS =
      Set.of(
          AISystem.class,
          CollisionSystem.class,
          HealthBarSystem.class,
          HealthSystem.class,
          HudSystem.class,
          PathSystem.class,
          ProjectileSystem.class);

  /**
   * Creates a set of standard systems.
   *
   * <p>The standard systems are: {@link StandardSystems#DEFAULT_SYSTEMS}.
   *
   * <p>Can be passed to {@link core.Game#add(Set)} to add them to the game.
   *
   * @param toInclude set of system classes to include from the standard set, null is not allowed,
   *     could be empty
   * @param toExclude set of system classes to exclude from the standard set, null is not allowed,
   *     could be empty
   * @return set of standard systems
   * @see System
   * @see core.Game
   * @see Set
   */
  public static Set<System> standardSystems(
      Set<Class<? extends System>> toInclude, Set<Class<? extends System>> toExclude) {
    Set<Class<? extends System>> set = new HashSet<>(DEFAULT_SYSTEMS);
    set.addAll(toInclude);
    set.removeAll(toExclude);
    return set.stream()
        .map(
            sc -> {
              try {
                return (System) sc.getConstructor().newInstance();
              } catch (Exception e) {
                // Exception during system construction.
                // This should never happen.
                throw new RuntimeException(e);
              }
            })
        .collect(Collectors.toSet());
  }
}
