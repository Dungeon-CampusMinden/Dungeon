package core.platform.litiengine.render.effects;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 *   <li>padding
 * </ul>
 *
 * <p>For very small sprites, the legacy repeated multi-slice pattern is hard to perceive.
 * Therefore this implementation switches to a single explicit moving sweep band for
 * small sprites, while larger sprites continue to use the repeated slice pattern.
 */
public final class LitiengineShineEffect implements LitiengineSpriteEffect {

  private static final double TWO_PI = Math.PI * 2.0;;
  private static final int SMALL_SPRITE_MAX_DIM = 48;

  private static final Map<MaskCacheKey, float[]> ALPHA_MASK_CACHE = new ConcurrentHashMap<>();

  private int padding = 20;
  private int sliceCount = 4;
  private float gapSize = 0.2f;
  private float rotationSpeed = 0.2f;
  private Color shineColor = new Color(255, 255, 128, 255);
  private boolean enabled = true;
  private long animationStartMs = -1L;

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

  public int padding() {
    return padding;
  }

  public LitiengineShineEffect padding(int padding) {
    this.padding = Math.max(0, padding);
    return this;
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
   * Shine is rendered as a separate overlay image at draw time.
   */
  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    return input;
  }

  /**
   * Creates the animated shine overlay for the given sprite.
   *
   * <p>The returned image can be larger than the input sprite because the old GDX shader also
   * supported explicit padding around the rendered effect.
   *
   * @param source already prepared sprite image (after tint / other pixel effects)
   * @param nowMs current timestamp
   * @return transparent overlay image containing only the shine bands
   */
  public BufferedImage createOverlay(BufferedImage source, long nowMs) {
    if (source == null || !enabled) {
      return null;
    }

    if (animationStartMs < 0L) {
      animationStartMs = nowMs;
    }

    double elapsedSeconds = Math.max(0.0, (nowMs - animationStartMs) / 1000.0);
    return renderOverlay(source, elapsedSeconds);
  }

  private BufferedImage renderOverlay(BufferedImage source, double nowSeconds) {
    int paddedWidth = source.getWidth() + 2 * padding;
    int paddedHeight = source.getHeight() + 2 * padding;

    float[] alphaMask = expandedAlphaMask(source, padding);

    if (isSmallSprite(source)) {
      return renderSingleSweepOverlay(
        paddedWidth, paddedHeight, alphaMask, nowSeconds);
    }

    return renderRepeatedSlicesOverlay(
      paddedWidth, paddedHeight, alphaMask, nowSeconds);
  }

  private boolean isSmallSprite(BufferedImage source) {
    return Math.max(source.getWidth(), source.getHeight()) <= SMALL_SPRITE_MAX_DIM;
  }

  private BufferedImage renderSingleSweepOverlay(
    int paddedWidth, int paddedHeight, float[] alphaMask, double nowSeconds) {
    BufferedImage output =
      new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_INT_ARGB);

    float cx = (paddedWidth - 1) / 2.0f;
    float cy = (paddedHeight - 1) / 2.0f;

    // For small sprites we deliberately use a fixed diagonal sweep direction.
    // This is visually much easier to perceive than a subtle rotating projection model.
    float dirX = 1.0f;
    float dirY = -0.65f;

    float dirLength = (float) Math.hypot(dirX, dirY);
    dirX /= dirLength;
    dirY /= dirLength;

    float maxProjection =
      Math.max(1.0f, Math.abs(cx * dirX) + Math.abs(cy * dirY));

    // Very wide and explicit band for tiny sprites.
    float bandWidth = clamp(0.42f - gapSize * 0.18f, 0.22f, 0.42f);
    float bandHalfWidth = bandWidth * 0.5f;

    double baseAngle = normalizePhase(nowSeconds * rotationSpeed) * TWO_PI;
    float cos = (float) Math.cos(baseAngle);
    float sin = (float) Math.sin(baseAngle);

    float phase = (float) normalizePhase(nowSeconds * rotationSpeed);
    float sweepCenter = -bandHalfWidth + phase * (1.0f + bandWidth);

    float shineAlpha = shineColor.getAlpha() / 255.0f;

