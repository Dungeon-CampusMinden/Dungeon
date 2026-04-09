package core.platform.litiengine.render;

import core.Entity;
import core.Game;
import core.System;
import core.camera.CameraMath;
import core.components.CameraComponent;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.LevelElement;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sprite renderer for the LITIENGINE host.
 *
 * <p>Draws the current level tiles and entity sprites using LITIENGINE's Graphics2D pipeline.
 * The active Graphics2D is provided by {@link core.game.litiengine.EcsRenderScreen} via
 * {@link LitiengineGraphicsContext}.
 */
public final class LitiengineSpriteRenderSystem extends System {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(LitiengineSpriteRenderSystem.class);

  private static final int BASE_TILE_PX = 32;
  private static final int MIN_TILE_PX = 8;

  // How many tiles beyond the viewport we still render (avoid pop-in).
  private static final int VIEW_MARGIN_TILES = 2;

  // Optional smoothing (0 = no smoothing, 1 = jump). Keep small and stable.
  private static final float CAMERA_LERP = 0.20f;

  private final Map<String, BufferedImage> tileImageCache = new HashMap<>();

  // Camera state in world units (tile space).
  private Point cameraActual;

  public LitiengineSpriteRenderSystem() {
    super(AuthoritativeSide.BOTH, PositionComponent.class);
  }

  @Override
  public void execute() {
    // no-op (render-only system)
  }

  @Override
  public void render(float deltaSeconds) {
    final Graphics2D g = LitiengineGraphicsContext.get();
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
      LitiengineCameraViews.set(view.offsetX(), view.offsetY(), view.levelHeight(), view.tilePx());

      // Then proceed with translate/clip as before
      g.translate(view.offsetX(), view.offsetY());

      levelOpt.ifPresent(level -> renderLevelTiles(g, level, view));
      renderEntities(g, levelOpt, view);
    } catch (Exception e) {
      LOGGER.warn("LITIENGINE sprite rendering failed: {}", e.getMessage(), e);
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

    // Use the same implicit resolution that LitiengineImages uses internally.
    String key = LitiengineImages.resolveImplicitFilePath(raw);

    if (tileImageCache.containsKey(key)) {
      return tileImageCache.get(key); // can be null -> cached miss
    }

    BufferedImage img = LitiengineImages.get(raw);
    tileImageCache.put(key, img);
    return img;
  }

  private void renderEntities(Graphics2D g, Optional<ILevel> levelOpt, CameraView view) {
    final int levelHeight = view.levelHeight();

    // Sort entities by y so "lower" entities appear in front (cheap painter's algorithm).
    final List<Entity> entities =
      ECSManagement.levelEntities()
        .sorted(Comparator.comparingDouble(e ->
          e.fetch(PositionComponent.class).map(pc -> pc.position().y()).orElse(0f)))
        .toList();

    for (Entity e : entities) {
      final Optional<PositionComponent> pcOpt = e.fetch(PositionComponent.class);
      if (pcOpt.isEmpty()) continue;

      final Point pos = pcOpt.get().position();

      // Viewport culling in world space (tiles).
      if (pos.x() < view.minTileX() || pos.x() > view.maxTileX() || pos.y() < view.minTileY() || pos.y() > view.maxTileY()) {
        continue;
      }

      final Optional<DrawComponent> dcOpt = e.fetch(DrawComponent.class);
      if (dcOpt.isPresent()) {
        if (tryDrawEntitySprite(g, e, pos, levelHeight, view.tilePx(), dcOpt.get())) {
          continue;
        }
      }

      // Fallback marker (debug)
      drawEntityMarker(g, e, pos, levelHeight, view.tilePx());
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

    BufferedImage img = LitiengineAnimationFrames.toImage(frame);
    if (img == null) return false;

    BufferedImage renderImg = applyTintIfNeeded(img, dc.tintColor());
    if (renderImg == null || renderImg.getWidth() <= 0 || renderImg.getHeight() <= 0) {
      return false;
    }

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

    LitiengineOutlineEffectComponent outline =
      entity.fetch(LitiengineOutlineEffectComponent.class).orElse(null);

    if (outline == null) {
      double scaleX = wPx / (double) renderImg.getWidth();
      double scaleY = hPx / (double) renderImg.getHeight();
      ImageRenderer.renderScaled(g, renderImg, drawX, drawY, scaleX, scaleY);
      return true;
    }

    long nowMs = Time.nowMs();
    int outlinePx = LitiengineImageEffects.effectiveOutlineWidth(outline, nowMs);
    Color outlineColor = LitiengineImageEffects.effectiveOutlineColor(outline, nowMs);

    LitiengineImageEffects.drawOutlinedSprite(
      g, renderImg, drawX, drawY, wPx, hPx, outlineColor, outlinePx);
    return true;
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

    final Point target = resolveCameraFocus(levelOpt);
    this.cameraActual = CameraMath.stepTowardsFocus(this.cameraActual, target, CAMERA_LERP);
    final Point focus = this.cameraActual != null ? this.cameraActual : target;

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

  private Point resolveCameraFocus(Optional<ILevel> levelOpt) {
    return CameraMath.resolveFocus(
      resolveTrackedPoint(),
      levelOpt.isPresent() ? Game.startTile().map(Tile::position) : Optional.empty());
  }

  private Optional<Point> resolveTrackedPoint() {
    Optional<Point> playerPos =
      Game.player()
        .flatMap(player -> player.fetch(PositionComponent.class))
        .map(PositionComponent::position);

    if (playerPos.isPresent()) {
      return playerPos;
    }

    return ECSManagement.levelEntities()
      .filter(entity -> entity.isPresent(CameraComponent.class))
      .findFirst()
      .flatMap(entity -> entity.fetch(PositionComponent.class))
      .map(PositionComponent::position);
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
    return Math.max(MIN_TILE_PX, Math.round(BASE_TILE_PX * LitiengineCameraState.zoom()));
  }
}
