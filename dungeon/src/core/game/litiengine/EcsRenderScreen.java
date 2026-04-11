package core.game.litiengine;

import core.game.ECSManagement;
import core.platform.litiengine.render.LitiengineGraphicsContext;
import core.platform.litiengine.render.scene.LitiengineSceneEffectPipeline;
import core.platform.litiengine.ui.LitiengineUiOverlayRegistry;
import core.utils.Time;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Thin LITIENGINE screen that bridges the active Graphics2D context into the ECS render pipeline.
 *
 * <p>All actual drawing is performed by ECS render systems (e.g. LitiengineSpriteRenderSystem).
 */
public final class EcsRenderScreen extends Screen {
  public static final String NAME = "ecs-render";

  public EcsRenderScreen() {
    super(NAME);
  }

  @Override
  public void render(Graphics2D g) {
    super.render(g);

    final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;

    if (!LitiengineSceneEffectPipeline.hasEnabledEffects()) {
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
      LitiengineSceneEffectPipeline.apply(sceneBuffer, Time.nowMs());
    g.drawImage(processed, 0, 0, null);
  }

  private void renderDirect(Graphics2D g, float deltaSeconds) {
    LitiengineGraphicsContext.set(g);
    try {
      ECSManagement.renderAll(deltaSeconds);
      LitiengineUiOverlayRegistry.renderAll(g);
    } finally {
      LitiengineGraphicsContext.clear();
    }
  }
}
