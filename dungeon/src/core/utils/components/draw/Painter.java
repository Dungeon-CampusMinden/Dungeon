package core.utils.components.draw;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.components.path.IPath;

/**
 * Handles drawing of sprites to a {@link SpriteBatch} with configurable options.
 *
 * <p>This class provides an easy-to-use API for rendering sprites in LibGDX. It only draws sprites
 * that are currently visible within the camera frustum to optimize performance. Textures are
 * managed via the {@link TextureMap} to avoid unnecessary reloads.
 *
 * <p>The rendering behavior can be customized through a {@link PainterConfig}, including scaling,
 * offset, tint, and other visual properties.
 *
 * <p>Typically used by the {@link core.systems.DrawSystem} and {@link core.systems.LevelSystem}.
 *
 * @see PainterConfig
 * @see TextureMap
 * @see core.systems.DrawSystem
 * @see core.systems.LevelSystem
 */
public class Painter {

  /** The SpriteBatch used for drawing all sprites. */
  private final SpriteBatch batch;

  /**
   * Creates a new Painter instance.
   *
   * @param batch the {@link SpriteBatch} to draw sprites on
   */
  public Painter(final SpriteBatch batch) {
    this.batch = batch;
  }

  /**
   * Draws a sprite at a given position with the specified configuration and rotation.
   *
   * <p>The sprite will only be drawn if its position is within the camera's frustum.
   *
   * @param position the world position where the sprite should be drawn
   * @param sprite the {@link Sprite} to draw
   * @param config the {@link PainterConfig} controlling scaling, tint, and offset
   * @param rotation rotation in degrees (clockwise)
   */
  public void draw(
      final Point position, final Sprite sprite, final PainterConfig config, float rotation) {

    // Apply offset from configuration
    Point realPos = position.translate(config.offset());

    // Only draw if visible in the camera frustum
    if (CameraSystem.isPointInFrustum(realPos)) {

      // Set sprite size and position
      sprite.setSize(config.scaling().x(), config.scaling().y());
      sprite.setPosition(realPos.x(), realPos.y());

      // Set rotation around the sprite's center
      sprite.setOriginCenter();
      sprite.setRotation(rotation);

      // Apply tint color if specified
      if (config.tintColor() != -1) {
        Color color = Color.CLEAR;
        Color.rgba8888ToColor(color, config.tintColor());
        sprite.setColor(color);
      } else {
        sprite.setColor(Color.WHITE);
      }

      // Draw the sprite
      sprite.draw(batch);
    }
  }

  /**
   * Draws a sprite at a given position with the specified configuration.
   *
   * <p>This is a convenience overload that draws without rotation (rotation defaults to 0°).
   *
   * @param position the world position where the sprite should be drawn
   * @param sprite the {@link Sprite} to draw
   * @param config the {@link PainterConfig} controlling scaling, tint, and offset
   */
  public void draw(final Point position, final Sprite sprite, final PainterConfig config) {
    draw(position, sprite, config, 0);
  }

  /**
   * Draws a texture from a path at a given position using the specified configuration.
   *
   * <p>This method automatically wraps the texture in a {@link Sprite} using {@link TextureMap} and
   * delegates to {@link #draw(Point, Sprite, PainterConfig)}.
   *
   * @param position the world position where the texture should be drawn
   * @param path the {@link IPath} identifying the texture to draw
   * @param config the {@link PainterConfig} controlling scaling, tint, and offset
   */
  public void draw(final Point position, final IPath path, final PainterConfig config) {
    draw(position, new Sprite(TextureMap.instance().textureAt(path)), config);
  }
}
