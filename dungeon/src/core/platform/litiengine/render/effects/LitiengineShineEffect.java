package core.platform.litiengine.render.effects;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * CPU-side LITIENGINE replacement for the old sprite-local shine shader.
 *
 * <p>The effect renders animated light slices directly into the sprite image and keeps the old
 * core shader semantics:
 *
 * <ul>
 *   <li>slice count
 *   <li>gap size between slices
 *   <li>rotation speed
 *   <li>shine color
 * </ul>
 *
 * <p>The former libGDX shader also exposed padding. That part is intentionally not ported in this
 * commit, because the current LITIENGINE sprite-effect pipeline transforms the sprite image
 * in-place and does not yet support expanded render bounds.
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

  /** @return number of shine slices */
  public int sliceCount() {
    return sliceCount;
  }

  /**
   * Sets the number of shine slices.
   *
   * @param sliceCount number of slices, clamped to at least 1
   * @return this effect for chaining
   */
  public LitiengineShineEffect sliceCount(int sliceCount) {
    this.sliceCount = Math.max(1, sliceCount);
    return this;
  }

  /** @return gap size between slices in {@code [0, 1]} */
  public float gapSize() {
    return gapSize;
  }

  /**
   * Sets the gap size between slices.
   *
   * @param gapSize gap size in {@code [0, 1]}; larger values create thinner light bands
   * @return this effect for chaining
   */
  public LitiengineShineEffect gapSize(float gapSize) {
    this.gapSize = clamp01(gapSize);
    return this;
  }

  /** @return rotation speed in rotations per second */
  public float rotationSpeed() {
    return rotationSpeed;
  }

  /**
   * Sets the rotation speed.
   *
   * @param rotationSpeed rotations per second
   * @return this effect for chaining
   */
  public LitiengineShineEffect rotationSpeed(float rotationSpeed) {
    this.rotationSpeed = rotationSpeed;
    return this;
  }

  /** @return current shine color */
  public Color shineColor() {
    return shineColor;
  }

  /**
   * Sets the shine color.
   *
   * @param shineColor shine color, must not be null
   * @return this effect for chaining
   */
  public LitiengineShineEffect shineColor(Color shineColor) {
    if (shineColor == null) {
      throw new IllegalArgumentException("shineColor must not be null");
    }
    this.shineColor = shineColor;
    return this;
  }

  /**
   * Enables or disables this effect.
   *
   * @param enabled whether the effect should be active
   * @return this effect for chaining
   */
  public LitiengineShineEffect enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    if (input == null || !enabled) {
      return input;
    }

    return render(input, nowMs / 1000.0f);
  }

  private BufferedImage render(BufferedImage source, float nowSeconds) {
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
    float pulse = 0.80f + 0.20f * (0.5f + 0.5f * (float) Math.sin(nowSeconds * rotationSpeed * TWO_PI));

    float shineRed = shineColor.getRed() / 255.0f;
    float shineGreen = shineColor.getGreen() / 255.0f;
    float shineBlue = shineColor.getBlue() / 255.0f;
    float shineAlpha = shineColor.getAlpha() / 255.0f;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = source.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;

        if (alpha == 0) {
          output.setRGB(x, y, 0);
          continue;
        }

        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;

        float relX = x - cx;
        float relY = y - cy;

        float projected = (relX * cos + relY * sin) / maxProjection;
        float u = projected * 0.5f + 0.5f;

        float localSlice = fract(u * sliceCount - sweepPhase);
        float bandIntensity = shineBandIntensity(localSlice, visibleFraction);

        if (bandIntensity <= 0.0f) {
          output.setRGB(x, y, argb);
          continue;
        }

        float radialDistance = (float) Math.hypot(relX, relY) / Math.max(1.0f, Math.max(cx, cy));
        float radialFade = 1.0f - 0.30f * clamp01(radialDistance);

        float overlay = clamp01(bandIntensity * radialFade * shineAlpha * pulse);

        float baseRed = red / 255.0f;
        float baseGreen = green / 255.0f;
        float baseBlue = blue / 255.0f;

        float outRed = applyLight(baseRed, shineRed, overlay);
        float outGreen = applyLight(baseGreen, shineGreen, overlay);
        float outBlue = applyLight(baseBlue, shineBlue, overlay);

        int outArgb =
          (alpha << 24)
            | (toChannel(outRed) << 16)
            | (toChannel(outGreen) << 8)
            | toChannel(outBlue);

        output.setRGB(x, y, outArgb);
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

  private static float applyLight(float base, float shine, float overlay) {
    return clamp01(base + (1.0f - base) * shine * overlay);
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
