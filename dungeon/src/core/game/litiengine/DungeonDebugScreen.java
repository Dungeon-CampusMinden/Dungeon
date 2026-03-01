package core.game.litiengine;

import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.LevelElement;
import core.utils.Point;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.resources.Resources;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Minimal LITIENGINE screen to verify that rendering is active.
 *
 * <p>Renders a debug overlay (tick + entity count) and a simple world visualization:
 * level grid + entity markers based on PositionComponent.
 *
 * <p>This is intentionally independent of the Dungeon libGDX rendering pipeline.
 * It uses LITIENGINE's pure Java (AWT) rendering and resource loading.
 */
public final class DungeonDebugScreen extends Screen {
  public static final String NAME = "dungeon-debug";

  // Treat dungeon world units as "tiles" (debug-only).
  private static final int TILE_PX = 32;
  private static final int ENTITY_PX = 10;

  // Toggle: render actual tile textures if available on the classpath.
  private static final boolean DRAW_TILE_TEXTURES = true;

  // Cache resolved texture paths -> loaded images (also caches "missing" as null).
  private final Map<String, BufferedImage> tileImageCache = new HashMap<>();

  public DungeonDebugScreen() {
    super(NAME);
  }

  @Override
  public void render(Graphics2D g) {
    super.render(g);

    // Pixel-art friendly scaling (avoid blurry interpolation on scaled sprites).
    Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
    );

    // Background
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, getWidthSafe(), getHeightSafe());

    // Draw world (if level loaded)
    Optional<ILevel> levelOpt = Game.currentLevel();
    levelOpt.ifPresent(level -> renderLevelGrid(g, level));

    // Draw entities (even if no level)
    renderEntities(g, levelOpt);

    // Overlay text
    g.setColor(Color.WHITE);
    int tick = core.game.ECSManagement.currentTick();
    long entities = core.game.ECSManagement.levelEntities().count();
    TextRenderer.render(
      g,
      "Dungeon (LITIENGINE) tick=" + tick + " entities=" + entities
        + (levelOpt.isPresent() ? "" : " [no level loaded]"),
      10,
      20
    );

    // Restore hint
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
  }

  private void renderLevelGrid(Graphics2D g, ILevel level) {
    Tile[][] layout = level.layout();
    if (layout == null || layout.length == 0 || layout[0].length == 0) {
      return;
    }

    int h = layout.length;
    int w = layout[0].length;

    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        Tile t = layout[y][x];
        if (t == null) continue;

        int sx = x * TILE_PX;
        int sy = (h - 1 - y) * TILE_PX; // flip Y for screen coords

        if (DRAW_TILE_TEXTURES) {
          BufferedImage img = imageForTile(t);
          if (img != null) {
            drawTileImage(g, img, sx, sy);
            continue;
          }
        }

        // fallback: simple coloring if no texture found
        LevelElement le = t.levelElement();
        if (le == LevelElement.WALL) {
          g.setColor(new Color(40, 40, 40));
        } else {
          g.setColor(new Color(80, 80, 80));
        }
        g.fillRect(sx, sy, TILE_PX, TILE_PX);

        // grid lines
        g.setColor(new Color(20, 20, 20));
        g.drawRect(sx, sy, TILE_PX, TILE_PX);
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

    // Let LITIENGINE load + cache internally (ResourcesContainer is an in-memory cache).
    // Returns null if not found.
    BufferedImage img = Resources.images().get(resolved);

    tileImageCache.put(resolved, img);
    return img;
  }

  private void renderEntities(Graphics2D g, Optional<ILevel> levelOpt) {
    int levelHeight = levelOpt.map(l -> l.layout().length).orElse(0);

    core.game.ECSManagement.levelEntities()
      .forEach(e ->
        e.fetch(PositionComponent.class)
          .ifPresent(pc -> drawEntityMarker(g, e, pc.position(), levelHeight)));
  }

  private void drawEntityMarker(Graphics2D g, Entity e, Point pos, int levelHeight) {
    int sx = Math.round(pos.x() * TILE_PX);
    int sy;

    if (levelHeight > 0) {
      sy = Math.round((levelHeight - 1 - pos.y()) * TILE_PX);
    } else {
      sy = Math.round(pos.y() * TILE_PX);
    }

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
    // Keep identical behavior to Animation/TextureMap helpers:
    // - If a folder is given: "<dir>/<basename>.png"
    // - If no known extension is present: same as above
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
