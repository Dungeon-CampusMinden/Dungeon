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
    if (!Platform.runtime().supportsGdxRendering()) return;
    invokeNoArg("setPMABlending");
  }

  /** Pre-Multiplied Alpha (PMA) blending. */
  public static void setPMABlending(Object batch) {
    if (!Platform.runtime().supportsGdxRendering()) return;
    invokeObjectArg("setPMABlending", batch);
  }

  /** Straight alpha blending. */
  public static void setStraightAlphaBlending() {
    if (!Platform.runtime().supportsGdxRendering()) return;
    invokeNoArg("setStraightAlphaBlending");
  }

  /** Straight alpha blending. */
  public static void setStraightAlphaBlending(Object batch) {
    if (!Platform.runtime().supportsGdxRendering()) return;
    invokeObjectArg("setStraightAlphaBlending", batch);
  }

  private static void invokeNoArg(String methodName) {
    try {
      Class<?> cls = Class.forName("core.platform.gdx.render.GdxBlendUtils");
      cls.getMethod(methodName).invoke(null);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("GDX blend utils not available", e);
    }
  }

  private static void invokeObjectArg(String methodName, Object arg) {
    try {
      Class<?> cls = Class.forName("core.platform.gdx.render.GdxBlendUtils");
      cls.getMethod(methodName, Object.class).invoke(null, arg);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("GDX blend utils not available", e);
    }
  }
}
