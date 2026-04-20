package contrib.client;

import core.System;
import core.game.ECSManagement;
import java.util.function.Supplier;

/**
 * A utility class for managing system client installations in the ECS (Entity Component System)
 * management framework. This class provides functionality to add system client instances
 * if they are not already registered.
 *
 * <p>This class is not instantiable and serves only as a utility container for its static methods.
 */
final class SystemClientInstaller {

  private SystemClientInstaller() {}

  static <T extends System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
