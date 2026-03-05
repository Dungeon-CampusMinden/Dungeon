package core.platform.litiengine.render;

import core.Entity;
import core.Game;
import core.System;
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
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sprite renderer for the LITIENGINE host.
 *
 * <p>This is the next step after the debug grid renderer: render actual tile textures and
 * entity sprites (DrawComponent) without debug grid lines.
 *
 * <p>Note: We still treat dungeon world units as tiles for now (TILE_PX). Camera/world scaling
 * will be introduced in a later commit.
 */
public final class LitiengineSpriteRenderSystem extends System {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(LitiengineSpriteRenderSystem.class);

  // Temporary: interpret dungeon world units as tiles.
  private static final int TILE_PX = 32;
  private static final int ENTITY_PX = 10;

  private final Map<String, BufferedImage> tileImageCache = new HashMap<>();

  public LitiengineSpriteRenderSystem() {
    super(AuthoritativeSide.BOTH, PositionComponent.class);
  }

  @Override
  public void execute() {
    // No game logic, just rendering.
  }

  @Override
  public void render(float deltaSeconds) {
    Graphics2D g = LitiengineGraphicsContext.get();
    if (g == null) return;

    // Pixel-art friendly scaling (avoid blurry interpolation on scaled sprites).
    Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

    try {
      // Background
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, getWidthSafe(), getHeightSafe());

      // Draw world (if level loaded)
      Optional<ILevel> levelOpt = Game.currentLevel();
      levelOpt.ifPresent(level -> renderLevelSprites(g, level));

      // Draw entities
      renderEntities(g, levelOpt);

      // Overlay text (keep minimal diagnostics)
      g.setColor(Color.WHITE);
      int tick = ECSManagement.currentTick();
      long entities = ECSManagement.levelEntities().count();
      TextRenderer.render(
        g,
        "Dungeon (LITIENGINE) tick=" + tick + " entities=" + entities
          + (levelOpt.isPresent() ? "" : " [no level loaded]"),
        10,
        20);
    } catch (Exception e) {
      LOGGER.warn("LITIENGINE sprite rendering failed: {}", e.getMessage(), e);
    } finally {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
    }
  }

  private void renderLevelSprites(Graphics2D g, ILevel level) {
    Tile[][] layout = level.layout();
    if (layout == null || layout.length == 0 || layout[0].length == 0) return;

    int h = layout.length;
    int w = layout[0].length;

    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        Tile t = layout[y][x];
        if (t == null) continue;

        int sx = x * TILE_PX;
        int sy = (h - 1 - y) * TILE_PX; // flip Y for screen coords

        BufferedImage img = imageForTile(t);
        if (img != null) {
          drawTileImage(g, img, sx, sy);
          continue;
        }

        // Fallback: simple coloring if no texture found
        LevelElement le = t.levelElement();
        g.setColor(le == LevelElement.WALL ? new Color(40, 40, 40) : new Color(80, 80, 80));
        g.fillRect(sx, sy, TILE_PX, TILE_PX);
      }
    }
  }

  private void drawTileImage(Graphics2D g, BufferedImage img, int sx, int sy) {
    if (img.getWidth() <= 0 || img.getHeight() <= 0) return;

    double scaleX = TILE_PX / (double) img.getWidth();
    double scaleY = TILE_PX / (double) img.getHeight();
    ImageRenderer.renderScaled(g, img, sx, sy, scaleX, scaleY);
  }

  private BufferedImage imageForTile(Tile t) {
    if (t == null || t.texturePath() == null) return null;

    String raw = t.texturePath().pathString();
    if (raw == null || raw.isBlank()) return null;

    String resolved = resolveImplicitFilePath(raw);

    // Cache hit (including cached-missing null)
    if (tileImageCache.containsKey(resolved)) {
      return tileImageCache.get(resolved);
    }

    BufferedImage img = Resources.images().get(resolved);
    tileImageCache.put(resolved, img);
    return img;
  }

  private void renderEntities(Graphics2D g, Optional<ILevel> levelOpt) {
    int levelHeight = levelOpt.map(l -> l.layout().length).orElse(0);

    // Typical top-down ordering: higher Y first, lower Y last (in front).
    List<Entity> ordered =
      ECSManagement.levelEntities()
        .sorted(Comparator.comparingDouble(
          e -> -e.fetch(PositionComponent.class).map(pc -> pc.position().y()).orElse(0f)))
        .toList();

    for (Entity e : ordered) {
      PositionComponent pc = e.fetch(PositionComponent.class).orElse(null);
      if (pc == null) continue;

      DrawComponent dc = e.fetch(DrawComponent.class).orElse(null);
      if (dc != null && tryDrawEntitySprite(g, pc.position(), levelHeight, dc)) {
        continue;
      }

      // Fallback marker (so you still see entities without DrawComponent)
      drawEntityMarker(g, e, pc.position(), levelHeight);
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

    // World -> screen
    float sxWorld = pos.x() * TILE_PX;
    float syWorld = (levelHeight > 0)
      ? (levelHeight - 1 - pos.y()) * TILE_PX
      : pos.y() * TILE_PX;

    // Estimate size from state machine world size (fallback: 1 tile).
    int wPx = TILE_PX;
    int hPx = TILE_PX;

    try {
      float wWorld = dc.stateMachine().getWidth();
      float hWorld = dc.stateMachine().getHeight();
      if (wWorld > 0) wPx = Math.max(1, Math.round(wWorld * TILE_PX));
      if (hWorld > 0) hPx = Math.max(1, Math.round(hWorld * TILE_PX));
    } catch (Exception ignored) {
      // ignore
    }

    // Align: center X on tile, align bottom to tile bottom (so tall sprites extend upwards).
    int drawX = Math.round(sxWorld + (TILE_PX - wPx) / 2f);
    int drawY = Math.round(syWorld + TILE_PX - hPx);

    if (img.getWidth() <= 0 || img.getHeight() <= 0) return false;

    double scaleX = wPx / (double) img.getWidth();
    double scaleY = hPx / (double) img.getHeight();
    ImageRenderer.renderScaled(g, img, drawX, drawY, scaleX, scaleY);
    return true;
  }

  private void drawEntityMarker(Graphics2D g, Entity e, Point pos, int levelHeight) {
    int sx = Math.round(pos.x() * TILE_PX);
    int sy = (levelHeight > 0)
      ? Math.round((levelHeight - 1 - pos.y()) * TILE_PX)
      : Math.round(pos.y() * TILE_PX);

    // color: local player = green, other player = cyan, others = orange
    Color c = new Color(255, 165, 0);
    if (e.isPresent(PlayerComponent.class)) {
      boolean local = e.fetch(PlayerComponent.class).map(PlayerComponent::isLocal).orElse(false);
      c = local ? Color.GREEN : Color.CYAN;
    }

    g.setColor(c);
    int r = ENTITY_PX / 2;
    g.fillOval(sx - r, sy - r, ENTITY_PX, ENTITY_PX);
  }

  private static String resolveImplicitFilePath(String pathString) {
    if (pathString.endsWith("/") || !pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      String dir = pathString.replaceAll("/$", "");
      String baseName = dir.substring(dir.lastIndexOf('/') + 1);
      return dir + "/" + baseName + ".png";
    }
    return pathString;
  }

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
