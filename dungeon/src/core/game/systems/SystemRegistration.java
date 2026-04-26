package core.game.systems;

import core.game.ECSManagement;
import java.util.function.Supplier;

/**
 * Utility class for registering runtime systems with the ECS management backend.
 *
 * <p>Provides helper methods to safely register systems that should only exist once in the system
 * registry, making system registration idempotent across multiple startup paths.
 */
public final class SystemRegistration {

  private SystemRegistration() {}

  /**
   * Registers a runtime system only if no system of the same type is present yet.
   *
   * <p>This utility keeps system registrations idempotent even if multiple startup paths touch the
   * same system type.
   *
   * @param type system type to check
   * @param factory factory used to create the system if absent
   * @param <T> concrete system type
   */
  public static <T extends core.System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
