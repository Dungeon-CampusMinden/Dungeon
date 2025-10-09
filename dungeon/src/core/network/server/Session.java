package core.network.server;

import core.network.messages.NetworkMessage;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transport-level session bound to a TCP channel and optional UDP address. Delegates actual sending
 * to injected functions provided by ServerTransport, and delegates identity (clientId/session) to
 * an attached ClientState.
 */
public final class Session {
  private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

  private final ChannelHandlerContext tcpCtx;
  private volatile InetSocketAddress udpAddress;

  private volatile ClientState clientState;

  private final BiFunction<InetSocketAddress, Object, CompletableFuture<Boolean>> udpSender;
  private final BiFunction<ChannelHandlerContext, Object, CompletableFuture<Boolean>> tcpSender;

  public Session(ChannelHandlerContext tcpCtx) {
    this(tcpCtx, null, null);
  }

  public Session(
      ChannelHandlerContext tcpCtx,
      BiFunction<InetSocketAddress, Object, CompletableFuture<Boolean>> udpSender,
      BiFunction<ChannelHandlerContext, Object, CompletableFuture<Boolean>> tcpSender) {
    this.tcpCtx = tcpCtx;
    this.udpSender = udpSender;
    this.tcpSender = tcpSender;
  }

  public short clientId() {
    return clientState().map(ClientState::clientId).orElse((short) 0);
  }

  public int sessionId() {
    return clientState().map(ClientState::sessionId).orElse(0);
  }

  public byte[] sessionToken() {
    return clientState().map(ClientState::sessionToken).orElse(new byte[0]);
  }

  public boolean verifyToken(byte[] token) {
    return clientState != null && token != null && Arrays.equals(clientState.sessionToken(), token);
  }

  // ClientState attachment
  public void attachClientState(ClientState state) {
    this.clientState = Objects.requireNonNull(state);
  }

  public Optional<ClientState> clientState() {
    return Optional.ofNullable(clientState);
  }

  // Transport info
  public ChannelHandlerContext tcpCtx() {
    return tcpCtx;
  }

  public InetSocketAddress udpAddress() {
    return udpAddress;
  }

  public void udpAddress(InetSocketAddress addr) {
    this.udpAddress = addr;
  }

  public void close() {
    try {
      tcpCtx.close();
    } catch (Exception ignored) {
    }
  }

  public boolean isClosed() {
    return !tcpCtx.channel().isActive();
  }

  public CompletableFuture<Boolean> sendMessage(NetworkMessage msg, boolean reliable) {
    if (reliable) {
      return sendTcpObject(msg);
    } else {
      return sendUdpObject(msg);
    }
  }

  private CompletableFuture<Boolean> sendTcpObject(NetworkMessage msg) {
    if (tcpCtx == null || tcpSender == null) return CompletableFuture.completedFuture(false);
    return tcpSender.apply(tcpCtx, msg);
  }

  private CompletableFuture<Boolean> sendUdpObject(NetworkMessage msg) {
    if (udpSender == null || udpAddress == null) return CompletableFuture.completedFuture(false);
    return udpSender.apply(udpAddress, msg);
  }
}
