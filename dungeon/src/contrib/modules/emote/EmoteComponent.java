package contrib.modules.emote;

import core.Component;

/** Component that stores the emote to be displayed and its duration. */
public class EmoteComponent implements Component {

  private static final float ANIMATION_LENGTH = 1f; // Duration for 1 cycle of wobbling up and down
  private static final float WOBBLE_DISTANCE = 0.1f;

  private final Emote emote;
  private final int duration;
  private float elapsedTime = 0f;
  private float oldT = 0f;
  private float t = 0f;

  /**
   * Creates a new EmoteComponent with the specified emote and duration.
   *
   * @param emote the emote to be displayed
   * @param duration the duration in milliseconds for which the emote should be displayed
   */
  public EmoteComponent(Emote emote, int duration) {
    this.emote = emote;
    this.duration = duration;
  }

  /**
   * Gets the emote to be displayed.
   *
   * @return the emote to be displayed
   */
  public Emote emote() {
    return emote;
  }

  /**
   * Gets the duration for which the emote should be displayed.
   *
   * @return the duration in milliseconds for which the emote should be displayed
   */
  public int duration() {
    return duration;
  }

  /**
   * Updates the elapsed time and the wobbling animation state. Should be called every frame with
   * the time since the last frame in seconds.
   *
   * @param deltaTime the time in seconds since the last frame
   */
  public void update(float deltaTime) {
    elapsedTime += deltaTime * 1000;

    // Update t for the wobbling animation
    oldT = t;
    t += deltaTime / ANIMATION_LENGTH;
    if (t > 1f) {
      t -= 1f; // Loop t back to 0 after completing a cycle
    }
  }

  /**
   * Calculates the vertical offset for the wobbling animation, as difference to last frame.
   *
   * @return difference in vertical offset since last frame
   */
  public float getWobbleMoveFrame() {
    float wobbleOffset = (float) Math.sin(t * 2 * Math.PI) * WOBBLE_DISTANCE;
    float oldWobbleOffset = (float) Math.sin(oldT * 2 * Math.PI) * WOBBLE_DISTANCE;
    return wobbleOffset - oldWobbleOffset;
  }

  /**
   * Checks if the emote's display duration has been exceeded.
   *
   * @return true if the emote's display duration has been exceeded, false otherwise
   */
  public boolean isDone() {
    return elapsedTime >= duration;
  }
}
