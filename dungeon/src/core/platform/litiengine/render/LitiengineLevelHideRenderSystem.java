package core.platform.litiengine.render;

import contrib.modules.levelHide.LevelHideComponent;
import contrib.modules.levelHide.LevelHideStateComponent;
import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Vector2;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * LITIENGINE render-only implementation for hidden world regions.
 *
 * <p>This system interprets the engine-neutral level-hide state and paints a dark overlay in
 * world-space using the current Graphics2D context.
 *
 * <p>The implementation is intentionally an AWT-based approximation of the former libGDX shader
 * effect: the region interior fades between hidden and revealed, and the configured transition size
 * is rendered as a soft edge band around the region.
 */
public final class LitiengineLevelHideRenderSystem extends System {
  private static final float TRANSITION_DURATION_SECONDS = 0.30f;
  private static final int MAX_FILL_ALPHA = 210;
  private static final int MAX_EDGE_ALPHA = 120;
  private static final int EDGE_STEPS = 5;

  /** Creates a new render-only system for level-hide regions. */
  public LitiengineLevelHideRenderSystem() {
    super(
      AuthoritativeSide.CLIENT,
      LevelHideComponent.class,
      LevelHideStateComponent.class,
      PositionComponent.class);
  }

  @Override
  public void execute() {
    // no-op (render-only system)
  }

  @Override
  public void render(float deltaSeconds) {
    Graphics2D g = LitiengineGraphicsContext.get();
    if (g == null || LitiengineCameraViews.activeView().isEmpty()) {
      return;
    }

    filteredEntityStream().forEach(entity -> renderRegion(g, entity));
  }

  private void renderRegion(Graphics2D g, Entity entity) {
    LevelHideComponent hideComponent = entity.fetch(LevelHideComponent.class).orElseThrow();
    LevelHideStateComponent stateComponent =
      entity.fetch(LevelHideStateComponent.class).orElseThrow();
    PositionComponent positionComponent = entity.fetch(PositionComponent.class).orElseThrow();

    float hiddenProgress = hiddenProgress(stateComponent);
    if (hiddenProgress <= 0.001f) {
      return;
    }

    core.utils.Rectangle worldRegion =
      hideComponent.region().translate(Vector2.of(positionComponent.position()));
    ScreenRect region = toScreenRect(worldRegion);
    int edgePx = LitiengineCameraViews.worldLengthToScreen(hideComponent.transitionSize());

    drawSoftEdge(g, region, edgePx, hiddenProgress);

    int fillAlpha = clampAlpha(Math.round(MAX_FILL_ALPHA * hiddenProgress));
    if (fillAlpha > 0) {
      g.setColor(new Color(0, 0, 0, fillAlpha));
      g.fillRect(region.x, region.y, region.width, region.height);
    }
  }

  private float hiddenProgress(LevelHideStateComponent stateComponent) {
    float phase = clamp01(stateComponent.transitionElapsedSeconds() / TRANSITION_DURATION_SECONDS);
    return stateComponent.hiding() ? phase : 1f - phase;
  }

  private void drawSoftEdge(Graphics2D g, ScreenRect region, int edgePx, float hiddenProgress) {
    if (edgePx <= 0) {
      return;
    }

    for (int i = 0; i < EDGE_STEPS; i++) {
      float outerFactor = 1f - (i / (float) EDGE_STEPS);
      float innerFactor = 1f - ((i + 1f) / EDGE_STEPS);

      int outerExpand = Math.max(0, Math.round(edgePx * outerFactor));
      int innerExpand = Math.max(0, Math.round(edgePx * innerFactor));

      ScreenRect outer = region.expand(outerExpand);
      ScreenRect inner = region.expand(innerExpand);

      int alpha =
        clampAlpha(Math.round(MAX_EDGE_ALPHA * hiddenProgress * ((i + 1f) / EDGE_STEPS)));
      if (alpha > 0) {
        fillBands(g, outer, inner, new Color(0, 0, 0, alpha));
      }
    }
  }

  private void fillBands(Graphics2D g, ScreenRect outer, ScreenRect inner, Color color) {
    int outerRight = outer.x + outer.width;
    int outerBottom = outer.y + outer.height;
    int innerRight = inner.x + inner.width;
    int innerBottom = inner.y + inner.height;

    g.setColor(color);

    if (inner.y > outer.y) {
      g.fillRect(outer.x, outer.y, outer.width, inner.y - outer.y);
    }
    if (innerBottom < outerBottom) {
      g.fillRect(outer.x, innerBottom, outer.width, outerBottom - innerBottom);
    }
    if (inner.x > outer.x) {
      g.fillRect(outer.x, inner.y, inner.x - outer.x, inner.height);
    }
    if (innerRight < outerRight) {
      g.fillRect(innerRight, inner.y, outerRight - innerRight, inner.height);
    }
  }

  private ScreenRect toScreenRect(core.utils.Rectangle worldRect) {
    Point topLeftWorld = new Point(worldRect.x(), worldRect.y() + worldRect.height());
    Point screenTopLeft = LitiengineCameraViews.worldToScreen(topLeftWorld);

    return new ScreenRect(
      Math.round(screenTopLeft.x()),
      Math.round(screenTopLeft.y()),
      LitiengineCameraViews.worldLengthToScreen(worldRect.width()),
      LitiengineCameraViews.worldLengthToScreen(worldRect.height()));
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0f, 1f);
  }

  private static int clampAlpha(int alpha) {
    return Math.clamp(alpha, 0, 255);
  }

  private record ScreenRect(int x, int y, int width, int height) {
    private ScreenRect expand(int amount) {
      return new ScreenRect(x - amount, y - amount, width + 2 * amount, height + 2 * amount);
    }
  }
}
