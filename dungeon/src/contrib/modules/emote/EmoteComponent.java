package contrib.modules.emote;

import core.Component;

public class EmoteComponent implements Component {

  private static final float ANIMATION_LENGTH = 1f; // Duration for 1 cycle of wobbling up and down
  private static final float WOBBLE_DISTANCE = 0.1f;

  private final Emote emote;
  private final int duration;
  private float elapsedTime = 0f;
  private float oldT = 0f;
  private float t = 0f;

  public EmoteComponent(Emote emote, int duration) {
    this.emote = emote;
    this.duration = duration;
  }

  public Emote emote() {
    return emote;
  }

  public int duration() {
    return duration;
  }

  public float elapsedTime() {
    return elapsedTime;
  }

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
   * @return difference in vertical offset since last frame
   */
  public float getWobbleMoveFrame(){
    float wobbleOffset = (float) Math.sin(t * 2 * Math.PI) * WOBBLE_DISTANCE;
    float oldWobbleOffset = (float) Math.sin(oldT * 2 * Math.PI) * WOBBLE_DISTANCE;
    return wobbleOffset - oldWobbleOffset;
  }

  public boolean isDone() {
    return elapsedTime >= duration;
  }
}
