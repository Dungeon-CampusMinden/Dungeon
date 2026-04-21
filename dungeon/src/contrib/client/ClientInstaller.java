package contrib.client;

import core.game.loop.ClientLoopHost;
import core.game.loop.ClientLoopHostInstaller;
import core.platform.Platform;
import java.util.List;

/**
 * Provides utilities for setting up the client loop host and configuring default installers
 * during client initialization.
 *
 * <p>This class is designed to simplify the client setup process and ensure the appropriate
 * components are installed for the client game loop to function correctly.
 *
 * <p>All methods in this class are static and intended to be invoked during client startup.
 */
public final class ClientInstaller {

  private ClientInstaller() {}

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
    return List.of(new UiClientInstaller(), new DebugClientInstaller());
  }
}
