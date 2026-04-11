package core.platform.litiengine.render.effects;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * CPU-side LITIENGINE replacement for the old sprite-local shine shader.
 *
 * <p>Unlike hue remap or color grading, shine is rendered as a separate overlay layer.
 * Therefore {@link #apply(BufferedImage, long)} keeps the input sprite unchanged, while
 * {@link #createOverlay(BufferedImage, long)} generates the animated highlight image.
 *
 * <p>This keeps the old core semantics:
 *
 * <ul>
 *   <li>slice count
 *   <li>gap size between slices
 *   <li>rotation speed
 *   <li>shine color
 * </ul>
 *
 * <p>The former libGDX shader also exposed padding. That part is intentionally still not ported in
 * this small commit, because the current LITIENGINE render path still draws inside the sprite
 * bounds.
 */
public final class LitiengineShineEffect implements LitiengineSpriteEffect {

  private static final float TWO_PI = (float) (Math.PI * 2.0);

  private int sliceCount = 4;
  private float gapSize = 0.2f;
  private float rotationSpeed = 0.2f;
  private Color shineColor = new Color(255, 255, 128, 255);
  private boolean enabled = true;

  /** Creates a shine effect with legacy-like default parameters. */
  public LitiengineShineEffect() {}

  /**
   * Creates a shine effect with explicit parameters.
   *
   * @param sliceCount number of light slices
   * @param gapSize gap size between slices in {@code [0, 1]}
   * @param rotationSpeed rotations per second
   * @param shineColor color of the shine overlay
   */
  public LitiengineShineEffect(
    int sliceCount, float gapSize, float rotationSpeed, Color shineColor) {
    sliceCount(sliceCount);
    gapSize(gapSize);
    rotationSpeed(rotationSpeed);
    shineColor(shineColor);
  }

  public int sliceCount() {
    return sliceCount;
  }

  public LitiengineShineEffect sliceCount(int sliceCount) {
    this.sliceCount = Math.max(1, sliceCount);
    return this;
  }

  public float gapSize() {
    return gapSize;
  }

  public LitiengineShineEffect gapSize(float gapSize) {
    this.gapSize = clamp01(gapSize);
    return this;
  }

  public float rotationSpeed() {
    return rotationSpeed;
  }

  public LitiengineShineEffect rotationSpeed(float rotationSpeed) {
    this.rotationSpeed = rotationSpeed;
    return this;
  }

  public Color shineColor() {
    return shineColor;
  }

  public LitiengineShineEffect shineColor(Color shineColor) {
    if (shineColor == null) {
      throw new IllegalArgumentException("shineColor must not be null");
    }
    this.shineColor = shineColor;
    return this;
  }

  public LitiengineShineEffect enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  /**
   * Shine is no longer applied as a sprite-wide pixel transformation.
   *
   * <p>The actual animated effect is rendered as a separate overlay image at draw time.
   */
  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    return input;
  }

  /**
   * Creates the animated shine overlay for the given sprite.
   *
   * @param source already prepared sprite image (after tint / other pixel effects)
   * @param nowMs current timestamp
   * @return transparent overlay image containing only the shine bands
   */
  public BufferedImage createOverlay(BufferedImage source, long nowMs) {
    if (source == null || !enabled) {
      return null;
    }

    return renderOverlay(source, nowMs / 1000.0f);
  }

  private BufferedImage renderOverlay(BufferedImage source, float nowSeconds) {
    BufferedImage output =
      new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

    int width = source.getWidth();
    int height = source.getHeight();

    float cx = (width - 1) / 2.0f;
    float cy = (height - 1) / 2.0f;

    float baseAngle = normalizePhase(nowSeconds * rotationSpeed) * TWO_PI;
    float cos = (float) Math.cos(baseAngle);
    float sin = (float) Math.sin(baseAngle);

    float maxProjection = Math.max(1.0f, Math.abs(cx * cos) + Math.abs(cy * sin));
    float visibleFraction = Math.max(0.06f, 1.0f - gapSize);

    float sweepPhase = normalizePhase(nowSeconds * rotationSpeed * 1.8f);
    float pulse =
      0.80f
        + 0.20f
        * (0.5f + 0.5f * (float) Math.sin(nowSeconds * rotationSpeed * TWO_PI));

    float shineAlpha = shineColor.getAlpha() / 255.0f;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int sourceArgb = source.getRGB(x, y);
        int sourceAlpha = (sourceArgb >>> 24) & 0xFF;

        if (sourceAlpha == 0) {
          output.setRGB(x, y, 0);
          continue;
        }

        float relX = x - cx;
        float relY = y - cy;

        float projected = (relX * cos + relY * sin) / maxProjection;
        float u = projected * 0.5f + 0.5f;

        float localSlice = fract(u * sliceCount - sweepPhase);
        float bandIntensity = shineBandIntensity(localSlice, visibleFraction);

        if (bandIntensity <= 0.0f) {
          output.setRGB(x, y, 0);
          continue;
        }

        float radialDistance = (float) Math.hypot(relX, relY) / Math.max(1.0f, Math.max(cx, cy));
        float radialFade = 1.0f - 0.30f * clamp01(radialDistance);

        float overlayStrength = clamp01(bandIntensity * radialFade * pulse);
        int overlayAlpha =
          toChannel((sourceAlpha / 255.0f) * shineAlpha * overlayStrength);

        if (overlayAlpha <= 0) {
          output.setRGB(x, y, 0);
          continue;
        }

        int overlayArgb =
          (overlayAlpha << 24)
            | (shineColor.getRed() << 16)
            | (shineColor.getGreen() << 8)
            | shineColor.getBlue();

        output.setRGB(x, y, overlayArgb);
      }
    }

    return output;
  }

  private static float shineBandIntensity(float localSlice, float visibleFraction) {
    if (localSlice >= visibleFraction) {
      return 0.0f;
    }

    float normalized = localSlice / visibleFraction;
    float centerDistance = Math.abs(normalized - 0.5f) * 2.0f;
    float base = 1.0f - centerDistance;

    return clamp01(base * base * (3.0f - 2.0f * base));
  }

  private static int toChannel(float value) {
    return Math.clamp(Math.round(clamp01(value) * 255.0f), 0, 255);
  }

  private static float normalizePhase(float value) {
    float normalized = value % 1.0f;
    return normalized < 0.0f ? normalized + 1.0f : normalized;
  }

  private static float fract(float value) {
    return value - (float) Math.floor(value);
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0.0f, 1.0f);
  }
}
