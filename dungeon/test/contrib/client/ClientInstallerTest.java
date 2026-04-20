package contrib.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import core.game.loop.ClientLoopHostInstaller;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests the explicit client installer configuration. */
public class ClientInstallerTest {

  /** Default installers are exposed in the expected order. */
  @Test
  public void defaultInstallersContainsExpectedInstallers() {
    List<ClientLoopHostInstaller> installers = ClientInstaller.defaultInstallers();

    assertEquals(3, installers.size());
    assertInstanceOf(UiClientInstaller.class, installers.get(0));
    assertInstanceOf(GameplayClientInstaller.class, installers.get(1));
    assertInstanceOf(DebugClientInstaller.class, installers.get(2));
  }
}
