package core.platform.litiengine.systems;

import core.Game;
import core.System;
import core.level.elements.ILevel;
import core.platform.litiengine.render.LitiengineCameraState;
import core.platform.litiengine.render.LitiengineCameraViews;
import java.util.Objects;

/**
 * Resets shared LITIENGINE camera state when the active level changes.
 *
 * <p>This keeps backend-local camera state out of the engine-neutral level loading code while
 * ensuring that a newly loaded level starts without stale follow smoothing or stale screen offsets.
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
    previousLevel = currentLevel;
  }

  @Override
  public void stop() {
    // Can't be stopped.
  }
}
