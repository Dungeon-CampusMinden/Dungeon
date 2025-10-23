package core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import core.network.handler.LocalNetworkHandler;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the LocalNetworkHandler class. This test verifies that the connection listener is
 * correctly called during the start and shutdown lifecycle of the LocalNetworkHandler.
 */
public class LocalNetworkHandlerTest {

  /**
   * Tests that the connection listener's `onConnected` method is called when the handler starts,
   * and the `onDisconnected` method is called when the handler shuts down.
   *
   * @throws Exception if any unexpected error occurs during the test execution.
   */
  @Test
  public void connectionListener_isCalled_onStartAndShutdown() throws Exception {
    LocalNetworkHandler handler = new LocalNetworkHandler();
    AtomicInteger connected = new AtomicInteger();
    AtomicInteger disconnected = new AtomicInteger();
    final String[] lastReason = new String[1];

    handler.addConnectionListener(
        new ConnectionListener() {
          @Override
          public void onConnected() {
            connected.incrementAndGet();
          }

          @Override
          public void onDisconnected(String reason) {
            lastReason[0] = reason;
            disconnected.incrementAndGet();
          }
        });

    handler.initialize(false, "localhost", 0, "user");
    handler.start();
    assertEquals(1, connected.get());

    String disconnectReason = "Test disconnect";
    handler.shutdown(disconnectReason);
    assertEquals(1, disconnected.get());
    assertEquals(disconnectReason, lastReason[0]);
  }
}
