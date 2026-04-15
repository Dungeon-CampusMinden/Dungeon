package core.game.render.sprite;

import contrib.modules.levelHide.LevelHideComponent;
import core.Entity;
import core.Game;
import core.System;
import core.camera.CameraMath;
import core.camera.CameraState;
import core.camera.CameraViewportState;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.render.AnimationFrameImages;
import core.game.render.RenderContext;
import core.render.ImageAssets;
import core.render.effects.ImageEffects;
import core.render.effects.OutlineEffectComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.LevelElement;
import core.platform.Platform;
import core.game.render.depth.DepthLayerEffectPipeline;
import core.game.render.sprite.effects.ShineSpriteEffect;
import core.game.render.sprite.effects.SpriteEffectsComponent;
import core.game.render.sprite.effects.SpriteEffectPipeline;
import core.game.render.level.LevelEffectPipeline;
import core.game.render.level.LevelPassContext;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.Time;
import core.utils.logging.DungeonLogger;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.*;

/**
 * A render system responsible for rendering sprites and level tiles to the game screen.
 *
 * <p>This system handles the complete rendering pipeline, including:
 * <ul>
 *   <li>Level tile rendering with optional effect passes</li>
 *   <li>Entity sprite rendering with depth sorting</li>
 *   <li>Camera positioning and viewport calculation</li>
 *   <li>Sprite effects such as shine and tinting</li>
 *   <li>Outline effects</li>
 *</ul>
 *
 * <p>The system supports both direct rendering and buffered rendering with effect pipelines.
 * It optimizes performance through caching and view frustum culling.
 */
public final class SpriteRenderSystem extends System {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(SpriteRenderSystem.class);

  private static final int BASE_TILE_PX = 32;
  private static final int MIN_TILE_PX = 8;

  // How many tiles beyond the viewport we still render (avoid pop-in).
  private static final int VIEW_MARGIN_TILES = 2;

  // Optional smoothing (0 = no smoothing, 1 = jump). Keep small and stable.
  private static final float CAMERA_LERP = 0.20f;

  private final Map<String, BufferedImage> tileImageCache = new HashMap<>();

  /**
   * Creates a new sprite render system.
   *
   * <p>This system operates on both the client and server sides and requires entities
   * to have a PositionComponent.
   */
  public SpriteRenderSystem() {
    super(AuthoritativeSide.BOTH, PositionComponent.class);
  }

  @Override
  public void execute() {
    // no-op (render-only system)
  }

  @Override
  public void render(float deltaSeconds) {
    final Graphics2D g = RenderContext.get();
    if (g == null) return;

    final Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
    );

    final AffineTransform oldTx = g.getTransform();

    try {
      // Background (keep explicit to avoid "garbage" frames)
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, getWidthSafe(), getHeightSafe());

      final Optional<ILevel> levelOpt = Game.currentLevel();
      final CameraView view = computeCameraView(levelOpt);

      // Publish view for cursor mapping
      CameraViewportState.set(view.offsetX(), view.offsetY(), view.levelHeight(), view.tilePx());

      // Then proceed with translate/clip as before
      g.translate(view.offsetX(), view.offsetY());

