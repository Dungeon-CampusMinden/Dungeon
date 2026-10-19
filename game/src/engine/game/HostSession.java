package engine.game;

import java.util.concurrent.CompletableFuture;

/**
 * Tracks whether this client is hosting a local dedicated server (started via the main menu's "Host
 * Game" option) and exposes its live status.
 *
 * <p>Only the hosting client sets this; joiners never do. Used by the in-game pause menu to show
 * the server status and the addresses other players can use to connect.
 */
public final class HostSession {

  private static volatile ServerProcess serverProcess;
  private static CompletableFuture<Void> serverShutdown = CompletableFuture.completedFuture(null);

  private HostSession() {}

  /**
   * Marks this client as the host of the given server process.
   *
   * @param process the hosted server process
   */
  static synchronized void hosting(ServerProcess process) {
    serverProcess = process;
  }

  /**
   * Whether this client is hosting a local server.
   *
   * @return {@code true} if this client started a hosted server
   */
  public static boolean isHosting() {
    return serverProcess != null;
  }

  /**
   * Whether the hosted server process is currently running.
   *
   * @return {@code true} if hosting and the server process is still alive
   */
  public static boolean isServerRunning() {
    ServerProcess hostedServer = serverProcess;
    return hostedServer != null && hostedServer.isAlive();
  }

  /** Stops the locally hosted server while the hosting client can still receive final status. */
  static void stopHosting() {
    stopHostingAsync().join();
  }

  /**
   * Stops the locally hosted server without blocking the render thread.
   *
   * @return completion of the current or already pending server shutdown
   */
  static synchronized CompletableFuture<Void> stopHostingAsync() {
    ServerProcess hostedServer = serverProcess;
    if (hostedServer == null) {
      return serverShutdown;
    }
    serverProcess = null;
    serverShutdown = CompletableFuture.runAsync(hostedServer::stop);
    return serverShutdown;
  }
}
