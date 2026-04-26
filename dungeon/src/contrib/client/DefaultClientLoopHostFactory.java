package contrib.client;

import contrib.client.install.ClientPresentationInstaller;
import contrib.client.install.DebugClientInstaller;
import core.game.loop.ClientLoopHost;
import core.game.loop.ClientLoopHostInstaller;
import core.platform.Platform;
import java.util.List;

/**
 * Provides factory-style utilities for creating and installing the default client loop host.
 *
 * <p>This class centralizes the creation of the default {@link ClientLoopHost} and the default set
 * of {@link ClientLoopHostInstaller}s used during client startup.
 *
 * <p>All methods in this class are static and intended to be invoked during client startup.
 */
public final class DefaultClientLoopHostFactory {

  private DefaultClientLoopHostFactory() {}

  /**
   * Installs the default client loop host if no host is configured yet.
   *
   * <p>Call this during client startup before {@code Game.run()}.
   */
  public static void installDefaultLoopHost() {
    if (Platform.loopHost() == null) {
      Platform.loopHost(defaultLoopHost());
    }
  }

  /**
   * Creates a client loop host configured with the default client installers.
   *
   * @return configured client loop host
   */
  public static ClientLoopHost defaultLoopHost() {
    return new ClientLoopHost(defaultInstallers());
  }

  /**
   * Creates the default client installer list.
   *
   * @return default client installers
   */
  public static List<ClientLoopHostInstaller> defaultInstallers() {
    return List.of(new ClientPresentationInstaller(), new DebugClientInstaller());
  }
}
