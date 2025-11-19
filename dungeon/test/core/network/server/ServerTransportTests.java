package core.network.server;

import static org.junit.jupiter.api.Assertions.*;

import core.Game;
import core.network.messages.NetworkMessage;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testingUtils.MockNetworkHandler;

/**
 * Unit tests for {@link ServerTransport}.
 *
 * <p>Validates Netty-based server transport initialization, channel lifecycle, session management,
 * message broadcasting, and input queue handling.
 */
public class ServerTransportTests {

  private static final int TEST_PORT = 17777;
  private static int portCounter = 0;
  private static final List<ServerTransport> transports = new ArrayList<>();
  private static final ThreadLocal<ServerTransport> currentTransport = new ThreadLocal<>();

  /**
   * Generates a unique port starting from the base TEST_PORT. Each call increments the port number
   * to ensure no conflicts between concurrent or sequential tests.
   *
   * @return a unique port number for testing
   */
  private static synchronized int uniquePort() {
    return TEST_PORT + (portCounter++);
  }

  /**
   * Stops all transports in the transports list and clears the list. This can be called to perform
   * batch cleanup of all created transports across all tests.
   */
  private static synchronized void stopAllTransports() {
    for (ServerTransport transport : transports) {
      if (transport != null) {
        try {
          transport.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    transports.clear();
  }

  /**
   * Sets up the server transport for testing by initializing {@link Game} with multiplayer server
   * configuration.
   *
   * <p>Configures the game to run in server mode and initializes a fresh {@link ServerTransport}
   * instance.
   */
  @BeforeEach
  public void setup() {
    ServerTransport transport = new ServerTransport();
    transports.add(transport);
    currentTransport.set(transport);
    MockNetworkHandler.useLocalNetworkHandler();
  }

  /**
   * Cleans up server transport resources and game state after each test.
   *
   * <p>Stops the transport, resets multiplayer configuration, and exits the game to ensure
   * isolation between tests.
   */
  @AfterEach
  public void cleanup() {
    ServerTransport transport = currentTransport.get();
    if (transport != null) {
      transport.stop();
      transports.remove(transport);
      currentTransport.remove();
    }
  }

  /**
   * Validates that the transport starts successfully on the configured test port and creates both
   * TCP and UDP channels.
   */
  @Test
  public void test_transportStartsOnConfiguredPort() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    assertNotNull(transport.tcpServerChannel());
    assertNotNull(transport.udpChannel());
  }

  /** Validates that both TCP and UDP channels are active after the transport starts. */
  @Test
  public void test_transportChannelsActive() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();

    transport.start(port);
    assertTrue(transport.tcpServerChannel().isActive());
    assertTrue(transport.udpChannel().isActive());
  }

  /** Validates that both TCP and UDP channels are properly closed when the transport stops. */
  @Test
  public void test_stopClosesChannels() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    assertTrue(transport.tcpServerChannel().isActive());
    assertTrue(transport.udpChannel().isActive());

    transport.stop();
    assertFalse(transport.tcpServerChannel().isActive());
    assertFalse(transport.udpChannel().isActive());
  }

  /** Validates that the session map is empty after transport initialization. */
  @Test
  public void test_sessionsMapEmpty() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    assertTrue(transport.sessions().isEmpty());
  }

  /** Validates that the client ID to session mapping is empty after transport initialization. */
  @Test
  public void test_clientIdMappingEmpty() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    assertTrue(transport.clientIdToSessionMap().isEmpty());
  }

  /**
   * Validates that broadcasting a message to an empty session map returns a completed {@link
   * CompletableFuture}.
   */
  @Test
  public void test_broadcastEmptySessionMap() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();
    transport.start(port);
    NetworkMessage msg = Mockito.mock(NetworkMessage.class);
    CompletableFuture<Boolean> result = transport.broadcast(msg, true);
    assertNotNull(result);
    assertTrue(result.isDone());
  }

  /**
   * Validates that the transport can be started and stopped multiple times without error,
   * demonstrating idempotent lifecycle management.
   */
  @Test
  public void test_startStopIdempotent() {
    ServerTransport transport = currentTransport.get();
    int port1 = uniquePort();

    transport.start(port1);
    Channel tcpChannel1 = transport.tcpServerChannel();
    Channel udpChannel1 = transport.udpChannel();
    assertTrue(tcpChannel1.isActive());
    assertTrue(udpChannel1.isActive());

    transport.stop();
    assertFalse(tcpChannel1.isActive());
    assertFalse(udpChannel1.isActive());

    int port2 = uniquePort();
    transport.start(port2);
    Channel tcpChannel2 = transport.tcpServerChannel();
    Channel udpChannel2 = transport.udpChannel();
    assertNotEquals(tcpChannel1, tcpChannel2);
    assertNotEquals(udpChannel1, udpChannel2);
    assertTrue(tcpChannel2.isActive());
    assertTrue(udpChannel2.isActive());
  }

  /** Validates that calling start() multiple times without stop() is idempotent. */
  @Test
  public void test_doubleStartIsIdempotent() {
    ServerTransport transport = currentTransport.get();
    int port = uniquePort();

    transport.start(port);
    Channel tcpChannel1 = transport.tcpServerChannel();
    Channel udpChannel1 = transport.udpChannel();

    transport.start(port);
    Channel tcpChannel2 = transport.tcpServerChannel();
    Channel udpChannel2 = transport.udpChannel();

    assertSame(tcpChannel1, tcpChannel2);
    assertSame(udpChannel1, udpChannel2);
  }

  /** Validates that calling stop() before start() does not cause errors. */
  @Test
  public void test_stopWithoutStartIsIdempotent() {
    ServerTransport transport = currentTransport.get();
    transport.stop();
    transport.stop();
  }

  /** Validates that multiple transport instances can run concurrently on different ports. */
  @Test
  public void test_multipleTransportsCanCoexist() {
    ServerTransport transport1 = new ServerTransport();
    ServerTransport transport2 = new ServerTransport();
    transports.add(transport1);
    transports.add(transport2);

    int port1 = uniquePort();
    int port2 = uniquePort();

    transport1.start(port1);
    transport2.start(port2);

    assertTrue(transport1.tcpServerChannel().isActive());
    assertTrue(transport1.udpChannel().isActive());
    assertTrue(transport2.tcpServerChannel().isActive());
    assertTrue(transport2.udpChannel().isActive());

    transport1.stop();
    transport2.stop();

    assertFalse(transport1.tcpServerChannel().isActive());
    assertFalse(transport2.tcpServerChannel().isActive());
  }
}
