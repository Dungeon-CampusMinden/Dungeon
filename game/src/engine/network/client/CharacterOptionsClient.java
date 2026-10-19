package engine.network.client;

import static engine.network.codec.NetworkCodec.deserialize;
import static engine.network.codec.NetworkCodec.serialize;
import static engine.network.config.NetworkConfig.MAX_TCP_OBJECT_SIZE;
import static engine.network.config.NetworkConfig.PROTOCOL_VERSION;

import engine.network.codec.NetworkCodec;
import engine.network.messages.NetworkMessage;
import engine.network.messages.c2s.CharacterOptionsRequest;
import engine.network.messages.s2c.CharacterOptions;
import engine.network.messages.s2c.ConnectReject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Performs the lightweight, unauthenticated main-menu character discovery exchange.
 *
 * <p>This connection is intentionally separate from the gameplay client: the server creates no
 * authenticated player identity for the options request. It uses {@link NetworkCodec} for messages
 * and the same four-byte length-prefixed TCP framing as {@link ClientNetwork}.
 */
public final class CharacterOptionsClient implements AutoCloseable {
  private static final int CONNECT_TIMEOUT_MS = 2_500;
  private static final int RESPONSE_TIMEOUT_MS = 8_000;

  private final String host;
  private final int port;
  private final String playerName;
  private final AtomicBoolean cancelled = new AtomicBoolean();
  private volatile Socket socket;
  private volatile Thread worker;

  /**
   * Creates a cancellable discovery connection.
   *
   * @param host server address
   * @param port server TCP port
   * @param playerName identity whose available characters should be queried
   */
  public CharacterOptionsClient(String host, int port, String playerName) {
    this.host = host;
    this.port = port;
    this.playerName = playerName;
  }

  /**
   * Sends one discovery request.
   *
   * @return the server's current character choices, or an exceptional result on failure
   */
  public CompletableFuture<CharacterOptions> request() {
    CompletableFuture<CharacterOptions> result = new CompletableFuture<>();
    worker =
        new Thread(
            () -> {
              try {
                result.complete(readOptions());
              } catch (Exception e) {
                if (!cancelled.get()) {
                  result.completeExceptionally(e);
                } else {
                  result.cancel(false);
                }
              } finally {
                closeSocket();
              }
            },
            "character-options-discovery");
    worker.setDaemon(true);
    worker.start();
    return result;
  }

  @Override
  public void close() {
    if (cancelled.compareAndSet(false, true)) {
      closeSocket();
      Thread currentWorker = worker;
      if (currentWorker != null) {
        currentWorker.interrupt();
      }
    }
  }

  private CharacterOptions readOptions() throws IOException {
    Socket currentSocket = new Socket();
    socket = currentSocket;
    if (cancelled.get()) {
      throw new IOException("Character discovery was cancelled.");
    }
    currentSocket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
    currentSocket.setSoTimeout(RESPONSE_TIMEOUT_MS);

    byte[] request = serialize(new CharacterOptionsRequest(PROTOCOL_VERSION, playerName));
    if (request.length > MAX_TCP_OBJECT_SIZE) {
      throw new IOException("Character discovery request is too large.");
    }
    DataOutputStream output = new DataOutputStream(currentSocket.getOutputStream());
    output.writeInt(request.length);
    output.write(request);
    output.flush();

    DataInputStream input = new DataInputStream(currentSocket.getInputStream());
    int length = input.readInt();
    if (length < 1 || length > MAX_TCP_OBJECT_SIZE) {
      throw new IOException("Server returned an invalid character discovery response.");
    }
    byte[] response = new byte[length];
    input.readFully(response);
    if (cancelled.get()) {
      throw new IOException("Character discovery was cancelled.");
    }
    ByteBuf buffer = Unpooled.wrappedBuffer(response);
    try {
      NetworkMessage message = deserialize(buffer);
      if (message instanceof CharacterOptions options) {
        return options;
      }
      if (message instanceof ConnectReject reject) {
        throw new CharacterOptionsRejectedException(ConnectReject.Reason.fromCode(reject.reason()));
      }
      throw new IOException("Server returned an unexpected character discovery response.");
    } finally {
      buffer.release();
    }
  }

  private void closeSocket() {
    Socket current = socket;
    socket = null;
    if (current != null) {
      try {
        current.close();
      } catch (IOException ignored) {
        // Closing is best-effort during cancellation and after a completed response.
      }
    }
  }

  /** An unauthenticated discovery response rejected the requested player name or protocol. */
  public static final class CharacterOptionsRejectedException extends IOException {
    private final ConnectReject.Reason reason;

    /**
     * Creates a discovery rejection.
     *
     * @param reason authoritative server rejection reason
     */
    public CharacterOptionsRejectedException(ConnectReject.Reason reason) {
      super("Character discovery rejected: " + reason);
      this.reason = reason;
    }

    /**
     * Returns the authoritative rejection reason.
     *
     * @return server rejection reason
     */
    public ConnectReject.Reason reason() {
      return reason;
    }
  }
}
