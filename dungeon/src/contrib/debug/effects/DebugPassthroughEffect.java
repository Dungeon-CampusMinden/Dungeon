package contrib.debug.effects;

import core.camera.CameraViewportState;
import core.game.render.effects.ToggleableEffect;
import core.game.render.scene.SceneEffect;
import core.utils.Point;
import java.awt.image.BufferedImage;

/**
 * Represents a passthrough debugging effect for visualizing specific scene attributes
 * during rendering.
 *
 * <p>This effect allows enabling or disabling visual debug views, such
 * as alpha transparency and world position, which can be used to analyze rendering
 * characteristics.
 *
 * <p>The effect is toggleable and implements a no-op rendering behavior
 * when disabled or when no debug views are active.
 */
public final class DebugPassthroughEffect
  implements SceneEffect, ToggleableEffect<DebugPassthroughEffect> {

  private boolean debugPMA = false;
  private boolean debugWorldPos = false;
  private boolean enabled = true;

  /** Creates a neutral debug effect with all debug views disabled. */
  public DebugPassthroughEffect() {}

  /**
   * Indicates whether the alpha/transparency debug visualization is enabled.
   *
   * @return true if the alpha debug view is enabled, false otherwise
   */
  public boolean debugPMA() {
    return debugPMA;
  }

  /**
   * Enables or disables alpha/transparency debug visualization.
   *
   * @param debugPMA true to enable the alpha debug view
   */
  public void debugPMA(boolean debugPMA) {
    this.debugPMA = debugPMA;
  }

  /**
   * Checks whether the world-position debug visualization is enabled.
   *
   * @return true if the world-position debug view is enabled, false otherwise
   */
  public boolean debugWorldPos() {
    return debugWorldPos;
  }

  /**
   * Enables or disables world-position debug visualization.
   *
   * @param debugWorldPos true to enable the world-position debug view
   */
  public void debugWorldPos(boolean debugWorldPos) {
    this.debugWorldPos = debugWorldPos;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public DebugPassthroughEffect enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    if (input == null || !enabled || (!debugPMA && !debugWorldPos)) {
      return input;
    }

    int width = input.getWidth();
    int height = input.getHeight();
    BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = input.getRGB(x, y);

        int debugRgb;
        if (debugPMA && debugWorldPos) {
          int alphaView = alphaDebugRgb(argb);
          int worldView = worldPositionDebugRgb(x, y);
          debugRgb = mixRgb(alphaView, worldView);
        } else if (debugPMA) {
          debugRgb = alphaDebugRgb(argb);
        } else {
          debugRgb = worldPositionDebugRgb(x, y);
        }

        // Debug view should be fully visible and not inherit original scene alpha.
        output.setRGB(x, y, 0xFF000000 | debugRgb);
      }
    }

    return output;
  }

  private static int alphaDebugRgb(int argb) {
    int r = (argb >>> 24) & 0xFF;

    if (r == 0) {
      return 0x000000;
    }

    int g = r;
    int b = r;

    // Semi-transparent pixels are highlighted slightly cyan so they stand out from opaque areas.
    if (r < 255) {
      g = clamp255(g + 35);
      b = clamp255(b + 75);
    }

    return (r << 16) | (g << 8) | b;
  }

  private static int worldPositionDebugRgb(int screenX, int screenY) {
    Point world = CameraViewportState.screenToWorld(new Point((float) screenX, (float) screenY));

    int r = wrappedChannel(world.x(), 4.0f);
    int g = wrappedChannel(world.y(), 4.0f);
    int b = wrappedChannel(world.x() + world.y(), 8.0f);

    boolean gridX = nearTileBoundary(world.x());
    boolean gridY = nearTileBoundary(world.y());
    if (gridX || gridY) {
      r = clamp255(r + 70);
      g = clamp255(g + 70);
      b = clamp255(b + 70);
    }

    return (r << 16) | (g << 8) | b;
  }

  private static int wrappedChannel(float value, float period) {
    float normalized = positiveModulo(value, period) / period;
    return clamp255(Math.round(normalized * 255.0f));
  }

  private static boolean nearTileBoundary(float value) {
    float frac = positiveModulo(value, 1.0f);
    return frac <= (float) 0.08 || frac >= 1.0f - (float) 0.08;
  }

  private static float positiveModulo(float value, float modulo) {
    float result = value % modulo;
    return result < 0f ? result + modulo : result;
  }

  private static int mixRgb(int rgbA, int rgbB) {
    float wb = clamp01();
    float wa = 1.0f - wb;

    int r =
      clamp255(
        Math.round(((rgbA >>> 16) & 0xFF) * wa + ((rgbB >>> 16) & 0xFF) * wb));
    int g =
      clamp255(
        Math.round(((rgbA >>> 8) & 0xFF) * wa + ((rgbB >>> 8) & 0xFF) * wb));
    int b =
      clamp255(
        Math.round((rgbA & 0xFF) * wa + (rgbB & 0xFF) * wb));

    return (r << 16) | (g << 8) | b;
  }

  private static float clamp01() {
    return Math.clamp((float) 0.5, 0f, 1f);
  }

  private static int clamp255(int value) {
    return Math.clamp(value, 0, 255);
  }
}
