package core.network.server;

import core.network.messages.NetworkMessage;
import core.utils.logging.DungeonLogger;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Transport-level session bound to a TCP channel and optional UDP address.
 *
 * <p>Manages client state attachment, message sending over TCP/UDP, and session lifecycle.
 *
 * <p>This class is used by the server to represent a connected client session. Or the client may
 * use it to represent its own session with the server.
 *
 * <p>It can send messages either reliably over TCP or unreliably over UDP, depending on the
 * requirements of the message.
 */
public final class Session {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Session.class);

  private final ChannelHandlerContext tcpCtx;
  private volatile InetSocketAddress udpAddress;

  private volatile ClientState clientState;

  private final BiFunction<InetSocketAddress, NetworkMessage, CompletableFuture<Boolean>> udpSender;
  private final BiFunction<ChannelHandlerContext, NetworkMessage, CompletableFuture<Boolean>>
      tcpSender;

  /**
   * Creates a Session with TCP context and senders.
   *
   * @param tcpCtx the TCP channel context
   * @param udpSender function to send UDP messages
   * @param tcpSender function to send TCP messages
   */
  public Session(
      ChannelHandlerContext tcpCtx,
      BiFunction<InetSocketAddress, NetworkMessage, CompletableFuture<Boolean>> udpSender,
      BiFunction<ChannelHandlerContext, NetworkMessage, CompletableFuture<Boolean>> tcpSender) {
    this.tcpCtx = tcpCtx;
    this.udpSender = udpSender;
    this.tcpSender = tcpSender;
  }

  /**
   * Gets the client ID from the attached ClientState.
   *
   * @return the client ID, or 0 if no ClientState is attached
   */
  public short clientId() {
    return clientState().map(ClientState::clientId).orElse((short) 0);
  }

  /**
   * Gets the session ID from the attached ClientState.
   *
   * @return the session ID, or 0 if no ClientState is attached
   */
  public int sessionId() {
    return clientState().map(ClientState::sessionId).orElse(0);
  }

  /**
   * Gets the session token from the attached ClientState.
   *
   * @return the session token, or an empty byte array if no ClientState is attached
   */
  public byte[] sessionToken() {
    return clientState().map(ClientState::sessionToken).orElse(new byte[0]);
  }

  /**
   * Verifies the provided token against the attached ClientState's session token.
   *
   * @param token the token to verify
   * @return true if the token matches, false otherwise
   */
  public boolean verifyToken(byte[] token) {
    return clientState != null && token != null && Arrays.equals(clientState.sessionToken(), token);
  }

  /**
   * Attaches a ClientState to this session.
   *
   * @param state the ClientState to attach
   */
  public void attachClientState(ClientState state) {
    this.clientState = Objects.requireNonNull(state);
  }

  /**
   * Gets the attached ClientState.
   *
   * @return an Optional containing the ClientState if attached, or empty if not
   */
  public Optional<ClientState> clientState() {
    return Optional.ofNullable(clientState);
  }

  /**
   * Gets the TCP channel context.
   *
   * @return the TCP ChannelHandlerContext
   */
  public ChannelHandlerContext tcpCtx() {
    return tcpCtx;
  }

  /**
   * Gets the UDP address.
   *
   * @return the UDP InetSocketAddress
   */
  public InetSocketAddress udpAddress() {
    return udpAddress;
  }

  /**
   * Sets the UDP address.
   *
   * @param addr the UDP InetSocketAddress to set
   */
  public void udpAddress(InetSocketAddress addr) {
    this.udpAddress = addr;
  }

  /** Closes the TCP channel associated with this session. */
  public void close() {
    try {
      tcpCtx.close();
    } catch (Exception e) {
      LOGGER.warn("Exception while closing TCP channel for session: " + e.getMessage());
    }
  }

  /**
   * Checks if the TCP channel is closed.
   *
   * @return true if the TCP channel is closed, false otherwise
   */
  public boolean isClosed() {
    return !tcpCtx.channel().isActive();
  }

  /**
   * Sends a NetworkMessage over TCP or UDP based on the reliability requirement.
   *
   * @param msg the NetworkMessage to send
   * @param reliable true to send over TCP, false to send over UDP
   * @return a CompletableFuture that completes with true if the request was successful acknowledged
   *     (if reliable) or sent (if unreliable), false otherwise
   */
  public CompletableFuture<Boolean> sendMessage(NetworkMessage msg, boolean reliable) {
    if (reliable) {
      return sendTcpObject(msg);
    } else {
      return sendUdpObject(msg);
    }
  }

  private CompletableFuture<Boolean> sendTcpObject(NetworkMessage msg) {
    if (tcpCtx == null || tcpSender == null) {
      LOGGER.warn("TCP context or sender is null; cannot send TCP message.");
      return CompletableFuture.completedFuture(false);
    }
    return tcpSender.apply(tcpCtx, msg);
  }

  private CompletableFuture<Boolean> sendUdpObject(NetworkMessage msg) {
    if (udpSender == null || udpAddress == null) {
      LOGGER.warn("UDP sender or address is null; cannot send UDP message.");
      return CompletableFuture.completedFuture(false);
    }
    return udpSender.apply(udpAddress, msg);
  }
}
