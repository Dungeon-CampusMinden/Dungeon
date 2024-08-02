package contrib.systems;

import core.System;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** This class manages a collection of standard systems. */
public class StandardSystems {
  /**
   * Gets a mutable set of all base systems classes, without the systems in the {@code toExclude}
   * set.
   *
   * @param toExclude set of system classes to exclude, can be empty or null
   * @return set of base systems classes
   */
  public static Set<Class<? extends System>> mutableSetOfBaseSystems(
      Set<Class<? extends System>> toExclude) {
    Set<Class<? extends System>> set =
        new HashSet<>(
            Set.of(
                AISystem.class,
                CollisionSystem.class,
                HealthBarSystem.class,
                HealthSystem.class,
                HudSystem.class,
                IdleSoundSystem.class,
                PathSystem.class,
                ProjectileSystem.class,
                SpikeSystem.class));
    if (toExclude != null) {
      set.removeAll(toExclude);
    }
    return set;
  }

  /**
   * Instantiates all standard systems in a new, unmodifiable set.
   *
   * @return set of all standard systems
   */
  public static Set<System> constructStandardSystems() {
    return mutableSetOfBaseSystems(Set.of()).stream()
        .map(
            sc -> {
              try {
                return sc.getDeclaredConstructor().newInstance();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .collect(Collectors.toSet());
  }

  /**
   * Instantiates all given systems in a new, unmodifiable set.
   *
   * @param systemClasses set of system classes
   * @return set of all given systems
   */
  public static Set<System> constructStandardSystems(Set<Class<? extends System>> systemClasses) {
    return systemClasses.stream()
        .map(
            sc -> {
              try {
                return sc.getDeclaredConstructor().newInstance();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
        .collect(Collectors.toSet());
  }
}
