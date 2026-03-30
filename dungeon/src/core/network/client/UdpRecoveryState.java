package core.network.client;

import static core.network.config.NetworkConfig.*;

/**
 * Tracks whether UDP is currently usable and when the next maintenance cycle should run.
 *
 * <p>The client uses this state to decide when to send aggressive re-registration retries, when to
 * emit keepalive registrations, and when a previously healthy UDP path has gone stale.
 */
final class UdpRecoveryState {

  private enum Mode {
    RETRY,
    KEEPALIVE
  }

  private Mode mode = Mode.RETRY;
  private boolean udpReady;
  private long lastRegisterAckTimeMs;
  private long nextRetryDelayMs = UDP_RETRY_INITIAL_DELAY_MS;

  synchronized boolean udpReady() {
    return udpReady;
  }

  synchronized boolean retryMode() {
    return mode == Mode.RETRY;
  }

  synchronized long lastRegisterAckTimeMs() {
    return lastRegisterAckTimeMs;
  }

  synchronized boolean enterRetryMode(boolean resetBackoff) {
    boolean changed = udpReady || mode != Mode.RETRY;
    udpReady = false;
    mode = Mode.RETRY;
    if (resetBackoff) {
      nextRetryDelayMs = UDP_RETRY_INITIAL_DELAY_MS;
    }
    return changed;
  }

  synchronized boolean markRecovered(long now) {
    boolean changed = !udpReady || mode != Mode.KEEPALIVE;
    udpReady = true;
    mode = Mode.KEEPALIVE;
    lastRegisterAckTimeMs = now;
    nextRetryDelayMs = UDP_RETRY_INITIAL_DELAY_MS;
    return changed;
  }

  synchronized void markRetryAckFailure() {
    udpReady = false;
    mode = Mode.RETRY;
  }

  synchronized boolean stale(long now) {
    return udpReady
        && lastRegisterAckTimeMs > 0
        && now - lastRegisterAckTimeMs > UDP_STALE_AFTER_MS;
  }

  synchronized long nextDelayMs() {
    return mode == Mode.KEEPALIVE ? UDP_KEEPALIVE_INTERVAL_MS : nextRetryDelayMs;
  }

  synchronized void afterMaintenanceAttempt() {
    if (mode != Mode.RETRY) {
      return;
    }
    long nextDelay = nextRetryDelayMs * UDP_RETRY_MULTIPLIER;
    nextRetryDelayMs = Math.min(UDP_RETRY_MAX_DELAY_MS, nextDelay);
  }
}
