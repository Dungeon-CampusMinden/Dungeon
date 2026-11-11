package feature.presentation;

/** Enum representing the speed of the transition animation. */
public enum TransitionSpeed {

  /** Fast transition speed (a sixth of a second to complete). */
  FAST(1 / 6f),

  /** Medium transition speed (half a second to complete). */
  MEDIUM(0.5f),

  /** Slow transition speed (one second to complete). */
  SLOW(1),

  /** Disabled transition (no animation). */
  DISABLED(0);

  /** Seconds the transition takes to complete. */
  public final float secondsToComplete;

  TransitionSpeed(final float secondsToComplete) {
    this.secondsToComplete = secondsToComplete;
  }
}
