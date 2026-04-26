package core.game.render.sprite;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.game.render.depth.DepthLayerEffectPipeline;
import core.game.render.image.ImageFrameResolver;
import core.game.render.sprite.effects.SpriteEffectPipeline;
import core.game.render.sprite.effects.SpriteEffectsComponent;
import core.game.render.sprite.effects.SpriteOutlineComponent;
import core.game.render.sprite.effects.SpriteOutlineRenderer;
import core.game.render.sprite.effects.shine.ShineSpriteEffect;
import core.utils.Point;
import core.utils.Time;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Handles rendering of entities with sprite-based visuals based on their position, depth, and
 * sprite effects.
 *
 * <p>This class is responsible for organizing and rendering entities into appropriate layers based
 * on their depth and visibility within the specified camera view. Additional effects such as
 * outlines, sprite tinting, and shine overlays are also applied if configured.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Organize visible entities by depth and sort them for proper rendering order.
 *   <li>Render entities with their visuals adjusted for depth-based effects, including blending and
 *       additional visual filters.
 *   <li>Handle sprite rendering for individual entities, including scaling, position adjustment,
 *       and application of effects.
 *   <li>Manage and render overlays with special effects such as shine or custom highlighting over
 *       entity sprites.
 * </ul>
 *
 * <p>The primary entry point is the {@code render} method, which processes and draws all visible
 * entities within the provided screen context.
 *
 * <p>Thread Safety: This class is not thread-safe and must be used from within the rendering
 * thread.
 */
final class EntitySpriteRenderer {
  void render(Graphics2D g, SpriteViewport view, int screenWidth, int screenHeight) {
    final int levelHeight = view.levelHeight();

    TreeMap<Integer, List<Entity>> depthGroups = visibleEntitiesByDepth(view);

    for (Map.Entry<Integer, List<Entity>> entry : depthGroups.entrySet()) {
      renderDepthLayer(
          g, entry.getKey(), entry.getValue(), levelHeight, view, screenWidth, screenHeight);
    }
  }

  private TreeMap<Integer, List<Entity>> visibleEntitiesByDepth(SpriteViewport view) {
    final TreeMap<Integer, List<Entity>> depthGroups = new TreeMap<>();

    ECSManagement.levelEntities()
        .filter(entity -> entity.fetch(PositionComponent.class).isPresent())
        .forEach(
            entity -> {
              Point pos = entity.fetch(PositionComponent.class).orElseThrow().position();

              if (!isWithinVisibleTileBounds(pos, view)) {
                return;
              }

              int depth = entity.fetch(DrawComponent.class).map(DrawComponent::depth).orElse(0);
              depthGroups.computeIfAbsent(depth, ignored -> new ArrayList<>()).add(entity);
            });

    depthGroups
        .values()
        .forEach(
            entitiesAtDepth ->
                entitiesAtDepth.sort(
                    Comparator.comparingDouble(
                        entity ->
                            entity
                                .fetch(PositionComponent.class)
                                .map(pc -> pc.position().y())
                                .orElse(0f))));

    return depthGroups;
  }

  private boolean isWithinVisibleTileBounds(Point pos, SpriteViewport view) {
    return !(pos.x() < view.minTileX()
        || pos.x() > view.maxTileX()
        || pos.y() < view.minTileY()
        || pos.y() > view.maxTileY());
  }

  private void renderDepthLayer(
      Graphics2D g,
      int depthLayer,
      List<Entity> entitiesAtDepth,
      int levelHeight,
      SpriteViewport view,
      int screenWidth,
      int screenHeight) {

    if (entitiesAtDepth.isEmpty()) {
      return;
    }

    if (!DepthLayerEffectPipeline.hasEnabledEffects(depthLayer)) {
      renderEntityList(g, entitiesAtDepth, levelHeight, view.tilePx());
      return;
    }

    BufferedImage layerBuffer =
        new BufferedImage(
            Math.max(1, screenWidth), Math.max(1, screenHeight), BufferedImage.TYPE_INT_ARGB);

    Graphics2D layerGraphics = layerBuffer.createGraphics();
    try {
      layerGraphics.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      layerGraphics.translate(view.offsetX(), view.offsetY());
      renderEntityList(layerGraphics, entitiesAtDepth, levelHeight, view.tilePx());
    } finally {
      layerGraphics.dispose();
    }

    BufferedImage processed = DepthLayerEffectPipeline.apply(depthLayer, layerBuffer, Time.nowMs());

    g.drawImage(
        processed, Math.round((float) -view.offsetX()), Math.round((float) -view.offsetY()), null);
  }

  private void renderEntityList(Graphics2D g, List<Entity> entities, int levelHeight, int tilePx) {
    for (Entity e : entities) {
      final Optional<PositionComponent> pcOpt = e.fetch(PositionComponent.class);
      if (pcOpt.isEmpty()) {
        continue;
      }

      final Point pos = pcOpt.get().position();

      final Optional<DrawComponent> dcOpt = e.fetch(DrawComponent.class);
      if (dcOpt.isPresent()) {
        DrawComponent dc = dcOpt.get();
        if (!dc.isVisible()) {
          continue;
        }

        tryDrawEntitySprite(g, e, pos, levelHeight, tilePx, dc);
      }
    }
  }

