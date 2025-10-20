package testingUtils;

import static org.junit.jupiter.api.Assertions.fail;

import core.Game;
import core.network.handler.INetworkHandler;
import core.network.handler.LocalNetworkHandler;
import java.lang.reflect.Field;

/**
 * Utility class to mock the network handler for testing purposes.
 *
 * <p>This class provides methods to replace the current network handler in the {@link Game} with a
 * {@link LocalNetworkHandler} or any other {@link INetworkHandler} implementation
 */
public final class MockNetworkHandler {

  /**
   * Changes the current {@link Game#network() NetworkHandler} to a {@link LocalNetworkHandler} for
   * testing.
   */
  public static void useLocalNetworkHandler() {
    INetworkHandler networkHandler = new LocalNetworkHandler();

    Field handlerField;
    try {
      handlerField = Game.class.getDeclaredField("networkHandler");
      handlerField.setAccessible(true);
      handlerField.set(null, networkHandler);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail("Failed to set network handler via reflection: " + e.getMessage());
    }
  }

  /**
   * Changes the current {@link Game#network() NetworkHandler} to a given {@link INetworkHandler}
   * for testing.
   *
   * @param handler the INetworkHandler to set
   */
  public static void useNetworkHandler(INetworkHandler handler) {
    Field handlerField;
    try {
      handlerField = Game.class.getDeclaredField("networkHandler");
      handlerField.setAccessible(true);
      handlerField.set(null, handler);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail("Failed to set network handler via reflection: " + e.getMessage());
    }
  }
}