    for (int y = 0; y < paddedHeight; y++) {
      for (int x = 0; x < paddedWidth; x++) {
        float maskAlpha = alphaMask[y * paddedWidth + x];
        if (maskAlpha <= 0.0f) {
          output.setRGB(x, y, 0);
          continue;
        }

        float relX = x - cx;
        float relY = y - cy;

        float projected = (relX * dirX + relY * dirY) / maxProjection;
        float u = projected * 0.5f + 0.5f;

        float distance = Math.abs(u - sweepCenter);
        if (distance > bandHalfWidth) {
          output.setRGB(x, y, 0);
          continue;
        }

        float normalized = 1.0f - (distance / Math.max(0.0001f, bandHalfWidth));

        // Strong explicit band with soft center falloff.
        float bandIntensity = smoothPulse(normalized);

        float radialDistance =
          (float) Math.hypot(relX, relY) / Math.max(1.0f, Math.max(cx, cy));
        float radialFade = 1.0f - 0.15f * clamp01(radialDistance);

        float overlayStrength = clamp01(maskAlpha * bandIntensity * radialFade);
        int overlayAlpha = toChannel(shineAlpha * overlayStrength);

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

  private BufferedImage renderRepeatedSlicesOverlay(
    int paddedWidth, int paddedHeight, float[] alphaMask, double nowSeconds) {
    BufferedImage output =
      new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_INT_ARGB);

    float cx = (paddedWidth - 1) / 2.0f;
    float cy = (paddedHeight - 1) / 2.0f;

    double baseAngle = normalizePhase(nowSeconds * rotationSpeed) * TWO_PI;
    float cos = (float) Math.cos(baseAngle);
    float sin = (float) Math.sin(baseAngle);

    float maxProjection = Math.max(1.0f, Math.abs(cx * cos) + Math.abs(cy * sin));
    float visibleFraction = Math.max(0.06f, 1.0f - gapSize);

    float sweepPhase = (float) normalizePhase(nowSeconds * rotationSpeed * 1.8);
    float pulse =
      0.80f
        + 0.20f
        * (0.5f
        + 0.5f
        * (float) Math.sin(nowSeconds * rotationSpeed * TWO_PI));

    float shineAlpha = shineColor.getAlpha() / 255.0f;

    for (int y = 0; y < paddedHeight; y++) {
      for (int x = 0; x < paddedWidth; x++) {
        float maskAlpha = alphaMask[y * paddedWidth + x];
        if (maskAlpha <= 0.0f) {
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

        float radialDistance =
          (float) Math.hypot(relX, relY) / Math.max(1.0f, Math.max(cx, cy));
        float radialFade = 1.0f - 0.30f * clamp01(radialDistance);

        float overlayStrength = clamp01(maskAlpha * bandIntensity * radialFade * pulse);
        int overlayAlpha = toChannel(shineAlpha * overlayStrength);

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

  private static float[] expandedAlphaMask(BufferedImage source, int padding) {
    MaskCacheKey key =
      new MaskCacheKey(
        System.identityHashCode(source), source.getWidth(), source.getHeight(), padding);

    return ALPHA_MASK_CACHE.computeIfAbsent(
      key, ignored -> buildExpandedAlphaMask(source, padding));
  }

  private static float[] buildExpandedAlphaMask(BufferedImage source, int padding) {
    int paddedWidth = source.getWidth() + 2 * padding;
    int paddedHeight = source.getHeight() + 2 * padding;
    float[] mask = new float[paddedWidth * paddedHeight];

    int radius = Math.max(0, padding);
    int radiusSq = radius * radius;

    for (int y = 0; y < paddedHeight; y++) {
      for (int x = 0; x < paddedWidth; x++) {
        int sourceX = x - padding;
        int sourceY = y - padding;

        float directAlpha = alphaAt(source, sourceX, sourceY);
        if (directAlpha > 0.0f) {
          mask[y * paddedWidth + x] = directAlpha;
          continue;
        }

        if (radius <= 0) {
          mask[y * paddedWidth + x] = 0.0f;
          continue;
        }

        int bestDistSq = Integer.MAX_VALUE;
        float bestAlpha = 0.0f;

        for (int dy = -radius; dy <= radius; dy++) {
          for (int dx = -radius; dx <= radius; dx++) {
            int distSq = dx * dx + dy * dy;
            if (distSq > radiusSq) {
              continue;
            }

            float candidateAlpha = alphaAt(source, sourceX + dx, sourceY + dy);
            if (candidateAlpha <= 0.0f) {
              continue;
            }

            if (distSq < bestDistSq) {
              bestDistSq = distSq;
              bestAlpha = candidateAlpha;
            }
          }
        }

        if (bestDistSq == Integer.MAX_VALUE) {
          mask[y * paddedWidth + x] = 0.0f;
          continue;
        }

        float distance = (float) Math.sqrt(bestDistSq);
        float feather = 1.0f - distance / Math.max(1.0f, radius);
        mask[y * paddedWidth + x] = clamp01(bestAlpha * feather * feather);
      }
    }

    return mask;
  }

  private static float alphaAt(BufferedImage image, int x, int y) {
    if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
      return 0.0f;
    }
    return ((image.getRGB(x, y) >>> 24) & 0xFF) / 255.0f;
  }

  private static float shineBandIntensity(float localSlice, float visibleFraction) {
    if (localSlice >= visibleFraction) {
      return 0.0f;
    }

    float normalized = localSlice / visibleFraction;
    return smoothPulse(normalized);
  }

  private static float smoothPulse(float value) {
    float centerDistance = Math.abs(value - 0.5f) * 2.0f;
    float base = 1.0f - centerDistance;
    return clamp01(base * base * (3.0f - 2.0f * base));
  }

  private static int toChannel(float value) {
    return Math.clamp(Math.round(clamp01(value) * 255.0f), 0, 255);
  }

  private static double normalizePhase(double value) {
    double normalized = value % 1.0;
    return normalized < 0.0 ? normalized + 1.0 : normalized;
  }

  private static float fract(float value) {
    return value - (float) Math.floor(value);
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0.0f, 1.0f);
  }

  private static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

  private record MaskCacheKey(int identityHash, int width, int height, int padding) {}
}
