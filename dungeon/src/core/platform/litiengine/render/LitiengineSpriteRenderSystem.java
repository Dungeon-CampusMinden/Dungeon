package core.platform.litiengine.render;

import core.Entity;
import core.Game;
import core.System;
import core.components.CameraComponent;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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

  // Current "world unit" assumption: 1 tile == TILE_PX pixels.
  // (This commit adds a basic camera transform; scaling can be modeled later.)
  private static final int TILE_PX = 32;
  private static final int ENTITY_PX = 10;

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

      // Apply camera transform (world -> screen offset)
      g.translate(view.offsetX, view.offsetY);

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

        final int sx = x * TILE_PX;
        // flip y so that y=0 is bottom row in world space (matches existing renderer)
        final int sy = (height - 1 - y) * TILE_PX;

        // Keep your current "simple" tile visualization:
        // - either cached tile image (if present)
        // - fallback to colored rect by tile type
        final LevelElement elem = tile.levelElement();
        BufferedImage img = tryResolveTileImage(elem);
        if (img != null) {
          double scaleX = TILE_PX / (double) img.getWidth();
          double scaleY = TILE_PX / (double) img.getHeight();
          ImageRenderer.renderScaled(g, img, sx, sy, scaleX, scaleY);
        } else {
          g.setColor(colorFor(elem));
          g.fillRect(sx, sy, TILE_PX, TILE_PX);
        }
      }
    }
  }

  private void renderEntities(Graphics2D g, Optional<ILevel> levelOpt, CameraView view) {
    final int levelHeight = view.levelHeight;

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
      if (pos.x() < view.minTileX || pos.x() > view.maxTileX || pos.y() < view.minTileY || pos.y() > view.maxTileY) {
        continue;
      }

      final Optional<DrawComponent> dcOpt = e.fetch(DrawComponent.class);
      if (dcOpt.isPresent()) {
        if (tryDrawEntitySprite(g, pos, levelHeight, dcOpt.get())) {
          continue;
        }
      }

      // Fallback marker (debug)
      drawEntityMarker(g, e, pos, levelHeight);
    }
  }

  private boolean tryDrawEntitySprite(Graphics2D g, Point pos, int levelHeight, DrawComponent dc) {
    final core.utils.components.draw.animation.AnimationFrame frame;
    try {
      frame = dc.stateMachine().getFrame();
    } catch (Exception ignored) {
      return false;
    }

    BufferedImage img = LitiengineAnimationFrames.toImage(frame);
    if (img == null) return false;

    float sxWorld = pos.x() * TILE_PX;
    float syWorld =
      (levelHeight > 0) ? (levelHeight - 1 - pos.y()) * TILE_PX : (pos.y() * TILE_PX);

    int wPx = TILE_PX;
    int hPx = TILE_PX;
    try {
      float wWorld = dc.stateMachine().getWidth();
      float hWorld = dc.stateMachine().getHeight();
      if (wWorld > 0) wPx = Math.max(1, Math.round(wWorld * TILE_PX));
      if (hWorld > 0) hPx = Math.max(1, Math.round(hWorld * TILE_PX));
    } catch (Exception ignored) {
    }

    int drawX = Math.round(sxWorld + (TILE_PX - wPx) / 2f);
    int drawY = Math.round(syWorld + TILE_PX - hPx);

    double scaleX = wPx / (double) img.getWidth();
    double scaleY = hPx / (double) img.getHeight();
    ImageRenderer.renderScaled(g, img, drawX, drawY, scaleX, scaleY);
    return true;
  }

  private void drawEntityMarker(Graphics2D g, Entity e, Point pos, int levelHeight) {
    int sx = Math.round(pos.x() * TILE_PX);
    int sy =
      (levelHeight > 0)
        ? Math.round((levelHeight - 1 - pos.y()) * TILE_PX)
        : Math.round(pos.y() * TILE_PX);

    Color c = new Color(255, 165, 0);
    if (e.isPresent(PlayerComponent.class)) {
      boolean local = e.fetch(PlayerComponent.class).map(PlayerComponent::isLocal).orElse(false);
      c = local ? Color.GREEN : Color.CYAN;
    }

    g.setColor(c);
    int r = ENTITY_PX / 2;
    g.fillOval(sx - r, sy - r, ENTITY_PX, ENTITY_PX);
  }

  private CameraView computeCameraView(Optional<ILevel> levelOpt) {
    final int screenW = getWidthSafe();
    final int screenH = getHeightSafe();
    final int levelHeight = levelOpt.map(l -> l.layout() != null ? l.layout().length : 0).orElse(0);

    final Point target = resolveCameraTarget(levelOpt);
    this.cameraActual = lerpPoint(this.cameraActual, target, CAMERA_LERP);

    final Point focus = (this.cameraActual != null) ? this.cameraActual : target;

    final int viewTilesX = (int) Math.ceil(screenW / (double) TILE_PX) + (VIEW_MARGIN_TILES * 2);
    final int viewTilesY = (int) Math.ceil(screenH / (double) TILE_PX) + (VIEW_MARGIN_TILES * 2);

    final int minTileX = (int) Math.floor(focus.x() - viewTilesX / 2.0);
    final int maxTileX = minTileX + viewTilesX;
    final int minTileY = (int) Math.floor(focus.y() - viewTilesY / 2.0);
    final int maxTileY = minTileY + viewTilesY;

    final double focusPxX = focus.x() * TILE_PX + (TILE_PX / 2.0);
    final double focusPxY =
      (levelHeight > 0)
        ? (levelHeight - 1 - focus.y()) * TILE_PX + (TILE_PX / 2.0)
        : (focus.y() * TILE_PX + (TILE_PX / 2.0));

    final double offsetX = (screenW / 2.0) - focusPxX;
    final double offsetY = (screenH / 2.0) - focusPxY;

    return new CameraView(offsetX, offsetY, minTileX, maxTileX, minTileY, maxTileY, levelHeight);
  }

  private Point resolveCameraTarget(Optional<ILevel> levelOpt) {
    // 1) Prefer local player if present
    Optional<Point> playerPos =
      Game.player().flatMap(p -> p.fetch(PositionComponent.class)).map(PositionComponent::position);
    if (playerPos.isPresent()) return playerPos.get();

    // 2) Otherwise: any entity with CameraComponent
    Optional<Point> camPos =
      ECSManagement.levelEntities()
        .filter(e -> e.isPresent(CameraComponent.class))
        .findFirst()
        .flatMap(e -> e.fetch(PositionComponent.class))
        .map(PositionComponent::position);
    return camPos.orElseGet(() -> Game.startTile().map(Tile::position).orElseGet(() -> new Point(0, 0)));

    // 3) Fallback: start tile or origin
  }

  private static Point lerpPoint(Point current, Point target, float alpha) {
    if (target == null) return current;
    if (current == null) return target;
    float a = clamp(alpha, 0f, 1f);
    float nx = current.x() * (1f - a) + target.x() * a;
    float ny = current.y() * (1f - a) + target.y() * a;
    return new Point(nx, ny);
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }

  private static float clamp(float v, float min, float max) {
    return Math.max(min, Math.min(max, v));
  }

  private BufferedImage tryResolveTileImage(LevelElement elem) {
    if (elem == null) return null;
    // Keep it conservative: only try cache lookup if you already have keys.
    // You can expand this mapping later (tile spritesheets / atlas).
    return tileImageCache.get(elem.name());
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
    int levelHeight
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
}
