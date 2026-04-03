package contrib.platform.gdx.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import contrib.hud.elements.Button;
import contrib.hud.elements.ImageButton;
import core.Game;
import core.platform.gdx.render.GdxAnimationFrames;
import core.platform.gdx.render.TextureMap;

/**
 * GDX-specific renderer for backend-neutral HUD buttons.
 *
 * <p>This class owns all libGDX drawing for {@link Button} and optional {@link ImageButton}
 * overlays so that the button model itself remains engine-neutral.
 */
public final class GdxButtonRenderer {

  private static final int IMAGE_BUTTON_PADDING = 15;

  private GdxButtonRenderer() {}

  /**
   * Draws the given button using the libGDX batch pipeline.
   *
   * @param batch target batch
   * @param button button to draw
   */
  public static void draw(Batch batch, Button button) {
    if (batch == null || button == null || Game.isHeadless()) {
      return;
    }

    button.updateFromStage();
    drawBackground(batch, button);

    if (button instanceof ImageButton imageButton) {
      drawImageOverlay(batch, imageButton);
    }
  }

  private static void drawBackground(Batch batch, Button button) {
    batch.draw(
      TextureMap.instance().textureAt(button.backgroundTexturePath()),
      button.x(),
      button.y(),
      button.width(),
      button.height());
  }

  private static void drawImageOverlay(Batch batch, ImageButton button) {
    Sprite nextFrame = GdxAnimationFrames.toSprite(button.animation().update());

    float aspectRatio = nextFrame.getWidth() / nextFrame.getHeight();
    int width = button.width() - 2 * IMAGE_BUTTON_PADDING;
    int height = (int) (width / aspectRatio);

    if (height > button.height() * 0.8f) {
      height = button.height() - 2 * IMAGE_BUTTON_PADDING;
      width = (int) (height * aspectRatio);
    }

    int x = button.x() + (button.width() / 2) - (width / 2);
    int y = button.y() + (button.height() / 2) - (height / 2);
    batch.draw(nextFrame, x, y, width, height);
  }
}
