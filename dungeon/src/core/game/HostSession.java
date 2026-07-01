package core.game;

/**
 * Tracks whether this client is hosting a local dedicated server (started via the main menu's "Host
 * Game" option) and exposes its live status.
 *
 * <p>Only the hosting client sets this; joiners never do. Used by the in-game pause menu to show
 * the server status and the addresses other players can use to connect.
 */
public final class HostSession {

  private static ServerProcess serverProcess;

  private HostSession() {}

  /**
   * Marks this client as the host of the given server process.
   *
   * @param process the hosted server process
   */
  static void hosting(ServerProcess process) {
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
    return serverProcess != null && serverProcess.isAlive();
  }
}
