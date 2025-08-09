package core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class LocalNetworkHandlerTest {

  @Test
  public void connectionListener_isCalled_onStartAndShutdown() throws Exception {
    LocalNetworkHandler handler = new LocalNetworkHandler();
    AtomicInteger connected = new AtomicInteger();
    AtomicInteger disconnected = new AtomicInteger();
    final Throwable[] lastReason = new Throwable[1];

    handler.addConnectionListener(
        new ConnectionListener() {
          @Override
          public void onConnected() {
            connected.incrementAndGet();
          }

          @Override
          public void onDisconnected(Throwable reason) {
            lastReason[0] = reason;
            disconnected.incrementAndGet();
          }
        });

    handler.initialize(false, "localhost", 0);
    handler.start();
    assertEquals(1, connected.get());

    handler.shutdown();
    assertEquals(1, disconnected.get());
    assertNull(lastReason[0]);
  }
}


