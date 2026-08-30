package engine.game;

import engine.utils.logging.DungeonLogger;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

/** Sends one application-defined terminal status from a managed server to its hosting process. */
public final class ManagedServerStatus {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ManagedServerStatus.class);
  private static final String LOCALHOST = "127.0.0.1";
  private static final int CONNECT_TIMEOUT_MILLIS = 500;

  private ManagedServerStatus() {}

  /**
   * Sends one opaque status payload to the managing process.
   *
   * @param payload application-defined status payload
   * @return whether this process is managed and the payload reached its manager
   */
  public static boolean send(final String payload) {
    Integer statusPort = Integer.getInteger(ServerProcess.STATUS_PORT_PROPERTY);
    if (!Boolean.getBoolean(ServerProcess.MANAGED_PROPERTY) || statusPort == null) {
      return false;
    }
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(LOCALHOST, statusPort), CONNECT_TIMEOUT_MILLIS);
      try (DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
        output.writeUTF(Objects.requireNonNull(payload, "payload"));
        output.flush();
      }
      return true;
    } catch (IOException | RuntimeException exception) {
      LOGGER.warn("Cannot report managed-server status to the hosting client.", exception);
      return false;
    }
  }
}
