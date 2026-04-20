package contrib.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import core.game.loop.ClientLoopHostInstaller;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

/** Tests discovery of contrib client loop installers. */
public class ContribClientInstallerServiceTest {

  /** ServiceLoader discovers all default contrib client loop installers. */
  @Test
  public void serviceLoaderDiscoversContribClientInstallers() {
    List<Class<? extends ClientLoopHostInstaller>> installerTypes =
        ServiceLoader.load(ClientLoopHostInstaller.class).stream()
            .map(ServiceLoader.Provider::type)
            .toList();

    assertTrue(installerTypes.contains(UiClientInstaller.class));
    assertTrue(installerTypes.contains(GameplayClientInstaller.class));
    assertTrue(installerTypes.contains(DebugClientInstaller.class));
  }
}
