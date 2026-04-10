package contrib.modules.levelHide;

import core.Component;
import core.utils.Time;

/**
 * Runtime state for level-hide regions.
 *
 * <p>The static region definition lives in {@link LevelHideComponent}. This component stores the
 * current reveal/hide state and the timestamp of the most recent state transition so rendering
 * backends can animate the transition without keeping backend-specific shader state.
 */
public final class LevelHideStateComponent implements Component {
  private static final long INITIAL_SETTLED_OFFSET_MS = 10_000L;

  private boolean hiding = true;
  private long transitionStartedAtMs = Time.nowMs() - INITIAL_SETTLED_OFFSET_MS;

  /**
   * Returns whether the region is currently hidden.
   *
   * @return true if hidden, false if revealed
   */
  public boolean hiding() {
    return hiding;
  }

  /**
   * Updates the hidden/revealed state.
   *
   * <p>Whenever the state changes, the transition timestamp is reset so render backends can start a
   * new transition animation from this point in time.
   *
   * @param hiding true to hide, false to reveal
   */
  public void hiding(boolean hiding) {
    if (this.hiding == hiding) {
      return;
    }
    this.hiding = hiding;
    this.transitionStartedAtMs = Time.nowMs();
  }

  /**
   * Returns the timestamp at which the current transition started.
   *
   * @return transition start timestamp in milliseconds
   */
  public long transitionStartedAtMs() {
    return transitionStartedAtMs;
  }

  /**
   * Returns the elapsed time in seconds since the current transition started.
   *
   * @return elapsed transition time in seconds
   */
  public float transitionElapsedSeconds() {
    return Math.max(0f, (Time.nowMs() - transitionStartedAtMs) / 1000f);
  }
}
