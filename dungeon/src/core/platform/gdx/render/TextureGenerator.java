package core.platform.gdx.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import core.platform.gdx.render.shader.ShaderList;

/** Utility class for generating and registering textures. */
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

  public static void registerRenderShaderTexture(String base, String path, ShaderList shaders) {
    // Legacy libGDX shader texture baking is disabled on the LITIENGINE migration path.
  }

  public static Pixmap staticRenderShaderTexture(String path, ShaderList shaders) {
    return null;
  }
}
