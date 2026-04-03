package contrib.hud.elements;

import core.utils.components.draw.animation.Animation;

/** A button that displays an animation on top of it. */
public class ImageButton extends Button {

  private final Animation animation;

  /**
   * Create a new image button.
   *
   * @param animation The animation to display.
   * @param x The x position.
   * @param y The y position.
   * @param width The width.
   * @param height The height.
   */
  public ImageButton(final Animation animation, int x, int y, int width, int height) {
    super(x, y, width, height);
    this.animation = animation;
  }

  /**
   * Returns the animation that should be rendered on top of the button background.
   *
   * @return button icon animation
   */
  public Animation animation() {
    return this.animation;
  }
}
