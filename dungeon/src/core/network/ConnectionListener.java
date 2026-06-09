package core.network;

import core.network.messages.s2c.ConnectReject;

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

  /**
   * Called when the server rejects a connection request before the client becomes connected.
   *
   * @param reason the typed server-side rejection reason
   */
  default void onRejected(ConnectReject.Reason reason) {}

  /**
   * Called when the client has applied the initial multiplayer world bootstrap and is ready to show
   * gameplay.
   */
  default void onInitialWorldReady() {}
}
