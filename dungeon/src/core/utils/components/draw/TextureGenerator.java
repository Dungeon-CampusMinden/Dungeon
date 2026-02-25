package core.utils.components.draw;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import core.platform.Platform;
import core.utils.components.draw.shader.ShaderList;
import core.utils.components.path.SimpleIPath;
import java.lang.reflect.Method;

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

  /**
    * Renders a texture using the GDX shader renderer and registers it in the TextureMap at the given
    * path. The base parameter is used for caching purposes in the shader renderer, and should be a
    * unique identifier for the texture being rendered (e.g. a file path or a descriptive name). The path
    * parameter is the path to register the generated texture at in the TextureMap. The shaders parameter
    * is the list of shaders to apply when rendering the texture.
    *
    * @param base A unique identifier for caching the rendered texture (e.g. a file path or descriptive name).
    * @param path The path to register the generated texture at in the TextureMap.
    * @param shaders The list of shaders to apply when rendering the texture.
   */
  public static void registerRenderShaderTexture(String base, String path, ShaderList shaders) {
    if (!Platform.runtime().supportsGdxRendering()) return;

    Pixmap pm = staticRenderShaderTexture(base, shaders);
    if (pm == null) return;

    try {
      TextureMap.instance().putPixmap(new SimpleIPath(path), pm, true);
    } finally {
      pm.dispose();
    }
  }

  /**
   * Renders a texture using the GDX shader renderer and returns it as a Pixmap. The base parameter is
   * used for caching purposes in the shader renderer, and should be a unique identifier for the
   * texture being rendered (e.g. a file path or a descriptive name). The shaders parameter is the
   * list of shaders to apply when rendering the texture.
   *
   * @param path A unique identifier for caching the rendered texture (e.g. a file path or descriptive name).
   * @param shaders The list of shaders to apply when rendering the texture.
   * @return The rendered texture as a Pixmap, or null if rendering is not supported.
   */
  public static Pixmap staticRenderShaderTexture(String path, ShaderList shaders) {
    if (!Platform.runtime().supportsGdxRendering()) return null;
    return invokeGdxRenderer(path, shaders);
  }

  private static Pixmap invokeGdxRenderer(String path, ShaderList shaders) {
    try {
      Class<?> cls = Class.forName("core.platform.gdx.render.GdxShaderTextureRenderer");
      Method m = cls.getMethod("renderToPixmap", String.class, ShaderList.class);
      return (Pixmap) m.invoke(null, path, shaders);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("GDX shader renderer not available", e);
    }
  }
}
