package core.network.server;

import core.game.PreRunConfiguration;
import core.network.MessageDispatcher;
import core.network.messages.NetworkMessage;
import core.utils.logging.DungeonLogger;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the lifecycle and operations of an authoritative game server.
 *
 * <p>This class handles starting and stopping the server, broadcasting messages to clients, sending
 * messages to specific clients, and polling incoming messages for dispatching.
 *
 * <p>The server uses a {@link ServerTransport} for network communication and an {@link
 * AuthoritativeServerLoop} to manage game state updates.
 *
 * <p>Usage:
 *
 * <pre>
 *   int port = 7777;
 *   ServerRuntime server = new ServerRuntime(port);
 *   server.start();
 *   // ... server operations ...
 *   server.stop();
 * </pre>
 *
 * @see AuthoritativeServerLoop AuthoritativeServerLoop for the main server loop handling game state
 *     updates.
 * @see ServerTransport ServerTransport for the underlying network transport layer.
 */
public final class ServerRuntime {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ServerRuntime.class);
  private static final Random RANDOM = new Random();

  /** The unique session ID for this server instance, randomly generated on startup. */
  public static final int SESSION_ID = RANDOM.nextInt();

  private final int port;

  private ServerTransport transport;
  private AuthoritativeServerLoop loop;

  /**
   * Creates a new ServerRuntime instance configured to run on the specified port.
   *
   * @param port The port number on which the server will listen for incoming connections.
   */
  public ServerRuntime(int port) {
    this.port = port;
  }

  /**
   * Starts the server, initializing the transport layer and main server loop.
   *
   * <p>If the server is already running, this method logs a warning and does nothing.
   */
  public void start() {
    if (loop != null && loop.isRunning()) {
      LOGGER.warn("Server already running on port {}, cannot start again", port);
      return;
    }

    LOGGER.info("Starting server on port {}", PreRunConfiguration.networkPort());
    this.transport = new ServerTransport();
    this.transport.start(port);
    this.loop = new AuthoritativeServerLoop(transport);
    this.loop.start();
  }

  /**
   * Stops the server, terminating the main loop and transport layer.
   *
   * <p>If the server is not running, this method does nothing.
   */
  public void stop() {
    if (loop != null) loop.stop();
    if (transport != null) transport.stop();
  }

  /**
   * Broadcasts a message to all connected clients.
   *
   * @param message The message to broadcast.
   * @param reliable Whether to send the message reliably (TCP) or unreliably (UDP).
   * @return A CompletableFuture that completes with true if (reliable) the message was acknowledged
   *     by all clients, or false if there was a failure. For unreliable messages, the Future
   *     completes immediately with true.
   */
  public CompletableFuture<Boolean> broadcastMessage(NetworkMessage message, boolean reliable) {
    if (loop != null) {
      return this.transport.broadcast(message, reliable);
    } else {
      LOGGER.warn("Server not initialized, cannot broadcast message");
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Sends a message to a specific client identified by their client ID.
   *
   * @param clientId The ID of the client to send the message to.
   * @param message The message to send.
   * @param reliable Whether to send the message reliably (TCP) or unreliably (UDP).
   * @return A CompletableFuture that completes with true if (reliable) the message was acknowledged
   *     by the client, or false if there was a failure. For unreliable messages, the Future
   *     completes immediately with true.
   */
  public CompletableFuture<Boolean> sendMessage(
      short clientId, NetworkMessage message, boolean reliable) {
    if (loop != null) {
      Session session = transport.clientIdToSessionMap().get(clientId);
      if (session == null) {
        LOGGER.warn("No session found for clientId {}, cannot send message", clientId);
        return CompletableFuture.completedFuture(false);
      }
      return session.sendMessage(message, reliable);
    } else {
      LOGGER.warn("Server not initialized, cannot send message to client {}", clientId);
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Polls the inbound message queue and dispatches messages to their respective handlers.
   *
   * <p>This method should be called regularly in the game's main loop to ensure timely processing
   * of incoming messages.
   *
   * @throws IllegalStateException if the ServerTransport is not initialized.
   * @see MessageDispatcher
   */
  public void pollAndDispatch() {
    if (transport == null) {
      throw new IllegalStateException("ServerTransport is not initialized. Call `start()` first.");
    }

    transport.pollAndDispatch();
  }
}
