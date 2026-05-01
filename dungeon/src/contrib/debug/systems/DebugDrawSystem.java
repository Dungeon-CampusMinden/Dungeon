package contrib.debug.systems;

import contrib.debug.draw.DebugDrawRenderer;
import contrib.debug.draw.DebugDrawService;
import core.System;
import core.camera.CameraViewport;
import core.game.render.RenderContext;
import java.awt.Graphics2D;

/** ECS system responsible for draining queued debug draw calls and rendering them each frame. */
public final class DebugDrawSystem extends System {

  /** Creates a new debug draw system. */
  public DebugDrawSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    // render-only system
  }

  @Override
  public void render(float deltaSeconds) {
    Graphics2D base = RenderContext.get();
    if (base == null || !DebugDrawService.isHudVisible()) {
      DebugDrawService.clearQueuedDrawCalls();
      return;
    }

    CameraViewport.Viewport view = CameraViewport.get();
    if (view == null || view.tilePx() <= 0) {
      DebugDrawService.clearQueuedDrawCalls();
      return;
    }

    DebugDrawRenderer.render(base, view, DebugDrawService.snapshotAndClear());
  }

  @Override
  public void stop() {
    // Debug draw remains active so queued calls are drained even during gameplay pauses.
  }

  @Override
  public void run() {
    this.run = true;
  }
}
