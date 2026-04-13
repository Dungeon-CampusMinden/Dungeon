package core.platform.litiengine.render;

import contrib.modules.levelHide.LevelHideComponent;
import contrib.modules.levelHide.LevelHideStateComponent;
import core.Entity;
import core.System;
import core.camera.LitiengineCameraViews;
import core.components.PositionComponent;
import core.game.render.RenderContext;
import core.utils.Point;
import core.utils.Vector2;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;

/**
 * LITIENGINE render-only implementation for hidden world regions.
 *
 * <p>This system interprets the engine-neutral level-hide state and paints a dark overlay in
 * world-space using the current Graphics2D context.
 *
 * <p>The rendering is intentionally aligned with the former shader semantics:
 *
 * <ul>
 *   <li>one continuous hide/reveal progress value,
 *   <li>one continuous transition band derived from transitionSize,
 *   <li>a smooth time-based easing instead of stepped edge bands.
 * </ul>
 */
public final class LitiengineLevelHideRenderSystem extends System {
  private static final float TRANSITION_DURATION_SECONDS = 0.30f;
  private static final int MAX_ALPHA = 210;

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
    // render-only system
  }

  @Override
  public void render(float deltaSeconds) {
    Graphics2D g = RenderContext.get();
    if (g == null || LitiengineCameraViews.activeView().isEmpty()) {
      return;
    }

    filteredEntityStream().forEach(entity -> renderRegion(g, entity));
  }

  private void renderRegion(Graphics2D baseGraphics, Entity entity) {
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
    int transitionPixels = transitionPixels(hideComponent.transitionSize());
    int alpha = alphaFor(hiddenProgress);

    if (region.width <= 0 || region.height <= 0 || alpha <= 0) {
      return;
    }

    Graphics2D g = (Graphics2D) baseGraphics.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

      drawInterior(g, region, alpha);

      if (transitionPixels > 0) {
        drawTransitionBands(g, region, transitionPixels, alpha);
        drawTransitionCorners(g, region, transitionPixels, alpha);
      }
    } finally {
      g.dispose();
    }
  }

  private void drawInterior(Graphics2D g, ScreenRect region, int alpha) {
    g.setColor(colorWithAlpha(alpha));
    g.fillRect(region.x, region.y, region.width, region.height);
  }

  private void drawTransitionBands(Graphics2D g, ScreenRect region, int transitionPixels, int alpha) {
    int leftX = region.x - transitionPixels;
    int rightX = region.right();
    int topY = region.y - transitionPixels;
    int bottomY = region.bottom();

    // left
    fillWithPaint(
      g,
      new LinearGradientPaint(
        region.x,
        0,
        leftX,
        0,
        new float[] {0f, 1f},
        new Color[] {colorWithAlpha(alpha), colorWithAlpha(0)}),
      leftX,
      region.y,
      transitionPixels,
      region.height);

    // right
    fillWithPaint(
      g,
      new LinearGradientPaint(
        rightX,
        0,
        rightX + transitionPixels,
        0,
        new float[] {0f, 1f},
        new Color[] {colorWithAlpha(alpha), colorWithAlpha(0)}),
      rightX,
      region.y,
      transitionPixels,
      region.height);

    // top
    fillWithPaint(
      g,
      new LinearGradientPaint(
        0,
        region.y,
        0,
        topY,
        new float[] {0f, 1f},
        new Color[] {colorWithAlpha(alpha), colorWithAlpha(0)}),
      region.x,
      topY,
      region.width,
      transitionPixels);

    // bottom
    fillWithPaint(
      g,
      new LinearGradientPaint(
        0,
        bottomY,
        0,
        bottomY + transitionPixels,
        new float[] {0f, 1f},
        new Color[] {colorWithAlpha(alpha), colorWithAlpha(0)}),
      region.x,
      bottomY,
      region.width,
      transitionPixels);
  }

  private void drawTransitionCorners(
    Graphics2D g, ScreenRect region, int transitionPixels, int alpha) {
    drawCorner(
      g,
      region.x,
      region.y,
      region.x - transitionPixels,
      region.y - transitionPixels,
      transitionPixels,
      alpha);

    drawCorner(
      g,
      region.right(),
      region.y,
      region.right(),
      region.y - transitionPixels,
      transitionPixels,
      alpha);

    drawCorner(
      g,
      region.x,
      region.bottom(),
      region.x - transitionPixels,
      region.bottom(),
      transitionPixels,
      alpha);

    drawCorner(
      g,
      region.right(),
      region.bottom(),
      region.right(),
      region.bottom(),
      transitionPixels,
      alpha);
  }

  private void drawCorner(
    Graphics2D g,
    int centerX,
    int centerY,
    int areaX,
    int areaY,
    int transitionPixels,
    int alpha) {
    fillWithPaint(
      g,
      new RadialGradientPaint(
        centerX,
        centerY,
        transitionPixels,
        new float[] {0f, 1f},
        new Color[] {colorWithAlpha(alpha), colorWithAlpha(0)}),
      areaX,
      areaY,
      transitionPixels,
      transitionPixels);
  }

  private void fillWithPaint(Graphics2D g, Paint paint, int x, int y, int width, int height) {
    if (width <= 0 || height <= 0) {
      return;
    }

    Paint oldPaint = g.getPaint();
    try {
      g.setPaint(paint);
      g.fillRect(x, y, width, height);
    } finally {
      g.setPaint(oldPaint);
    }
  }

  private float hiddenProgress(LevelHideStateComponent stateComponent) {
    float phase = clamp01(stateComponent.transitionElapsedSeconds() / TRANSITION_DURATION_SECONDS);
    float easedPhase = smoothStep01(phase);
    return stateComponent.hiding() ? easedPhase : 1f - easedPhase;
  }

  private int transitionPixels(float transitionSizeWorldUnits) {
    if (!(transitionSizeWorldUnits > 0f)) {
      return 0;
    }

    int px = LitiengineCameraViews.worldLengthToScreen(transitionSizeWorldUnits);
    return Math.max(1, px);
  }

  private ScreenRect toScreenRect(core.utils.Rectangle worldRect) {
    Point topLeftWorld = new Point(worldRect.x(), worldRect.y() + worldRect.height());
    Point screenTopLeft = LitiengineCameraViews.worldToScreen(topLeftWorld);

    return new ScreenRect(
      Math.round(screenTopLeft.x()),
      Math.round(screenTopLeft.y()),
      Math.max(1, LitiengineCameraViews.worldLengthToScreen(worldRect.width())),
      Math.max(1, LitiengineCameraViews.worldLengthToScreen(worldRect.height())));
  }

  private int alphaFor(float hiddenProgress) {
    return clampAlpha(Math.round(MAX_ALPHA * clamp01(hiddenProgress)));
  }

  private static float smoothStep01(float value) {
    float t = clamp01(value);
    return t * t * (3f - 2f * t);
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0f, 1f);
  }

  private static int clampAlpha(int alpha) {
    return Math.clamp(alpha, 0, 255);
  }

  private static Color colorWithAlpha(int alpha) {
    return new Color(0, 0, 0, clampAlpha(alpha));
  }

  private record ScreenRect(int x, int y, int width, int height) {

    private int right() {
      return x + width;
    }

    private int bottom() {
      return y + height;
    }
  }
}
