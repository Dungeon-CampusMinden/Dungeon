package engine.network.messages.c2s;

import engine.network.messages.NetworkMessage;

/**
 * Client-to-server: confirms that the client applied the initial world bootstrap.
 *
 * @param roomPlayedBefore client-local fact for the current room
 */
public record InitialWorldReady(boolean roomPlayedBefore) implements NetworkMessage {
  /** Creates the message for an untracked room or an empty local room history. */
  public InitialWorldReady() {
    this(false);
  }
}
