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

  /**
   * Returns whether UDP is currently considered healthy.
   *
   * @return true if UDP is ready to be used, false otherwise
   */
  synchronized boolean udpReady() {
    return udpReady;
  }

  /**
   * Returns whether the state is currently in retry mode.
   *
   * @return true if maintenance should use retry scheduling, false for keepalive scheduling
   */
  synchronized boolean retryMode() {
    return mode == Mode.RETRY;
  }

  /**
   * Returns the timestamp of the last successful UDP registration acknowledgement.
   *
   * @return the last acknowledgement time in system milliseconds
   */
  synchronized long lastRegisterAckTimeMs() {
    return lastRegisterAckTimeMs;
  }

  /**
   * Switches the state into retry mode.
   *
   * @return true if the mode or readiness changed, false otherwise
   */
  synchronized boolean enterRetryMode() {
    boolean changed = udpReady || mode != Mode.RETRY;
    udpReady = false;
    mode = Mode.RETRY;
    nextRetryDelayMs = UDP_RETRY_INITIAL_DELAY_MS;
    return changed;
  }

  /**
   * Marks UDP as recovered after a successful registration acknowledgement.
   *
   * @param now timestamp of the acknowledgement in system milliseconds
   * @return true if the state transitioned into healthy keepalive mode, false otherwise
   */
  synchronized boolean markRecovered(long now) {
    boolean changed = !udpReady || mode != Mode.KEEPALIVE;
    udpReady = true;
    mode = Mode.KEEPALIVE;
    lastRegisterAckTimeMs = now;
    nextRetryDelayMs = UDP_RETRY_INITIAL_DELAY_MS;
    return changed;
  }

  /**
   * Records a failed registration acknowledgement while staying in retry mode.
   *
   * <p>The current backoff progression is preserved.
   */
  synchronized void markRetryAckFailure() {
    udpReady = false;
    mode = Mode.RETRY;
  }

  /**
   * Returns whether the last successful UDP acknowledgement is older than the stale threshold.
   *
   * @param now current system time in milliseconds
   * @return true if UDP should be considered stale, false otherwise
   */
  synchronized boolean stale(long now) {
    return udpReady
        && lastRegisterAckTimeMs > 0
        && now - lastRegisterAckTimeMs > UDP_STALE_AFTER_MS;
  }

  /**
   * Returns the delay before the next maintenance cycle should run.
   *
   * @return the next delay in milliseconds
   */
  synchronized long nextDelayMs() {
    return mode == Mode.KEEPALIVE ? UDP_KEEPALIVE_INTERVAL_MS : nextRetryDelayMs;
  }

  /**
   * Advances the retry backoff after a maintenance attempt.
   *
   * <p>No change is applied while the state is in keepalive mode.
   */
  synchronized void afterMaintenanceAttempt() {
    if (mode != Mode.RETRY) {
      return;
    }
    long nextDelay = nextRetryDelayMs * UDP_RETRY_MULTIPLIER;
    nextRetryDelayMs = Math.min(UDP_RETRY_MAX_DELAY_MS, nextDelay);
  }
}
