package core.game.litiengine;

import core.game.ECSManagement;
import core.platform.litiengine.render.LitiengineGraphicsContext;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import java.awt.Graphics2D;

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

    LitiengineGraphicsContext.set(g);
    try {
      final float deltaSeconds = Game.loop().getDeltaTime() / 1000.0f;
      ECSManagement.renderAll(deltaSeconds);
    } finally {
      LitiengineGraphicsContext.clear();
    }
  }
}
