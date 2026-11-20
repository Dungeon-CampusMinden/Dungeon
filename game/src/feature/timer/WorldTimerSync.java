package feature.timer;

import engine.Entity;
import engine.Game;
import java.util.Map;

/**
 * Carries the fixed values of a {@link WorldTimerComponent} through spawns and snapshots. Clients
 * compute the remaining time from their synchronized play clock, so only a start, a duration, a
 * freeze point and the direction travel.
 */
public final class WorldTimerSync {

  /** Active play time at which the countdown began. */
  public static final String STARTED_AT = "worldTimer.startedAt";

  /** Countdown length in seconds. */
  public static final String DURATION = "worldTimer.duration";

  /** Active play time at which the countdown froze, absent while it runs. */
  public static final String STOPPED_AT = "worldTimer.stoppedAt";

  /** Present for a stopwatch, absent for a countdown. */
  public static final String COUNTS_UP = "worldTimer.countsUp";

  private WorldTimerSync() {}

  /**
   * Appends the countdown state.
   *
   * @param entity timer entity
   * @param metadata target spawn or snapshot metadata
   */
  public static void append(Entity entity, Map<String, String> metadata) {
    entity
        .fetch(WorldTimerComponent.class)
        .ifPresent(timer -> {
          metadata.put(STARTED_AT, String.valueOf(timer.startedAtActiveMs()));
          metadata.put(DURATION, String.valueOf(timer.duration()));
          if (timer.stoppedAtActiveMs() != Long.MAX_VALUE) {
            metadata.put(STOPPED_AT, String.valueOf(timer.stoppedAtActiveMs()));
          }
          if (timer.countsUp()) metadata.put(COUNTS_UP, "true");
        });
  }

  /**
   * Updates the client countdown; incomplete or malformed packets leave valid state intact.
   *
   * @param entity client-side timer entity
   * @param metadata server metadata
   */
  public static void apply(Entity entity, Map<String, String> metadata) {
    String startedAt = metadata.get(STARTED_AT);
    String duration = metadata.get(DURATION);
    if (startedAt == null || duration == null) return;
    try {
      String stoppedAt = metadata.get(STOPPED_AT);
      WorldTimerComponent received =
          new WorldTimerComponent(
              Long.parseLong(startedAt),
              Integer.parseInt(duration),
              stoppedAt == null ? Long.MAX_VALUE : Long.parseLong(stoppedAt),
              metadata.containsKey(COUNTS_UP));
      if (received.duration() < 0) return;
      if (entity.fetch(WorldTimerComponent.class).filter(received::equals).isEmpty()) {
        entity.add(received);
      }
      // Games that do not set up timers themselves still draw them.
      if (!Game.systems().containsKey(WorldTimerSystem.class)) Game.add(new WorldTimerSystem());
    } catch (NumberFormatException ignored) {
      // A malformed packet must not remove the last valid countdown.
    }
  }
}
