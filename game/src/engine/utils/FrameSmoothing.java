package engine.utils;

import com.badlogic.gdx.Gdx;
import engine.Game;

/** Smoothing toward a target that takes equally long at any frame rate. */
public final class FrameSmoothing {

  private static final float MAX_FRAME_SECONDS = 0.25f;

  private FrameSmoothing() {}

  /**
   * Converts the share of a remaining distance covered per world tick into the share for the
   * current frame.
   *
   * @param sharePerTick the share covered in {@link Game#TICK_SECONDS}, from 0 to 1
   * @return the share to cover in this frame
   */
  public static float share(float sharePerTick) {
    float delta = Gdx.graphics == null ? 0f : Gdx.graphics.getDeltaTime();
    if (delta <= 0f) return sharePerTick;
    float ticks = Math.min(delta, MAX_FRAME_SECONDS) / Game.TICK_SECONDS;
    return (float) (1 - Math.pow(1 - sharePerTick, ticks));
  }
}
