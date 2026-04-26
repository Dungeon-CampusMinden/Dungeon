package core.platform.client.loop;

import core.game.ECSManagement;
import java.util.function.Supplier;

/**
 * Extension point for client loop host startup.
 *
 * <p>Implementations can contribute client-specific platform services and runtime systems without
 * making {@link ClientLoopHost} depend on those concrete features. Installers are passed explicitly
 * to a {@link ClientLoopHost} constructor.
 */
public interface ClientLoopHostInstaller {

  /**
   * Installs client-side platform services after core platform adapters and input handling are
   * wired.
   */
  default void installPlatformServices() {}

  /**
   * Installs client-side runtime systems after the core client system profile has been initialized.
   */
  default void installRuntimeSystems() {}

  /**
   * Registers a runtime system only if no system of the same type is present yet.
   *
   * <p>This keeps installer contributions idempotent even if multiple startup paths touch the same
   * runtime system.
   *
   * @param type system type to check
   * @param factory factory used to create the system if absent
   * @param <T> concrete system type
   */
  static <T extends core.System> void addSystemIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
