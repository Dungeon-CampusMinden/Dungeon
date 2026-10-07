package rooms.systemRecovery.network;

import engine.Component;
import engine.Entity;
import feature.timer.WorldTimerComponent;
import java.util.Map;

/** Carries the shared countdown through both initial spawns and ongoing snapshots. */
public final class SystemRecoveryTimerSync {

  /** Server wall-clock second of the last countdown update; clients only use it to detect one. */
  public static final String TIMESTAMP = "systemRecovery.timer.timestamp";

  /** Authoritative remaining budget at the transmitted timestamp. */
  public static final String DURATION = "systemRecovery.timer.duration";

  private SystemRecoveryTimerSync() {}

  /**
   * Appends timer presentation state without depending on a graphical server.
   *
   * @param entity timer entity
   * @param metadata target spawn or snapshot metadata
   */
  public static void append(Entity entity, Map<String, String> metadata) {
    entity
        .fetch(WorldTimerComponent.class)
        .ifPresent(
            timer -> {
              metadata.put(TIMESTAMP, String.valueOf(timer.timestamp()));
              metadata.put(DURATION, String.valueOf(timer.duration()));
            });
  }

  /**
   * Updates the client renderer; incomplete or malformed packets leave valid state intact.
   *
   * <p>The renderer counts down from the component's timestamp with the local wall clock. Each new
   * server update is therefore anchored to the local receive time, so a skewed client clock cannot
   * shift the display. While the countdown is frozen, the server still advances its timestamp every
   * second, which re-anchors the client and keeps the display frozen.
   *
   * @param entity client-side timer entity
   * @param metadata server metadata
   */
  public static void apply(Entity entity, Map<String, String> metadata) {
    apply(entity, metadata, (int) (System.currentTimeMillis() / 1000L));
  }

  static void apply(Entity entity, Map<String, String> metadata, int localNowSeconds) {
    String timestamp = metadata.get(TIMESTAMP);
    String duration = metadata.get(DURATION);
    if (timestamp == null || duration == null) return;
    try {
      ServerTimerState received =
          new ServerTimerState(Integer.parseInt(timestamp), Integer.parseInt(duration));
      if (received.duration() < 0) return;
      // Snapshots can repeat an unchanged state; re-anchoring it would hold the display back.
      if (entity.fetch(ServerTimerState.class).filter(received::equals).isPresent()) return;
      entity.add(received);
      entity.add(new WorldTimerComponent(localNowSeconds, received.duration()));
    } catch (NumberFormatException ignored) {
      // A malformed packet must not remove the last valid countdown.
    }
  }

  /**
   * Client-only record of the last applied server update.
   *
   * @param timestamp server wall-clock second of the update
   * @param duration remaining seconds at that update
   */
  record ServerTimerState(int timestamp, int duration) implements Component {}
}
