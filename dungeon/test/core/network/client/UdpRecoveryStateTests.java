package core.network.client;

import static core.network.config.NetworkConfig.UDP_KEEPALIVE_INTERVAL_MS;
import static core.network.config.NetworkConfig.UDP_RETRY_INITIAL_DELAY_MS;
import static core.network.config.NetworkConfig.UDP_RETRY_MAX_DELAY_MS;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link UdpRecoveryState}. */
public class UdpRecoveryStateTests {

  /** Validates that retry delay starts at the configured initial delay. */
  @Test
  public void test_retryDelayStartsAtInitialValue() {
    UdpRecoveryState state = new UdpRecoveryState();

    assertEquals(UDP_RETRY_INITIAL_DELAY_MS, state.nextDelayMs());
    assertTrue(state.retryMode());
    assertFalse(state.udpReady());
  }

  /** Validates that retry delay doubles until it reaches the configured maximum. */
  @Test
  public void test_retryDelayDoublesUntilCapped() {
    UdpRecoveryState state = new UdpRecoveryState();

    state.afterMaintenanceAttempt();
    assertEquals(UDP_RETRY_INITIAL_DELAY_MS * 2L, state.nextDelayMs());

    for (int i = 0; i < 20; i++) {
      state.afterMaintenanceAttempt();
    }

    assertEquals(UDP_RETRY_MAX_DELAY_MS, state.nextDelayMs());
  }

  /** Validates that a successful acknowledgement resets backoff and marks UDP healthy. */
  @Test
  public void test_successfulAckResetsBackoff() {
    UdpRecoveryState state = new UdpRecoveryState();
    state.afterMaintenanceAttempt();
    state.afterMaintenanceAttempt();

    assertTrue(state.markRecovered(1_000L));
    assertTrue(state.udpReady());
    assertFalse(state.retryMode());
    assertEquals(1_000L, state.lastRegisterAckTimeMs());
    assertEquals(UDP_KEEPALIVE_INTERVAL_MS, state.nextDelayMs());

    state.markRetryAckFailure();
    assertEquals(UDP_RETRY_INITIAL_DELAY_MS, state.nextDelayMs());
  }

  /** Validates that stale detection returns to retry mode and resets the retry delay. */
  @Test
  public void test_staleDetectionReturnsToRetryMode() {
    UdpRecoveryState state = new UdpRecoveryState();
    state.markRecovered(1_000L);

    assertTrue(state.stale(5_501L));
    assertTrue(state.enterRetryMode(true));
    assertTrue(state.retryMode());
    assertFalse(state.udpReady());
    assertEquals(UDP_RETRY_INITIAL_DELAY_MS, state.nextDelayMs());
  }
}
