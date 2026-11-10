package engine.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import engine.network.handler.LocalNetworkHandler;
import engine.network.input.InputCommandRouter;
import engine.network.messages.c2s.InputMessage;
import feature.entities.HeroController;
import java.util.Optional;
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

    handler.initialize(false, "localhost", 0, "user", Optional.empty());
    handler.start();
    assertEquals(1, connected.get());

    String disconnectReason = "Test disconnect";
    handler.shutdown(disconnectReason);
    assertEquals(1, disconnected.get());
    assertEquals(disconnectReason, lastReason[0]);
  }

  /**
   * Tests that local input waits for the next world tick: a frame without a tick applies nothing,
   * and of several ticks in one frame only the first one applies it.
   *
   * @throws Exception if any unexpected error occurs during the test execution.
   */
  @Test
  public void sendInput_waitsForTheNextTick_andIsAppliedOnce() throws Exception {
    String route = "test:local_input";
    AtomicInteger applied = new AtomicInteger();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    Game.removeAllEntities();
    Entity player = new Entity("player");
    player.add(new PlayerComponent());
    Game.add(player);
    LocalNetworkHandler handler = new LocalNetworkHandler();
    handler.initialize(true, "localhost", 0, "user", Optional.empty());
    handler.start();
    InputCommandRouter.register(route, true, context -> applied.incrementAndGet());
    try {
      handler.sendInput(InputMessage.custom(route));
      assertEquals(0, applied.get());

      HeroController.drainAndApplyInputs();
      assertEquals(1, applied.get());
      HeroController.drainAndApplyInputs();
      assertEquals(1, applied.get());
    } finally {
      InputCommandRouter.unregister(route);
      handler.shutdown("Test finished");
      Game.removeAllEntities();
    }
  }
}
