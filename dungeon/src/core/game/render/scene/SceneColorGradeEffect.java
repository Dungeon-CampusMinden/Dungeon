package core.game.render.scene;

import core.camera.CameraViewportState;
import core.game.render.effects.BaseColorGradeEffect;
import core.game.render.effects.ToggleableEffect;
import core.utils.Point;
import java.awt.image.BufferedImage;

/**
 * Applies HSV-style color grading to the fully rendered scene image.
 *
 * <p>The shared color grading, region, transition, and enabled-state behavior lives in
 * {@link BaseColorGradeEffect}. This class supplies scene-buffer to world-space coordinate
 * mapping and the scene effect API.
 */
public final class SceneColorGradeEffect
  extends BaseColorGradeEffect<SceneColorGradeEffect>
  implements SceneEffect, ToggleableEffect<SceneColorGradeEffect> {

  /** Creates a neutral scene color-grade effect that leaves the scene unchanged. */
  public SceneColorGradeEffect() {
    super();
  }

  /**
   * Creates a scene color-grade effect with the given HSV parameters.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @param saturationMultiplier saturation multiplier; negative values are clamped to 0
   * @param valueMultiplier value/brightness multiplier; negative values are clamped to 0
   */
  public SceneColorGradeEffect(
    float hue, float saturationMultiplier, float valueMultiplier) {
    super(hue, saturationMultiplier, valueMultiplier);
  }

  @Override
  protected SceneColorGradeEffect self() {
    return this;
  }

  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    if (input == null || !enabled()) {
      return input;
    }

    return applyColorGrade(
      input,
      (screenX, screenY) ->
        CameraViewportState.screenToWorld(new Point((float) screenX, (float) screenY)));
  }
}
