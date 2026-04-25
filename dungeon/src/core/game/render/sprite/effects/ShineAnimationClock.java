package core.game.render.sprite.effects;

/**
 * A helper class used to track the timing of an animation for a shine effect.
 *
 * <p>The {@code ShineAnimationClock} is responsible for calculating the elapsed
 * time, expressed in seconds, since the start of an animation. The animation
 * start time is initialized upon the first invocation of the {@code elapsedSeconds} method.
 *
 * <p>This class ensures that the time calculations are consistent and can be
 * reused across different animation-related components.
 *
 * <p>Instances of this class are immutable after initialization of the
 * animation start time through the first method call.
 */
final class ShineAnimationClock {

  private long animationStartMs = -1L;

  double elapsedSeconds(final long nowMs) {
    if (animationStartMs < 0L) {
      animationStartMs = nowMs;
    }

    return Math.max(0.0, (nowMs - animationStartMs) / 1000.0);
  }
}
