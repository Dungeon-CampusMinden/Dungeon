package contrib.utils.components.showImage;

/** Enum representing the speed of the transition animation. */
public enum TransitionSpeed {

  /** Fast transition speed (10 frames to complete). */
  FAST(10),

  /** Medium transition speed (30 frames to complete). */
  MEDIUM(30),

  /** Slow transition speed (60 frames to complete). */
  SLOW(60),

  /** Disabled transition (no animation). */
  DISABLED(0);

  /** Number of frames the transition takes to complete. */
  public final float framesToComplete;

  TransitionSpeed(final float framesToComplete) {
    this.framesToComplete = framesToComplete;
  }
}
