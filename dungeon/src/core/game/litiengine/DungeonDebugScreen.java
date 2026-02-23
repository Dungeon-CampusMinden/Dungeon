package core.game.litiengine;

import core.game.ECSManagement;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Minimal LITIENGINE screen to verify that rendering is active.
 *
 * <p>Renders a small overlay (tick + entity count). This is intentionally independent
 * of the Dungeon rendering pipeline (which is still libGDX-based).
 */
public final class DungeonDebugScreen extends Screen {
  public static final String NAME = "dungeon-debug";

  public DungeonDebugScreen() {
    super(NAME);
  }

  @Override
  public void render(Graphics2D g) {
    super.render(g);

    // Screen-space overlay: keep it simple and independent of camera/world transforms.
    g.setColor(Color.WHITE);

    final int tick = ECSManagement.currentTick();
    final long entitiesInLevel = ECSManagement.levelEntities().count();

    TextRenderer.render(g, "Dungeon (LITIENGINE) - tick=" + tick + " entities=" + entitiesInLevel, 10, 20);
  }
}
