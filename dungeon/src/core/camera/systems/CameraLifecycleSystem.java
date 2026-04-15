package core.camera.systems;

import core.Game;
import core.System;
import core.camera.CameraState;
import core.camera.CameraViewportState;
import core.level.elements.ILevel;
import core.platform.Platform;
import core.utils.Point;
import java.util.Objects;

/**
 * A system that manages camera state transitions when levels change.
 *
 * <p>This system monitors level changes and resets the camera state whenever the current level
 * switches. It also seeds the camera focus position based on the platform's camera settings.
 * This system runs on the client side only.
 */
public final class CameraLifecycleSystem extends System {
  private ILevel previousLevel;

  /**
   * Creates a new camera lifecycle system.
   *
   * <p>This system operates on the client side only.
   */
  public CameraLifecycleSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    ILevel currentLevel = Game.currentLevel().orElse(null);
    if (Objects.equals(previousLevel, currentLevel)) {
      return;
    }

    CameraState.resetFocus();
    CameraViewportState.reset();
    seedCurrentFocus();
    previousLevel = currentLevel;
  }

  private void seedCurrentFocus() {
    Point seededFocus =
      Platform.camera().supportsFollowTargetResolution()
        ? Platform.camera().resolveFollowTarget()
        : Platform.camera().focusPosition();

    CameraState.seedFocus(seededFocus);
  }

  @Override
  public void stop() {
    // Can't be stopped.
  }
}
