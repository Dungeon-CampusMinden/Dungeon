package foundation.runtime;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Detached player-visible projection of coherent Foundation authority state.
 *
 * @param riddles riddles in deterministic definition order
 * @param timer authoritative timer state
 * @param doorOpen whether the common exit door is open
 * @param terminal immutable terminal result when present
 */
public record Projection(
    List<RiddleView> riddles,
    TimerView timer,
    boolean doorOpen,
    Optional<TerminalResult> terminal) {
  /** Creates a deeply immutable projection. */
  public Projection {
    riddles = List.copyOf(Objects.requireNonNull(riddles, "riddles"));
    Objects.requireNonNull(timer, "timer");
    terminal = Objects.requireNonNull(terminal, "terminal");
  }

  /**
   * Player-visible status for one riddle.
   *
   * @param id stable riddle identifier
   * @param status locked, active, or solved status
   * @param inputs shared input completion and non-secret configuration
   * @param releasedHints only explicitly released authored hints
   */
  public record RiddleView(
      String id, ProgressStatus status, List<InputView> inputs, List<ReleasedHint> releasedHints) {
    /** Creates an immutable riddle view. */
    public RiddleView {
      Objects.requireNonNull(id, "id");
      Objects.requireNonNull(status, "status");
      inputs = List.copyOf(Objects.requireNonNull(inputs, "inputs"));
      releasedHints = List.copyOf(Objects.requireNonNull(releasedHints, "releasedHints"));
    }
  }

  /**
   * Persistent player-visible state of one riddle input.
   *
   * @param id stable input identifier
   * @param satisfied whether authority accepted the input
   * @param visibleDigitCount permitted numeric answer length
   */
  public record InputView(String id, boolean satisfied, Optional<Integer> visibleDigitCount) {
    /** Creates an immutable input view. */
    public InputView {
      Objects.requireNonNull(id, "id");
      visibleDigitCount = Objects.requireNonNull(visibleDigitCount, "visibleDigitCount");
      visibleDigitCount.ifPresent(
          count -> {
            if (count < 1) {
              throw new IllegalArgumentException("visibleDigitCount must be positive");
            }
          });
    }
  }

  /**
   * Public shared timer state.
   *
   * @param started whether all readiness gates have ever completed
   * @param state waiting, running, or terminal state
   * @param elapsed authoritative elapsed time
   * @param remaining nonnegative time to the authored limit
   * @param overtime whether a soft timer reached its limit
   */
  public record TimerView(
      boolean started, TimerState state, Duration elapsed, Duration remaining, boolean overtime) {
    /** Creates an immutable timer view. */
    public TimerView {
      Objects.requireNonNull(state, "state");
      Objects.requireNonNull(elapsed, "elapsed");
      Objects.requireNonNull(remaining, "remaining");
      if (elapsed.isNegative() || remaining.isNegative()) {
        throw new IllegalArgumentException("timer durations must be nonnegative");
      }
      if (state == TimerState.WAITING_FOR_READY && started) {
        throw new IllegalArgumentException("a waiting timer cannot be started");
      }
      if (state == TimerState.RUNNING && !started) {
        throw new IllegalArgumentException("a running timer must be started");
      }
    }
  }

  /** Closed public state of the authoritative shared timer. */
  public enum TimerState {
    /** The configured minimum number of players is not technically ready yet. */
    WAITING_FOR_READY,
    /** The timer has started and continues independently of later disconnects. */
    RUNNING,
    /** Authority has an immutable terminal result and no more time can advance. */
    TERMINAL
  }

  /** Riddle progression state. */
  public enum ProgressStatus {
    /** The riddle cannot yet accept progress. */
    LOCKED,
    /** The mandatory riddle accepts progress. */
    ACTIVE,
    /** The riddle has completed all inputs. */
    SOLVED
  }
}
