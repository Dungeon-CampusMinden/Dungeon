package engine.network.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.game.PreRunConfiguration;
import engine.network.handler.NettyNetworkHandler;
import engine.network.messages.s2c.SoundPlayMessage;
import engine.network.messages.s2c.SoundStopMessage;
import engine.sound.SoundSpec;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/** Tests for {@link SoundTracker}. */
class SoundTrackerTest {
  private static final short CLIENT = 1;

  private NettyNetworkHandler network;

  @BeforeEach
  void setUp() {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(true);
    network = Mockito.mock(NettyNetworkHandler.class);
    ServerRuntime runtime = Mockito.mock(ServerRuntime.class);
    ServerTransport transport = Mockito.mock(ServerTransport.class);
    Mockito.when(network.isServer()).thenReturn(true);
    Mockito.when(network.serverRuntime()).thenReturn(Optional.of(runtime));
    Mockito.when(runtime.transport()).thenReturn(Optional.of(transport));
    Mockito.when(transport.clientIdToSessionMap())
        .thenReturn(Map.of(CLIENT, Mockito.mock(Session.class)));
    MockNetworkHandler.useNetworkHandler(network);
  }

  @AfterEach
  void tearDown() {
    Game.removeAllEntities();
    SoundTracker.instance().clear();
    PreRunConfiguration.multiplayerEnabled(false);
    MockNetworkHandler.useLocalNetworkHandler();
  }

  /** Removing an entity stops its loops for good and lets its other sounds finish. */
  @Test
  void removingEntityStopsItsLoops() {
    Entity emitter = new Entity("emitter");
    Game.add(emitter);
    long loop = Game.audio().playOnEntity(emitter, SoundSpec.builder("loop").looping(true));
    long once = Game.audio().playOnEntity(emitter, SoundSpec.builder("once"));
    Mockito.clearInvocations(network);

    Game.remove(emitter);
    SoundTracker.instance().resyncSoundsToClient(CLIENT);

    Mockito.verify(network).send(CLIENT, new SoundStopMessage(loop), true);
    Mockito.verify(network, Mockito.never())
        .send(Mockito.anyShort(), Mockito.any(SoundPlayMessage.class), Mockito.anyBoolean());
    assertFalse(SoundTracker.instance().isTracked(loop));
    assertTrue(SoundTracker.instance().isTracked(once));
  }
}
