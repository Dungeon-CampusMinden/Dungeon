package core.network;

/**
 * Listener for connection lifecycle events.
 *
 * <p>Implementations should be lightweight. Callbacks are invoked on the game loop thread, not on
 * IO/transport threads.
 */
public interface ConnectionListener {
  /** Called when the handler successfully starts and becomes connected. */
  void onConnected();

  /**
   * Called when the handler disconnects.
   *
   * @param reason the reason for disconnection, or null if orderly
   */
  void onDisconnected(String reason);
}
