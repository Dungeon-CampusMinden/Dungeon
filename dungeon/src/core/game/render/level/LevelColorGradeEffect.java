package core.game.render.level;

import core.game.render.effects.AbstractColorGradeEffect;
import core.utils.Point;
import java.awt.image.BufferedImage;

/**
 * Applies HSV-style color grading to the rendered level layer only.
 *
 * <p>The shared color grading, region, transition, and enabled-state behavior lives in
 * {@link AbstractColorGradeEffect}. This class supplies level-buffer to world-space coordinate
 * mapping and the level effect API.
 */
public final class LevelColorGradeEffect
  extends AbstractColorGradeEffect<LevelColorGradeEffect>
  implements LevelEffectRegistry.ToggleableLevelEffect {

  /** Creates a neutral level color-grade effect that leaves the level layer unchanged. */
  public LevelColorGradeEffect() {
    super();
  }

  /**
   * Creates a level color-grade effect with the given HSV parameters.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @param saturationMultiplier saturation multiplier; negative values are clamped to 0
   * @param valueMultiplier value/brightness multiplier; negative values are clamped to 0
   */
  public LevelColorGradeEffect(
    float hue, float saturationMultiplier, float valueMultiplier) {
    super(hue, saturationMultiplier, valueMultiplier);
  }

  @Override
  protected LevelColorGradeEffect self() {
    return this;
  }

  @Override
  public BufferedImage apply(BufferedImage input, LevelPassContext context, long nowMs) {
    return applyColorGrade(
      input,
      (bufferX, bufferY) -> worldPointForBufferPixel(bufferX, bufferY, context));
  }

  private static Point worldPointForBufferPixel(
    int bufferX, int bufferY, LevelPassContext context) {
    int tilePx = Math.max(1, context.tilePx());

    float worldX = context.minTileX() + ((bufferX + 0.5f) / tilePx);
    float worldY = context.maxTileY() + 1.0f - ((bufferY + 0.5f) / tilePx);

    return new Point(worldX, worldY);
  }
}
