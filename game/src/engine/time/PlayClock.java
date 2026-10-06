package engine.time;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** Server-owned active play time, independent of simulation pauses and tracking consent. */
public final class PlayClock {
  /** Clock transition recorded by tracking. */
  public enum Event {
    STARTED,
    PAUSED,
    RESUMED,
    ENDED
  }

  /** Why the clock stands still while play has started. */
  public enum PauseReason {
    PAUSE_DIALOG,
    PLAYERS_MISSING
  }

  /**
   * One clock transition.
   *
   * @param event transition kind
   * @param reason pause reason for {@link Event#PAUSED}
   * @param activeMs active play time at the transition
   */
  public record Transition(Event event, Optional<PauseReason> reason, long activeMs) {}

  private final LongSupplier monotonicMs;
  private final Map<Short, Integer> participants = new HashMap<>();
  private final Set<Short> pausedParticipants = new HashSet<>();
  private Optional<Set<Integer>> playingEntities = Optional.empty();
  private Consumer<Transition> onTransition = ignored -> {};
  private int minimumPlayers = 1;
  private long accumulatedMs;
  private long runningSinceMs;
  private boolean ready;
  private boolean started;
  private boolean running;
  private boolean stopped;
  private Optional<PauseReason> pauseReason = Optional.empty();

  /** Creates a clock on the system's monotonic time. */
  public PlayClock() {
    this(() -> java.lang.System.nanoTime() / 1_000_000L);
  }

  /**
   * Creates a clock with an injectable monotonic millisecond source.
   *
   * @param monotonicMs monotonic time in milliseconds
   */
  public PlayClock(LongSupplier monotonicMs) {
    this.monotonicMs = Objects.requireNonNull(monotonicMs, "monotonicMs");
  }

  /**
   * Resets for a new room before participants connect or a save is restored.
   *
   * @param minimumPlayers playing participants required for the clock to run
   */
  public synchronized void configure(int minimumPlayers) {
    if (minimumPlayers < 1) throw new IllegalArgumentException("minimumPlayers must be positive");
    this.minimumPlayers = minimumPlayers;
    accumulatedMs = 0;
    ready = started = running = stopped = false;
    pauseReason = Optional.empty();
    playingEntities = Optional.empty();
    participants.clear();
    pausedParticipants.clear();
  }

  /** Announces that loading and the room intro have ended. */
  public void ready() {
    mutate(
        () -> {
          ready = true;
          return reconcile();
        });
  }

  /**
   * Adds a connected participant after initial-world readiness.
   *
   * @param clientId network client ID
   * @param entityId player entity ID
   */
  public void participantJoined(short clientId, int entityId) {
    mutate(
        () -> {
          participants.put(clientId, entityId);
          return reconcile();
        });
  }

  /**
   * Removes a disconnected participant.
   *
   * @param clientId network client ID
   */
  public void participantLeft(short clientId) {
    mutate(
        () -> {
          participants.remove(clientId);
          pausedParticipants.remove(clientId);
          return reconcile();
        });
  }

  /**
   * Restricts playing participants to those who passed a room-specific intro gate.
   *
   * @param entityIds player entity IDs that finished the intro
   */
  public void playingParticipants(Set<Integer> entityIds) {
    mutate(
        () -> {
          playingEntities = Optional.of(Set.copyOf(entityIds));
          return reconcile();
        });
  }

  /**
   * Records only explicit pause screens, never task or input dialogs.
   *
   * @param clientId network client ID
   * @param paused whether the participant has the pause menu open
   */
  public void paused(short clientId, boolean paused) {
    mutate(
        () -> {
          if (!participants.containsKey(clientId)) return Optional.empty();
          if (paused) pausedParticipants.add(clientId);
          else pausedParticipants.remove(clientId);
          return reconcile();
        });
  }

  /**
   * Returns the active play time.
   *
   * @return active play time in milliseconds
   */
  public synchronized long activeMs() {
    return accumulatedMs + (running ? Math.max(0, monotonicMs.getAsLong() - runningSinceMs) : 0);
  }

  /**
   * Restores a save before play starts; elapsed real-world time is discarded.
   *
   * @param activeMs saved active play time
   */
  public synchronized void restore(long activeMs) {
    if (activeMs < 0) throw new IllegalArgumentException("activeMs must be non-negative");
    if (started) throw new IllegalStateException("Cannot restore after play started");
    accumulatedMs = activeMs;
  }

  /**
   * Applies server clock state to a client clock, which advances locally while running.
   *
   * @param activeMs server active play time
   * @param running whether the server clock runs
   */
  public synchronized void synchronize(long activeMs, boolean running) {
    if (activeMs < 0) throw new IllegalArgumentException("activeMs must be non-negative");
    accumulatedMs = activeMs;
    this.running = running;
    runningSinceMs = monotonicMs.getAsLong();
  }

  /** Freezes permanently at the final room outcome or session shutdown. */
  public void stop() {
    mutate(
        () -> {
          if (stopped) return Optional.empty();
          accumulatedMs = activeMs();
          running = false;
          stopped = true;
          pauseReason = Optional.empty();
          return transition(Event.ENDED);
        });
  }

  /**
   * Returns whether the clock currently advances.
   *
   * @return true while running
   */
  public synchronized boolean running() {
    return running;
  }

  /**
   * Returns the number of connected participants.
   *
   * @return connected participants
   */
  public synchronized int participantCount() {
    return participants.size();
  }

  /**
   * Sets the single transition listener, called outside the clock's lock.
   *
   * @param listener transition listener
   */
  public synchronized void onTransition(Consumer<Transition> listener) {
    onTransition = Objects.requireNonNull(listener, "listener");
  }

  private Optional<Transition> reconcile() {
    if (!ready || stopped) return Optional.empty();
    var playing =
        participants.entrySet().stream()
            .filter(
                entry -> playingEntities.map(ids -> ids.contains(entry.getValue())).orElse(true))
            .map(Map.Entry::getKey)
            .toList();
    Optional<PauseReason> reason =
        playing.size() < minimumPlayers
            ? Optional.of(PauseReason.PLAYERS_MISSING)
            : playing.stream().allMatch(pausedParticipants::contains)
                ? Optional.of(PauseReason.PAUSE_DIALOG)
                : Optional.empty();
    if (!started) {
      if (reason.isPresent()) return Optional.empty();
      started = running = true;
      runningSinceMs = monotonicMs.getAsLong();
      return transition(Event.STARTED);
    } else if (reason.isPresent()) {
      if (running || !reason.equals(pauseReason)) {
        accumulatedMs = activeMs();
        running = false;
        pauseReason = reason;
        return transition(Event.PAUSED);
      }
    } else if (!running) {
      pauseReason = Optional.empty();
      running = true;
      runningSinceMs = monotonicMs.getAsLong();
      return transition(Event.RESUMED);
    }
    return Optional.empty();
  }

  private Optional<Transition> transition(Event event) {
    return Optional.of(new Transition(event, pauseReason, activeMs()));
  }

  private void mutate(Supplier<Optional<Transition>> mutation) {
    Optional<Transition> transition;
    Consumer<Transition> listener;
    synchronized (this) {
      transition = mutation.get();
      listener = onTransition;
    }
    // Tracking and shutdown may read the clock while holding their own locks.
    transition.ifPresent(listener);
  }
}
