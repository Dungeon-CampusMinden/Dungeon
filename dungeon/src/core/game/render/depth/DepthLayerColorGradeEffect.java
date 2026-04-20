package core.game.render.depth;

import core.camera.CameraState;
import core.camera.CameraViewportState;
import core.game.render.effects.AbstractColorGradeEffect;
import core.utils.Point;
import java.awt.image.BufferedImage;

/**
 * Applies HSV-style color grading to one rendered entity depth layer only.
 *
 * <p>The shared color grading, region, transition, and enabled-state behavior lives in
 * {@link AbstractColorGradeEffect}. This class supplies depth-buffer to world-space coordinate
 * mapping and the depth-layer effect API.
 */
public final class DepthLayerColorGradeEffect
  extends AbstractColorGradeEffect<DepthLayerColorGradeEffect>
  implements DepthLayerEffectRegistry.ToggleableDepthLayerEffect {

  /** Creates a neutral depth-layer color-grade effect that leaves the depth layer unchanged. */
  public DepthLayerColorGradeEffect() {
    super();
  }

  /**
   * Creates a depth-layer color-grade effect with the given HSV parameters.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @param saturationMultiplier saturation multiplier; negative values are clamped to 0
   * @param valueMultiplier value/brightness multiplier; negative values are clamped to 0
   */
  public DepthLayerColorGradeEffect(
    float hue, float saturationMultiplier, float valueMultiplier) {
    super(hue, saturationMultiplier, valueMultiplier);
  }

  @Override
  protected DepthLayerColorGradeEffect self() {
    return this;
  }

  @Override
  public BufferedImage apply(BufferedImage input, int depthLayer, long nowMs) {
    if (input == null || !enabled()) {
      return input;
    }

    Point focus = CameraState.focusPosition();
    return applyColorGrade(
      input,
      (screenX, screenY) ->
        CameraViewportState.screenToWorld(
          new Point((float) screenX, (float) screenY), focus));
  }
}
