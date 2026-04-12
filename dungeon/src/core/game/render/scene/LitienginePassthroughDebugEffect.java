package core.game.render.scene;

import core.camera.LitiengineCameraState;
import core.camera.LitiengineCameraViews;
import core.utils.Point;
import java.awt.image.BufferedImage;

/**
 * First LITIENGINE replacement for the old passthrough debug shader.
 *
 * <p>The old libGDX shader exposed two debug flags:
 *
 * <ul>
 *   <li>{@code debugPMA}
 *   <li>{@code debugWorldPos}
 * </ul>
 *
 * <p>In the current Java2D/BufferedImage render path, a literal GPU-side passthrough shader no
 * longer exists. Therefore, this class re-models the same debug intent as a scene-pass image effect:
 *
 * <ul>
 *   <li>{@code debugPMA}: alpha/transparency visualization
 *   <li>{@code debugWorldPos}: world-position heatmap with tile-grid emphasis
 * </ul>
 *
 * <p>This effect is intentionally scene-pass based, because the old PassthroughShader was used in
 * the scene shader list, not as a sprite-local effect.
 */
public final class LitienginePassthroughDebugEffect
  implements LitiengineSceneEffects.ToggleableSceneEffect {

  private boolean debugPMA = false;
  private boolean debugWorldPos = false;
  private boolean enabled = true;

  /** Creates a neutral debug effect with all debug views disabled. */
  public LitienginePassthroughDebugEffect() {}

  /** @return whether alpha/transparency debug visualization is enabled */
  public boolean debugPMA() {
    return debugPMA;
  }

  /**
   * Enables or disables alpha/transparency debug visualization.
   *
   * @param debugPMA true to enable the alpha debug view
   * @return this effect for chaining
   */
  public LitienginePassthroughDebugEffect debugPMA(boolean debugPMA) {
    this.debugPMA = debugPMA;
    return this;
  }

  /** @return whether world-position debug visualization is enabled */
  public boolean debugWorldPos() {
    return debugWorldPos;
  }

  /**
   * Enables or disables world-position debug visualization.
   *
   * @param debugWorldPos true to enable the world-position debug view
   * @return this effect for chaining
   */
  public LitienginePassthroughDebugEffect debugWorldPos(boolean debugWorldPos) {
    this.debugWorldPos = debugWorldPos;
    return this;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public void enabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    if (input == null || !enabled || (!debugPMA && !debugWorldPos)) {
      return input;
    }

    int width = input.getWidth();
    int height = input.getHeight();
    BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    Point focus = LitiengineCameraState.focusPosition();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = input.getRGB(x, y);

        int debugRgb;
        if (debugPMA && debugWorldPos) {
          int alphaView = alphaDebugRgb(argb);
          int worldView = worldPositionDebugRgb(x, y, width, height, focus);
          debugRgb = mixRgb(alphaView, worldView, 0.5f);
        } else if (debugPMA) {
          debugRgb = alphaDebugRgb(argb);
        } else {
          debugRgb = worldPositionDebugRgb(x, y, width, height, focus);
        }

        // Debug view should be fully visible and not inherit original scene alpha.
        output.setRGB(x, y, 0xFF000000 | debugRgb);
      }
    }

    return output;
  }

  /**
   * Re-models the old debugPMA flag as an alpha/transparency visualization.
   *
   * <p>Opaque pixels appear bright, semi-transparent pixels are tinted cyan, and fully transparent
   * pixels appear black.
   */
  private static int alphaDebugRgb(int argb) {
    int alpha = (argb >>> 24) & 0xFF;

    if (alpha <= 0) {
      return 0x000000;
    }

    int base = alpha;
    int r = base;
    int g = base;
    int b = base;

    // Semi-transparent pixels are highlighted slightly cyan so they stand out from opaque areas.
    if (alpha < 255) {
      g = clamp255(g + 35);
      b = clamp255(b + 75);
    }

    return (r << 16) | (g << 8) | b;
  }

  /**
   * Visualizes the world position corresponding to the current screen pixel.
   *
   * <p>Red varies with world X, green with world Y, and blue with the combined diagonal position.
   * Tile boundaries are brightened to make the grid easier to read.
   */
  private static int worldPositionDebugRgb(
    int screenX, int screenY, int screenWidth, int screenHeight, Point focus) {
    Point world =
      LitiengineCameraViews.screenToWorld(
        new Point((float) screenX, (float) screenY), focus, screenWidth, screenHeight);

    int r = wrappedChannel(world.x(), 4.0f);
    int g = wrappedChannel(world.y(), 4.0f);
    int b = wrappedChannel(world.x() + world.y(), 8.0f);

    boolean gridX = nearTileBoundary(world.x(), 0.08f);
    boolean gridY = nearTileBoundary(world.y(), 0.08f);
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

  private static boolean nearTileBoundary(float value, float tolerance) {
    float frac = positiveModulo(value, 1.0f);
    return frac <= tolerance || frac >= 1.0f - tolerance;
  }

  private static float positiveModulo(float value, float modulo) {
    float result = value % modulo;
    return result < 0f ? result + modulo : result;
  }

  private static int mixRgb(int rgbA, int rgbB, float weightB) {
    float wb = clamp01(weightB);
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

  private static float clamp01(float value) {
    return Math.clamp(value, 0f, 1f);
  }

  private static int clamp255(int value) {
    return Math.clamp(value, 0, 255);
  }
}
