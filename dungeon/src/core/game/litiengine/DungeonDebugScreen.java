package core.game.litiengine;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.LevelElement;
import core.platform.litiengine.render.LitiengineAnimationFrames;
import core.utils.Point;
import core.utils.components.draw.animation.AnimationFrame;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.gui.screens.Screen;
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
 * Minimal LITIENGINE screen to verify that rendering is active.
 *
 * <p>Renders a debug overlay (tick + entity count) and a simple world visualization:
 * level grid + entities.
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

  // Toggle: render DrawComponent sprites (AnimationFrames) instead of only markers.
  private static final boolean DRAW_ENTITY_SPRITES = true;

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
      RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

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
    int tick = ECSManagement.currentTick();
    long entities = ECSManagement.levelEntities().count();
    TextRenderer.render(
      g,
      "Dungeon (LITIENGINE) tick=" + tick + " entities=" + entities
        + (levelOpt.isPresent() ? "" : " [no level loaded]"),
      10,
      20);

    // Restore hint
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);

    final float deltaSeconds = de.gurkenlabs.litiengine.Game.loop().getDeltaTime() / 1000.0f;
    ECSManagement.renderAll(deltaSeconds);
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

    // Sort like typical top-down rendering: higher Y first, lower Y last (in front).
    List<Entity> ordered =
      ECSManagement.levelEntities()
        .sorted(
          Comparator.comparingDouble(
            e ->
              -e.fetch(PositionComponent.class)
                .map(pc -> pc.position().y())
                .orElse(0f)))
        .toList();

    for (Entity e : ordered) {
      PositionComponent pc = e.fetch(PositionComponent.class).orElse(null);
      if (pc == null) continue;

      if (DRAW_ENTITY_SPRITES) {
        DrawComponent dc = e.fetch(DrawComponent.class).orElse(null);
        if (dc != null && tryDrawEntitySprite(g, e, pc.position(), levelHeight, dc)) {
          continue;
        }
      }

      drawEntityMarker(g, e, pc.position(), levelHeight);
    }
  }

  private boolean tryDrawEntitySprite(
    Graphics2D g, Entity e, Point pos, int levelHeight, DrawComponent dc) {

    final AnimationFrame frame;
    try {
      frame = dc.stateMachine().getFrame();
    } catch (Exception ignored) {
      return false;
    }

    BufferedImage img = LitiengineAnimationFrames.toImage(frame);
    if (img == null) return false;

    // World -> screen
    float sxWorld = pos.x() * TILE_PX;
    float syWorld;
    if (levelHeight > 0) {
      syWorld = (levelHeight - 1 - pos.y()) * TILE_PX;
    } else {
      syWorld = pos.y() * TILE_PX;
    }

    // Estimate size from state machine world size (fallback: 1 tile).
    int wPx = TILE_PX;
    int hPx = TILE_PX;

    try {
      float wWorld = dc.stateMachine().getWidth();
      float hWorld = dc.stateMachine().getHeight();
      if (wWorld > 0) wPx = Math.max(1, Math.round(wWorld * TILE_PX));
      if (hWorld > 0) hPx = Math.max(1, Math.round(hWorld * TILE_PX));
    } catch (Exception ignored) {}

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
