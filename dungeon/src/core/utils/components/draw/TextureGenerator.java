package core.utils.components.draw;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import core.systems.DrawSystem;
import core.utils.components.draw.shader.ShaderList;
import core.utils.components.path.SimpleIPath;

/**
 * Utility class for generating and registering textures.
 */
public class TextureGenerator {

  /**
   * Generates a solid colored texture with the given width, height and color, and registers it in
   * the TextureMap at the given path.
   *
   * @param path The path to register the texture at.
   * @param width The width of the texture.
   * @param height The height of the texture.
   * @param color The color of the texture.
   */
  public static void registerGenerateColorTexture(String path, int width, int height, Color color) {
    Texture texture = generateColorTexture(width, height, color);
    TextureMap.instance().putTexture(new core.utils.components.path.SimpleIPath(path), texture);
  }

  /**
   * Generates a solid colored texture with the given width, height and color.
   *
   * @param width The width of the texture.
   * @param height The height of the texture.
   * @param color The color of the texture.
   * @return The generated texture.
   */
  public static Texture generateColorTexture(int width, int height, Color color) {
    Color pma = color.cpy().premultiplyAlpha();
    Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    pixmap.setColor(pma);
    pixmap.fill();
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  /**
   * Renders a texture with the given shaders and registers it in the TextureMap at the given base.
   *
   * @param base The base to the base texture.
   * @param path The base to register the rendered texture at.
   * @param shaders The shaders to apply.
   */
  public static void registerRenderShaderTexture(String base, String path, ShaderList shaders){
    TextureMap.instance().putPixmap(new SimpleIPath(path), staticRenderShaderTexture(base, shaders), true);
  }

  /**
   * Renders a texture with the given shaders and returns the resulting pixmap.
   *
   * @param path The path to the base texture.
   * @param shaders The shaders to apply.
   * @return The resulting pixmap.
   */
  public static Pixmap staticRenderShaderTexture(String path, ShaderList shaders){
    Texture base = TextureMap.instance().textureAt(new SimpleIPath(path));
    TextureRegion region = new TextureRegion(base);
    FrameBuffer fbo = DrawSystem.getInstance().processShaders(region, shaders);
    fbo.begin();
    Pixmap pm = Pixmap.createFromFrameBuffer(
      0, 0,
      fbo.getWidth(),
      fbo.getHeight()
    );
    fbo.end();
    return pm;
  }

}
