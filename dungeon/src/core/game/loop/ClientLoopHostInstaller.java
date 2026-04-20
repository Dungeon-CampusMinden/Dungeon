package core.game.loop;

/**
 * Extension point for client loop host startup.
 *
 * <p>Implementations can contribute client-specific platform services and runtime systems without
 * making {@link ClientLoopHost} depend on those concrete features. Providers can be registered via
 * {@link java.util.ServiceLoader} or passed explicitly to a {@link ClientLoopHost} constructor.
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
}
