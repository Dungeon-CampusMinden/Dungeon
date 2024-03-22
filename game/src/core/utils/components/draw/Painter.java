package core.utils.components.draw;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.components.path.IPath;

/**
 * Draws the sprites on the batch.
 *
 * <p>This class is a custom, easy-to-use API for LibGDX to draw systems.
 *
 * <p>Use {@link #draw(Point, IPath, PainterConfig)} to draw a sprite on the screen.
 *
 * <p>The Painter will only draw sprites that are currently visible on the camera. The Painter uses
 * the {@link TextureMap} to store already loaded textures to save performance and storage.
 *
 * <p>Use the {@link PainterConfig} to configure the painting options.
 *
 * <p>The Painter is used by the {@link core.systems.DrawSystem} and {@link
 * core.systems.LevelSystem}.
 *
 * @see PainterConfig
 * @see core.systems.DrawSystem
 * @see core.systems.LevelSystem
 */
public class Painter {
  private final SpriteBatch batch;

  /**
   * Create a new Painter.
   *
   * @param batch The {@link SpriteBatch} on that this painter will draw the sprites.
   */
  public Painter(final SpriteBatch batch) {
    this.batch = batch;
  }

  /**
   * Draw the given texture on the given point with the given configuration.
   *
   * <p>Will only draw the texture if it's in the frustum of the camera.
   *
   * @param position Position of the texture in the game world.
   * @param texturePath Path to the texture to draw.
   * @param config Painting configuration.
   */
  public void draw(final Point position, final IPath texturePath, final PainterConfig config) {
    float realX = position.x + config.xOffset(); // including the drawOffset
    float realY = position.y + config.yOffset(); // including the drawOffset
    if (CameraSystem.isPointInFrustum(realX, realY)) {
      Sprite sprite = new Sprite(TextureMap.instance().textureAt(texturePath));
      // set up scaling of textures
      sprite.setSize(config.xScaling(), config.yScaling());
      // where to draw the sprite
      sprite.setPosition(realX, realY);

      // need to be called before drawing
      batch.begin();

      // tint the sprite
      if (config.tintColor() != -1) {
        Color color = Color.CLEAR;
        Color.rgba8888ToColor(color, config.tintColor());
        sprite.setColor(color);
      }
      // draw sprite
      sprite.draw(batch);
      // need to be called after drawing
      batch.end();
    }
  }
}
