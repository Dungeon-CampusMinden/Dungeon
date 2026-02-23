package core.game.litiengine;

import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.LevelElement;
import core.utils.Point;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Optional;

/**
 * Minimal LITIENGINE screen to verify that rendering is active.
 *
 * <p>Renders a debug overlay (tick + entity count) and a simple world visualization:
 * level grid + entity markers based on PositionComponent.
 *
 * <p>This is intentionally independent of the Dungeon rendering pipeline (which is still libGDX-based).
 */
public final class DungeonDebugScreen extends Screen {
  public static final String NAME = "dungeon-debug";

  // Simple debug scale: treat dungeon world units as "tiles"
  private static final int TILE_PX = 32;
  private static final int ENTITY_PX = 10;

  public DungeonDebugScreen() {
    super(NAME);
  }

  @Override
  public void render(Graphics2D g) {
    super.render(g);

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

        // simple coloring: walls darker, floor lighter
        LevelElement le = t.levelElement();
        if (le == LevelElement.WALL) {
          g.setColor(new Color(40, 40, 40));
        } else {
          g.setColor(new Color(80, 80, 80));
        }

        int sx = x * TILE_PX;
        int sy = (h - 1 - y) * TILE_PX; // flip Y for screen coordinates
        g.fillRect(sx, sy, TILE_PX, TILE_PX);

        // grid lines (optional)
        g.setColor(new Color(20, 20, 20));
        g.drawRect(sx, sy, TILE_PX, TILE_PX);
      }
    }
  }

  private void renderEntities(Graphics2D g, Optional<ILevel> levelOpt) {
    // If we have a level, use its height for Y-flip.
    int levelHeight = levelOpt.map(l -> l.layout().length).orElse(0);

    ECSManagement.levelEntities()
      .forEach(
        e -> e.fetch(PositionComponent.class).ifPresent(pc -> drawEntityMarker(g, e, pc.position(), levelHeight)));
  }

  private void drawEntityMarker(Graphics2D g, Entity e, Point pos, int levelHeight) {
    // Interpret world position as tile coordinates (debug-only).
    // If your world uses pixel coordinates instead, you can switch TILE_PX to 1.
    int sx = Math.round(pos.x() * TILE_PX);
    int sy;

    if (levelHeight > 0) {
      sy = Math.round((levelHeight - 1 - pos.y()) * TILE_PX);
    } else {
      // fallback: no flip if level unknown
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

  private int getWidthSafe() {
    // Screen#getWidth isn't always available depending on LITIENGINE version; window size fallback.
    try {
      return (int) de.gurkenlabs.litiengine.Game.window().getWidth();
    } catch (Exception ignored) {
      return 1280;
    }
  }

  private int getHeightSafe() {
    try {
      return (int) de.gurkenlabs.litiengine.Game.window().getHeight();
    } catch (Exception ignored) {
      return 720;
    }
  }
}
