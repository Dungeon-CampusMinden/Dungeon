package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;

/**
 * Authoritative play-clock state, sent with the world sync and whenever the clock starts or stops
 * running.
 *
 * @param activeMs active play time at sending
 * @param running whether clients should keep advancing the value locally
 */
public record PlayClockMessage(long activeMs, boolean running) implements NetworkMessage {
  public PlayClockMessage {
    if (activeMs < 0) throw new IllegalArgumentException("activeMs must be non-negative");
  }
}
