package engine.network.messages.c2s;

import engine.network.messages.NetworkMessage;
import java.util.Objects;
import java.util.Optional;

/**
 * Client-to-server: confirms that the client applied the initial world bootstrap.
 *
 * @param roomPlayedBefore client-local fact for the current room
 * @param trackingConsent this client's tracking decision, or empty for rooms without a consent flow
 */
public record InitialWorldReady(boolean roomPlayedBefore, Optional<Boolean> trackingConsent)
    implements NetworkMessage {

  /** Creates a message for a room without a client tracking-consent flow. */
  public InitialWorldReady(boolean roomPlayedBefore) {
    this(roomPlayedBefore, Optional.empty());
  }

  /** Creates a message with an optional client tracking decision. */
  public InitialWorldReady(boolean roomPlayedBefore, Boolean trackingConsent) {
    this(roomPlayedBefore, Optional.ofNullable(trackingConsent));
  }

  /** Validates the optional consent value. */
  public InitialWorldReady {
    trackingConsent = Objects.requireNonNull(trackingConsent, "trackingConsent");
  }
}
