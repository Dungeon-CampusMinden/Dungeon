package core.utils.components.draw;

import core.platform.Platform;

/**
 * Utility class for setting blending modes used in rendering.
 *
 * <p>Provides static helper methods to configure blending on the current render platform,
 * including Pre-Multiplied Alpha (PMA) blending and straight alpha blending.
 */
public final class BlendUtils {

  private BlendUtils() {}

  /** Default blending used in this project (PMA Blending). */
  public static void setBlending() {
    setBlending(null);
  }

  /** Default blending used in this project (PMA Blending). */
  public static void setBlending(Object batch) {
    setPMABlending(batch);
  }

  /** Pre-Multiplied Alpha (PMA) blending. */
  public static void setPMABlending() {
    Platform.render().setPMABlending();
  }

  /** Pre-Multiplied Alpha (PMA) blending. */
  public static void setPMABlending(Object batch) {
    Platform.render().setPMABlending(batch);
  }

  /** Straight alpha blending. */
  public static void setStraightAlphaBlending() {
    Platform.render().setStraightAlphaBlending();
  }

  /** Straight alpha blending. */
  public static void setStraightAlphaBlending(Object batch) {
    Platform.render().setStraightAlphaBlending(batch);
  }
}
