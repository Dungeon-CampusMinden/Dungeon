package core.game.render;

import core.game.ECSManagement;
import core.game.render.scene.SceneEffectPipeline;
import core.ui.overlay.UiOverlayRegistry;
import core.utils.Time;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * The main render screen for the ECS-based game engine.
 *
 * <p>This screen manages the rendering of all ECS systems and UI overlays. It supports optional
 * scene-level effect pipelines that process the entire rendered scene before display.
 *
 * <p>When scene effects are enabled, the screen renders to an intermediate buffer first,
 * applies the effect pipeline, and then draws the result to the display. Otherwise,
 * rendering is done directly for better performance.
 */
public final class EcsRenderScreen extends Screen {
  public static final String NAME = "ecs-render";

  /** Creates a new ECS render screen. */
  public EcsRenderScreen() {
    super(NAME);
  }

  @Override
  public void render(Graphics2D g) {
    super.render(g);

    final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;

    if (!SceneEffectPipeline.hasEnabledEffects()) {
      renderDirect(g, deltaSeconds);
      return;
    }

    BufferedImage sceneBuffer =
      new BufferedImage(
        Math.max(1, core.Game.windowWidth()),
        Math.max(1, core.Game.windowHeight()),
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D sceneGraphics = sceneBuffer.createGraphics();
    try {
      sceneGraphics.setRenderingHints(g.getRenderingHints());
      renderDirect(sceneGraphics, deltaSeconds);
    } finally {
      sceneGraphics.dispose();
    }

    BufferedImage processed =
      SceneEffectPipeline.apply(sceneBuffer, Time.nowMs());
    g.drawImage(processed, 0, 0, null);
  }

  private void renderDirect(Graphics2D g, float deltaSeconds) {
    RenderContext.set(g);
    try {
      ECSManagement.renderAll(deltaSeconds);
      UiOverlayRegistry.renderAll(g);
    } finally {
      RenderContext.clear();
    }
  }
}