      levelOpt.ifPresent(level -> renderLevelWithPasses(g, level, view));
      renderEntities(g, levelOpt, view);
    } catch (Exception e) {
      LOGGER.warn("Sprite rendering failed: {}", e.getMessage(), e);
    } finally {
      g.setTransform(oldTx);
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
    }
  }

  private void renderLevelTiles(Graphics2D g, ILevel level, CameraView view) {
    final Tile[][] layout = level.layout();
    if (layout == null || layout.length == 0 || layout[0].length == 0) return;

    final int height = layout.length;
    final int width = layout[0].length;

    final int minX = clamp(view.minTileX, 0, width - 1);
    final int maxX = clamp(view.maxTileX, 0, width - 1);
    final int minY = clamp(view.minTileY, 0, height - 1);
    final int maxY = clamp(view.maxTileY, 0, height - 1);

    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        final Tile tile = layout[y][x];
        if (tile == null) continue;

        final int tilePx = view.tilePx();
        final int sx = x * tilePx;
        final int sy = (height - 1 - y) * tilePx;

        final LevelElement elem = tile.levelElement();
        BufferedImage img = imageForTile(tile);
        if (img != null) {
          drawTileImage(g, img, sx, sy, tilePx);
        } else {
          g.setColor(colorFor(elem));
          g.fillRect(sx, sy, tilePx, tilePx);
        }
      }
    }
  }

  private void renderLevelWithPasses(Graphics2D g, ILevel level, CameraView view) {
    if (!LevelEffectPipeline.hasEnabledEffects()) {
      renderLevelTiles(g, level, view);
      return;
    }

    VisibleLevelBuffer levelBuffer = renderVisibleLevelToBuffer(level, view);
    if (levelBuffer == null) {
      return;
    }

    BufferedImage processed =
      LevelEffectPipeline.apply(levelBuffer.image(), levelBuffer.context(), Time.nowMs());
    g.drawImage(processed, levelBuffer.drawX(), levelBuffer.drawY(), null);
  }

  private VisibleLevelBuffer renderVisibleLevelToBuffer(ILevel level, CameraView view) {
    final Tile[][] layout = level.layout();
    if (layout == null || layout.length == 0 || layout[0].length == 0) {
      return null;
    }

    final int height = layout.length;
    final int width = layout[0].length;

    final int minX = clamp(view.minTileX, 0, width - 1);
    final int maxX = clamp(view.maxTileX, 0, width - 1);
    final int minY = clamp(view.minTileY, 0, height - 1);
    final int maxY = clamp(view.maxTileY, 0, height - 1);

    if (minX > maxX || minY > maxY) {
      return null;
    }

    final int tilePx = view.tilePx();
    final int bufferWidth = Math.max(1, (maxX - minX + 1) * tilePx);
    final int bufferHeight = Math.max(1, (maxY - minY + 1) * tilePx);

    BufferedImage buffer = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D bg = buffer.createGraphics();

    try {
      bg.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

      for (int y = minY; y <= maxY; y++) {
        for (int x = minX; x <= maxX; x++) {
          final Tile tile = layout[y][x];
          if (tile == null) {
            continue;
          }

          final int sx = (x - minX) * tilePx;
          final int sy = (maxY - y) * tilePx;

          final LevelElement elem = tile.levelElement();
          BufferedImage img = imageForTile(tile);
          if (img != null) {
            drawTileImage(bg, img, sx, sy, tilePx);
          } else {
            bg.setColor(colorFor(elem));
            bg.fillRect(sx, sy, tilePx, tilePx);
          }
        }
      }
    } finally {
      bg.dispose();
    }

    int drawX = minX * tilePx;
    int drawY = (height - 1 - maxY) * tilePx;
    return new VisibleLevelBuffer(
      buffer,
      drawX,
      drawY,
      new LevelPassContext(minX, maxY, tilePx));
  }

  private record VisibleLevelBuffer(
    BufferedImage image,
    int drawX,
    int drawY,
    LevelPassContext context) {}

  private void drawTileImage(Graphics2D g, BufferedImage img, int sx, int sy, int tilePx) {
    if (img.getWidth() <= 0 || img.getHeight() <= 0) return;
    double scaleX = tilePx / (double) img.getWidth();
    double scaleY = tilePx / (double) img.getHeight();
    ImageRenderer.renderScaled(g, img, sx, sy, scaleX, scaleY);
  }

  private BufferedImage imageForTile(Tile t) {
    if (t == null || t.texturePath() == null) return null;
    String raw = t.texturePath().pathString();
    if (raw == null || raw.isBlank()) return null;

    String key = ImageAssets.resolveImplicitFilePath(raw);

    if (tileImageCache.containsKey(key)) {
      return tileImageCache.get(key); // can be null -> cached miss
    }

    BufferedImage img = ImageAssets.get(raw);
    tileImageCache.put(key, img);
    return img;
  }

  private void renderEntities(Graphics2D g, Optional<ILevel> levelOpt, CameraView view) {
    final int levelHeight = view.levelHeight();

    TreeMap<Integer, List<Entity>> depthGroups = visibleEntitiesByDepth(view);

    for (Map.Entry<Integer, List<Entity>> entry : depthGroups.entrySet()) {
      renderDepthLayer(g, entry.getKey(), entry.getValue(), levelHeight, view);
    }
  }

  private TreeMap<Integer, List<Entity>> visibleEntitiesByDepth(CameraView view) {
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
                entity.fetch(PositionComponent.class).map(pc -> pc.position().y()).orElse(0f))));

    return depthGroups;
  }

  private boolean isWithinVisibleTileBounds(Point pos, CameraView view) {
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
    CameraView view) {

    if (entitiesAtDepth.isEmpty()) {
      return;
    }

    if (!DepthLayerEffectPipeline.hasEnabledEffects(depthLayer)) {
      renderEntityList(g, entitiesAtDepth, levelHeight, view.tilePx());
      return;
    }

    BufferedImage layerBuffer =
      new BufferedImage(
        Math.max(1, getWidthSafe()),
        Math.max(1, getHeightSafe()),
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D layerGraphics = layerBuffer.createGraphics();
    try {
      layerGraphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      layerGraphics.translate(view.offsetX(), view.offsetY());
      renderEntityList(layerGraphics, entitiesAtDepth, levelHeight, view.tilePx());
    } finally {
      layerGraphics.dispose();
    }

    BufferedImage processed =
      DepthLayerEffectPipeline.apply(depthLayer, layerBuffer, Time.nowMs());

    g.drawImage(
      processed,
      Math.round((float) -view.offsetX()),
      Math.round((float) -view.offsetY()),
      null);
  }

  private void renderEntityList(
    Graphics2D g, List<Entity> entities, int levelHeight, int tilePx) {
    for (Entity e : entities) {
      final Optional<PositionComponent> pcOpt = e.fetch(PositionComponent.class);
      if (pcOpt.isEmpty()) {
        continue;
      }

      final Point pos = pcOpt.get().position();

      final Optional<DrawComponent> dcOpt = e.fetch(DrawComponent.class);
      if (dcOpt.isPresent()) {
        if (tryDrawEntitySprite(g, e, pos, levelHeight, tilePx, dcOpt.get())) {
          continue;
        }
      }

      if (e.isPresent(LevelHideComponent.class)) {
        continue;
      }

      drawEntityMarker(g, e, pos, levelHeight, tilePx);
    }
  }

  private boolean tryDrawEntitySprite(
    Graphics2D g, Entity entity, Point pos, int levelHeight, int tilePx, DrawComponent dc) {
    final core.utils.components.draw.animation.AnimationFrame frame;
    try {
      frame = dc.stateMachine().getFrame();
    } catch (Exception ignored) {
      return false;
    }

    BufferedImage img = AnimationFrameImages.toImage(frame);
    if (img == null) return false;

    BufferedImage renderImg = applyTintIfNeeded(img, dc.tintColor());
    if (renderImg == null || renderImg.getWidth() <= 0 || renderImg.getHeight() <= 0) {
      return false;
    }

    long nowMs = Time.nowMs();
    renderImg = SpriteEffectPipeline.apply(entity, renderImg, nowMs);

    float sxWorld = pos.x() * tilePx;
    float syWorld =
      (levelHeight > 0) ? (levelHeight - 1 - pos.y()) * tilePx : (pos.y() * tilePx);

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

    OutlineEffectComponent outline =
      entity.fetch(OutlineEffectComponent.class).orElse(null);

    if (outline == null) {
      drawScaledImage(g, renderImg, drawX, drawY, wPx, hPx);
      drawOverlayImages(g, shineOverlays);
      return true;
    }

    int outlinePx = ImageEffects.effectiveOutlineWidth(outline, nowMs);
    Color outlineColor = ImageEffects.effectiveOutlineColor(outline, nowMs);

    ImageEffects.drawOutlinedSprite(
      g, renderImg, drawX, drawY, wPx, hPx, outlineColor, outlinePx);
    drawOverlayImages(g, shineOverlays);
    return true;
  }

  private ArrayList<OverlayDraw> createShineOverlays(
    Entity entity,
    BufferedImage baseSprite,
    long nowMs,
    int drawX,
    int drawY,
    int wPx,
    int hPx) {

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

        overlays.add(new OverlayDraw(overlay, overlayDrawX, overlayDrawY, overlayWidth, overlayHeight));
      }
    }

    return overlays;
  }

  private void drawOverlayImages(Graphics2D g, List<OverlayDraw> overlays) {
    for (OverlayDraw overlay : overlays) {
      drawScaledImage(
        g,
        overlay.image(),
        overlay.drawX(),
        overlay.drawY(),
        overlay.width(),
        overlay.height());
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

  private record OverlayDraw(
    BufferedImage image,
    int drawX,
    int drawY,
    int width,
    int height) {}

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

  private void drawEntityMarker(Graphics2D g, Entity e, Point pos, int levelHeight, int tilePx) {
    int sx = Math.round(pos.x() * tilePx);
    int sy =
      (levelHeight > 0)
        ? Math.round((levelHeight - 1 - pos.y()) * tilePx)
        : Math.round(pos.y() * tilePx);

    Color c = new Color(255, 165, 0);
    if (e.isPresent(PlayerComponent.class)) {
      boolean local = e.fetch(PlayerComponent.class).map(PlayerComponent::isLocal).orElse(false);
      c = local ? Color.GREEN : Color.CYAN;
    }

    g.setColor(c);
    int size = Math.max(6, Math.round(tilePx * 0.3f));
    int r = size / 2;
    g.fillOval(sx - r, sy - r, size, size);
  }

  private CameraView computeCameraView(Optional<ILevel> levelOpt) {
    final int screenW = getWidthSafe();
    final int screenH = getHeightSafe();
    final int levelHeight =
      levelOpt.map(level -> level.layout() != null ? level.layout().length : 0).orElse(0);
    final int tilePx = effectiveTilePx();

    final Point target = resolveCameraFollowTarget(levelOpt);
    CameraState.followTarget(target);

    final Point focus = CameraState.stepFocus(CAMERA_LERP);

    final Rectangle worldBounds =
      CameraMath.worldBounds(
          focus,
          screenW / (float) tilePx,
          screenH / (float) tilePx,
          1f)
        .expand(VIEW_MARGIN_TILES);

    final int minTileX = (int) Math.floor(worldBounds.x());
    final int maxTileX = (int) Math.ceil(worldBounds.x() + worldBounds.width());
    final int minTileY = (int) Math.floor(worldBounds.y());
    final int maxTileY = (int) Math.ceil(worldBounds.y() + worldBounds.height());

    final double focusPxX = focus.x() * tilePx + (tilePx / 2.0);
    final double focusPxY =
      (levelHeight > 0)
        ? ((levelHeight - 1 - focus.y()) * tilePx + (tilePx / 2.0))
        : (focus.y() * tilePx + (tilePx / 2.0));

    final double offsetX = (screenW / 2.0) - focusPxX;
    final double offsetY = (screenH / 2.0) - focusPxY;

    return new CameraView(
      offsetX, offsetY, minTileX, maxTileX, minTileY, maxTileY, levelHeight, tilePx);
  }

  private Point resolveCameraFollowTarget(Optional<ILevel> levelOpt) {
    if (Platform.camera().supportsFollowTargetResolution()) {
      return Platform.camera().resolveFollowTarget();
    }

    return CameraMath.resolveFocus(
      Optional.empty(),
      levelOpt.isPresent() ? Game.startTile().map(Tile::position) : Optional.empty());
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }

  private static Color colorFor(LevelElement elem) {
    if (elem == null) return Color.DARK_GRAY;
    return switch (elem) {
      case FLOOR -> new Color(40, 40, 40);
      case WALL -> new Color(90, 90, 90);
      case HOLE -> new Color(10, 10, 10);
      default -> new Color(60, 60, 60);
    };
  }

  private record CameraView(
    double offsetX,
    double offsetY,
    int minTileX,
    int maxTileX,
    int minTileY,
    int maxTileY,
    int levelHeight,
    int tilePx
  ) {}

  private int getWidthSafe() {
    try {
      return de.gurkenlabs.litiengine.Game.window().getWidth();
    } catch (Exception ignored) {
      return 1280;
    }
  }

  private int getHeightSafe() {
    try {
      return de.gurkenlabs.litiengine.Game.window().getHeight();
    } catch (Exception ignored) {
      return 720;
    }
  }

  private static int effectiveTilePx() {
    return Math.max(MIN_TILE_PX, Math.round(BASE_TILE_PX * CameraState.zoom()));
  }
}
