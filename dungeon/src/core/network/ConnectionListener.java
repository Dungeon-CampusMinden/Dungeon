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

  /** Called when the handler disconnects. If orderly, {@code reason} is null. */
  void onDisconnected(Throwable reason);
}


