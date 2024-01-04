package contrib.hud.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import core.utils.components.draw.Animation;
import core.utils.components.draw.TextureMap;

/** A button that displays an animation on top of it. */
public class ImageButton extends Button {

  private static final int PADDING = 15;

  private final Animation animation;

  /**
   * Create a new image button.
   *
   * @param parent The parent GUI.
   * @param animation The animation to display.
   * @param x The x position.
   * @param y The y position.
   * @param width The width.
   * @param height The height.
   */
  public ImageButton(
      final CombinableGUI parent, final Animation animation, int x, int y, int width, int height) {
    super(parent, x, y, width, height);
    this.animation = animation;
  }

  @Override
  public void draw(final Batch batch) {
    super.draw(batch);
    Texture nextFrame = TextureMap.instance().textureAt(this.animation.nextAnimationTexturePath());
    float aspectRatio = nextFrame.getWidth() / (float) nextFrame.getHeight();
    int width = this.width - 2 * PADDING;
    int height = (int) (width / aspectRatio);
    if (height > this.height * 0.8f) {
      height = this.height - 2 * PADDING;
      width = (int) (height * aspectRatio);
    }
    int x = this.x() + (this.width / 2) - (width / 2);
    int y = this.y() + (this.height / 2) - (height / 2);
    batch.draw(
        TextureMap.instance().textureAt(this.animation.nextAnimationTexturePath()),
        x,
        y,
        width,
        height);
  }
}
