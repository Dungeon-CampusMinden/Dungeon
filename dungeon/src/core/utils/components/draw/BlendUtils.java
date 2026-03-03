package core.utils.components.draw;

import core.platform.Platform;

/**
 * Engine-agnostic blending facade.
 *
 * <p>This class must NOT reference libGDX GL classes directly. If the current runtime supports
 * GDX rendering, calls are forwarded to {@code core.platform.gdx.render.GdxBlendUtils} via
 * reflection.
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