  private void tryDrawEntitySprite(
      Graphics2D g, Entity entity, Point pos, int levelHeight, int tilePx, DrawComponent dc) {
    final core.utils.components.draw.animation.AnimationFrame frame;
    try {
      frame = dc.stateMachine().getFrame();
    } catch (Exception ignored) {
      return;
    }

    BufferedImage img = ImageFrameResolver.toImage(frame);
    if (img == null) return;

    BufferedImage renderImg = applyTintIfNeeded(img, dc.tintColor());
    if (renderImg == null || renderImg.getWidth() <= 0 || renderImg.getHeight() <= 0) {
      return;
    }

    long nowMs = Time.nowMs();
    renderImg = SpriteEffectPipeline.apply(entity, renderImg, nowMs);

    float sxWorld = pos.x() * tilePx;
    float syWorld = (levelHeight > 0) ? (levelHeight - 1 - pos.y()) * tilePx : (pos.y() * tilePx);

    int wPx = tilePx;
    int hPx = tilePx;

    try {
      float wWorld = dc.stateMachine().getWidth();
      float hWorld = dc.stateMachine().getHeight();
      if (wWorld > 0) wPx = Math.max(1, Math.round(wWorld * tilePx));
      if (hWorld > 0) hPx = Math.max(1, Math.round(hWorld * tilePx));
    } catch (Exception ignored) {
    }

    int drawX = Math.round(sxWorld + (tilePx - wPx) / 2f);
    int drawY = Math.round(syWorld + tilePx - hPx);

    ArrayList<OverlayDraw> shineOverlays =
        createShineOverlays(entity, renderImg, nowMs, drawX, drawY, wPx, hPx);

    SpriteOutlineComponent outline = entity.fetch(SpriteOutlineComponent.class).orElse(null);

    if (outline == null) {
      drawScaledImage(g, renderImg, drawX, drawY, wPx, hPx);
      drawOverlayImages(g, shineOverlays);
      return;
    }

    int outlinePx = SpriteOutlineRenderer.effectiveOutlineWidth(outline, nowMs);
    Color outlineColor = SpriteOutlineRenderer.effectiveOutlineColor(outline, nowMs);

    SpriteOutlineRenderer.drawOutlinedSprite(
        g, renderImg, drawX, drawY, wPx, hPx, outlineColor, outlinePx);
    drawOverlayImages(g, shineOverlays);
  }

  private ArrayList<OverlayDraw> createShineOverlays(
      Entity entity, BufferedImage baseSprite, long nowMs, int drawX, int drawY, int wPx, int hPx) {

    ArrayList<OverlayDraw> overlays = new ArrayList<>();

    SpriteEffectsComponent effectsComponent =
        entity.fetch(SpriteEffectsComponent.class).orElse(null);
    if (effectsComponent == null || baseSprite == null) {
      return overlays;
    }

    for (var effect : effectsComponent.effects().getEnabledSorted()) {
      if (effect instanceof ShineSpriteEffect shineEffect) {
        BufferedImage overlay = shineEffect.createOverlay(baseSprite, nowMs);
        if (overlay == null || overlay.getWidth() <= 0 || overlay.getHeight() <= 0) {
          continue;
        }

        int overlayWidth =
            Math.max(1, Math.round(wPx * (overlay.getWidth() / (float) baseSprite.getWidth())));
        int overlayHeight =
            Math.max(1, Math.round(hPx * (overlay.getHeight() / (float) baseSprite.getHeight())));

        int overlayDrawX = drawX - Math.round((overlayWidth - wPx) / 2f);
        int overlayDrawY = drawY - Math.round((overlayHeight - hPx) / 2f);

        overlays.add(
            new OverlayDraw(overlay, overlayDrawX, overlayDrawY, overlayWidth, overlayHeight));
      }
    }

    return overlays;
  }

  private void drawOverlayImages(Graphics2D g, List<OverlayDraw> overlays) {
    for (OverlayDraw overlay : overlays) {
      drawScaledImage(
          g, overlay.image(), overlay.drawX(), overlay.drawY(), overlay.width(), overlay.height());
    }
  }

  private void drawScaledImage(
      Graphics2D g, BufferedImage image, int drawX, int drawY, int wPx, int hPx) {
    if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
      return;
    }

    double scaleX = wPx / (double) image.getWidth();
    double scaleY = hPx / (double) image.getHeight();
    ImageRenderer.renderScaled(g, image, drawX, drawY, scaleX, scaleY);
  }

  private BufferedImage applyTintIfNeeded(BufferedImage source, int tintRgba8888) {
    if (source == null) {
      return null;
    }

    // DrawComponent contract: -1 means "no tint".
    if (tintRgba8888 == -1) {
      return source;
    }

    float redScale = ((tintRgba8888 >>> 24) & 0xFF) / 255f;
    float greenScale = ((tintRgba8888 >>> 16) & 0xFF) / 255f;
    float blueScale = ((tintRgba8888 >>> 8) & 0xFF) / 255f;
    float alphaScale = (tintRgba8888 & 0xFF) / 255f;

    BufferedImage tinted =
        new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

    RescaleOp op =
        new RescaleOp(
            new float[] {redScale, greenScale, blueScale, alphaScale},
            new float[] {0f, 0f, 0f, 0f},
            null);

    op.filter(source, tinted);
    return tinted;
  }

  private record OverlayDraw(BufferedImage image, int drawX, int drawY, int width, int height) {}
}
