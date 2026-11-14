package feature.emote;

import engine.Component;
import engine.Entity;
import engine.utils.Vector2;
import java.util.Optional;

/** Component that stores the emote to be displayed, its duration and the entity it follows. */
public class EmoteComponent implements Component {

  private static final float ANIMATION_LENGTH = 1f; // Duration for 1 cycle of wobbling up and down
  private static final float WOBBLE_DISTANCE = 0.1f;

  private final Emote emote;
  private final int duration;
  private final Entity target;
  private final Vector2 offset;
  private float elapsedTime = 0f;
  private float oldT = 0f;
  private float t = 0f;

  /**
   * Creates a new EmoteComponent with the specified emote and duration. The emote stays where it
   * was created.
   *
   * @param emote the emote to be displayed
   * @param duration the duration in milliseconds for which the emote should be displayed
   */
  public EmoteComponent(Emote emote, int duration) {
    this(emote, duration, null, Vector2.ZERO);
  }

  /**
   * Creates a new EmoteComponent for an emote that follows an entity.
   *
   * @param emote the emote to be displayed
   * @param duration the duration in milliseconds for which the emote should be displayed
   * @param target the entity the emote follows
   * @param offset offset of the emote's position from the target's center
   */
  public EmoteComponent(Emote emote, int duration, Entity target, Vector2 offset) {
    this.emote = emote;
    this.duration = duration;
    this.target = target;
    this.offset = offset;
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
   * Gets the entity the emote follows.
   *
   * @return the entity the emote follows, or an empty Optional if the emote stays in place
   */
  public Optional<Entity> target() {
    return Optional.ofNullable(target);
  }

  /**
   * Gets the offset of the emote's position from the center of the entity it follows.
   *
   * @return the offset from the target's center
   */
  public Vector2 offset() {
    return offset;
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
    return wobbleOffset(t) - wobbleOffset(oldT);
  }

  /**
   * Calculates the vertical offset for the wobbling animation in the current frame.
   *
   * @return vertical offset from the emote's resting position
   */
  public float getWobbleOffset() {
    return wobbleOffset(t);
  }

  private static float wobbleOffset(float t) {
    return (float) Math.sin(t * 2 * Math.PI) * WOBBLE_DISTANCE;
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
