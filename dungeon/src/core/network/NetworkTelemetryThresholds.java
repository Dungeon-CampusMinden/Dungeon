package core.network;

import core.network.config.NetworkConfig;
import core.network.telemetry.TelemetrySeverity;
import java.util.Set;

final class NetworkTelemetryThresholds {
  static final long SERVER_SNAPSHOT_STALE_AFTER_MS = 2_000L;
  static final float DEBUG_RTT_MAX_MS = 100f;
  static final int FULL_SNAPSHOT_SOFT_MAX_BYTES = 256 * 1024;

  private static final Set<String> BAD_FULL_SNAPSHOT_REASONS =
      Set.of(
          FullSnapshotSendReason.MISSING_BASELINE_HISTORY.name(),
          FullSnapshotSendReason.CLIENT_MISSING_BASELINE.name());

  private NetworkTelemetryThresholds() {}

  static long tickMicros() {
    return 1_000_000L / NetworkConfig.SERVER_TICK_HZ;
  }

  static long substepMicros() {
    return tickMicros() / 4L;
  }

  static long fullSnapshotApplyMicros() {
    return tickMicros();
  }

  static long clientFrameMicros() {
    return tickMicros() + tickMicros() / 2L;
  }

  static long queueBacklogMicros() {
    return Math.max(50_000L, tickMicros() * 3L);
  }

  static long gcPauseMs() {
    return Math.max(1L, tickMicros() / 1_000L);
  }

  static TelemetrySeverity badIfPositive(long value) {
    return value > 0L ? TelemetrySeverity.BAD : TelemetrySeverity.NORMAL;
  }

  static TelemetrySeverity badIfGreater(long value, long max) {
    return value >= 0L && value > max ? TelemetrySeverity.BAD : TelemetrySeverity.NORMAL;
  }

  static TelemetrySeverity badIfGreater(double value, double max) {
    return value >= 0.0 && value > max ? TelemetrySeverity.BAD : TelemetrySeverity.NORMAL;
  }

  static TelemetrySeverity badIfFalse(boolean value, boolean meaningful) {
    return meaningful && !value ? TelemetrySeverity.BAD : TelemetrySeverity.NORMAL;
  }

  static TelemetrySeverity badIfReason(String reason) {
    return BAD_FULL_SNAPSHOT_REASONS.contains(reason)
        ? TelemetrySeverity.BAD
        : TelemetrySeverity.NORMAL;
  }

  static String expectedZero() {
    return " (expected 0)";
  }

  static String expectedAtMost(String formattedLimit) {
    return " (expected <= " + formattedLimit + ")";
  }
}
