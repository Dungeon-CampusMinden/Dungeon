package core.camera.systems;

import core.Game;
import core.System;
import core.camera.LitiengineCameraState;
import core.camera.LitiengineCameraViews;
import core.level.elements.ILevel;
import core.platform.Platform;
import core.utils.Point;
import java.util.Objects;

/**
 * Resets shared LITIENGINE camera state when the active level changes.
 *
 * <p>This keeps backend-local camera state out of the engine-neutral level loading code while
 * ensuring that a newly loaded level starts without stale follow smoothing or stale screen offsets.
 *
 * <p>After the reset, the camera is immediately seeded with the current follow target so the new
 * level does not briefly start at the origin before the renderer computes the first camera step.
 */
public final class LitiengineCameraLifecycleSystem extends System {
  private ILevel previousLevel;

  public LitiengineCameraLifecycleSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    ILevel currentLevel = Game.currentLevel().orElse(null);
    if (Objects.equals(previousLevel, currentLevel)) {
      return;
    }

    LitiengineCameraState.resetFocus();
    LitiengineCameraViews.reset();
    seedCurrentFocus();
    previousLevel = currentLevel;
  }

  private void seedCurrentFocus() {
    Point seededFocus =
      Platform.camera().supportsFollowTargetResolution()
        ? Platform.camera().resolveFollowTarget()
        : Platform.camera().focusPosition();

    LitiengineCameraState.seedFocus(seededFocus);
  }

  @Override
  public void stop() {
    // Can't be stopped.
  }
}
