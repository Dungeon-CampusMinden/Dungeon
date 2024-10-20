package de.fwatermann.dungine.graphics.texture.animation;

import de.fwatermann.dungine.graphics.texture.Texture;
import org.joml.Vector2i;

/**
 * The `AnimationFrame` class represents a single frame in an animation.
 * It contains a texture, size, and position.
 */
public class AnimationFrame {

  private final Texture texture;
  private final Vector2i size;
  private final Vector2i position;

  /**
   * Constructs an `AnimationFrame` with the specified texture, size, and position.
   *
   * @param texture the texture of the animation frame.
   * @param size the size of the animation frame.
   * @param position the position of the animation frame.
   */
  public AnimationFrame(Texture texture, Vector2i size, Vector2i position) {
    this.texture = texture;
    this.size = size;
    this.position = position;
  }

  /**
   * Gets the texture of the animation frame.
   *
   * @return the texture of the animation frame.
   */
  public Texture texture() {
    return this.texture;
  }

  /**
   * Gets the size of the animation frame.
   *
   * @return the size of the animation frame.
   */
  public Vector2i size() {
    return this.size;
  }

  /**
   * Gets the position of the animation frame.
   *
   * @return the position of the animation frame.
   */
  public Vector2i position() {
    return this.position;
  }
}
