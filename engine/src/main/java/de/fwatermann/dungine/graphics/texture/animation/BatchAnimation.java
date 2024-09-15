package de.fwatermann.dungine.graphics.texture.animation;

import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.graphics.texture.TextureManager;
import de.fwatermann.dungine.resource.Resource;
import org.joml.Vector2i;

/**
 * Class representing a batch animation.
 *
 * <p>A batch animation is an animation whose frames are all contained in a single texture. Therefor
 * the frames must have equal dimensions.
 */
public class BatchAnimation extends Animation {

  private final Texture texture;
  private final int count;
  private final Direction direction;

  /**
   * Constructs a BatchAnimation with the specified texture, frame count, and direction.
   *
   * @param texture the texture used for the animation.
   * @param count the number of frames in the animation.
   * @param direction the direction of the animation.
   */
  public BatchAnimation(Texture texture, int count, Direction direction) {
    super(count);
    this.texture = texture;
    this.count = count;
    this.direction = direction;
  }

  /**
   * Constructs a BatchAnimation with the specified resource, frame count, and direction.
   *
   * @param resource the resource used to load the texture.
   * @param count the number of frames in the animation.
   * @param direction the direction of the animation.
   */
  public BatchAnimation(Resource resource, int count, Direction direction) {
    this(TextureManager.load(resource), count, direction);
  }

  @Override
  protected AnimationFrame makeFrame(int pIndex) {
    int index = pIndex % this.count;
    Vector2i size;
    if (this.direction == Direction.UP || this.direction == Direction.DOWN) {
      size = new Vector2i(this.texture.width(), this.texture.height() / this.count);
    } else {
      size = new Vector2i(this.texture.width() / this.count, this.texture.height());
    }
    Vector2i position =
        switch (this.direction) {
          case UP -> new Vector2i(0, index * size.y);
          case DOWN -> new Vector2i(0, this.texture.height() - (index + 1) * size.y);
          case LEFT -> new Vector2i(index * size.x, 0);
          case RIGHT -> new Vector2i(this.texture.width() - (index + 1) * size.x, 0);
        };
    return new AnimationFrame(this.texture, size, position);
  }

  /** Enum representing the direction of the animation. */
  public enum Direction {
    DOWN,
    UP,
    LEFT,
    RIGHT;
  }
}
