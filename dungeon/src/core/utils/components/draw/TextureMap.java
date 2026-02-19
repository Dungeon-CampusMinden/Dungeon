package core.utils.components.draw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import core.utils.components.path.IPath;
import core.utils.logging.DungeonLogger;
import java.util.HashMap;

/**
 * Maps Paths to libGDX {@link Texture}s, to reduce unnecessary loading of textures.
 *
 * <p>Use {@link #instance()} to get the only instance of the {@link TextureMap}, and use {@link
 * #textureAt(IPath)} to get the texture that is stored at the given path.
 */
public final class TextureMap extends HashMap<String, Texture> {
  private static final TextureMap INSTANCE = new TextureMap();
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TextureMap.class);

  /**
   * Get the instance of the TextureMap.
   *
   * @return The only instance of the TextureMap.
   */
  public static TextureMap instance() {
    return INSTANCE;
  }

  /**
   * Searches the HashMap for the matching texture and returns it. If the texture is not stored in
   * the HashMap, it is created and saved.
   *
   * @param path Path to the texture.
   * @return The Texture at the given path.
   */
  public Texture textureAt(final IPath path) {
    if (!containsKey(path.pathString())) {
      put(path.pathString(), loadPMA(path.pathString()));
    }

    return get(path.pathString());
  }

  /**
   * Puts the given texture into the map at the given path. If there is already a texture at that
   * path, it is disposed of first.
   *
   * @param path Path to store the texture at.
   * @param texture The texture to store. NOTE: Must be a premultiplied alpha texture.
   */
  public void putTexture(final IPath path, final Texture texture) {
    if (containsKey(path.pathString())) {
      Texture oldTexture = get(path.pathString());
      oldTexture.dispose();
    }
    put(path.pathString(), texture);
  }

  /**
   * Puts the given pixmap as a premultiplied alpha texture into the map at the given path. If there
   * is already a texture at that path, it is disposed of first.
   *
   * @param path Path to store the texture at.
   * @param pixmap The pixmap to convert and store.
   * @param flipY Whether to flip the pixmap vertically when creating the texture.
   */
  public void putPixmap(final IPath path, final Pixmap pixmap, boolean flipY) {
    Pixmap toUse = pixmap;
    if (flipY) {
      toUse = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());
      for (int y = 0; y < pixmap.getHeight(); y++) {
        for (int x = 0; x < pixmap.getWidth(); x++) {
          int pixel = pixmap.getPixel(x, y);
          toUse.drawPixel(x, pixmap.getHeight() - y - 1, pixel);
        }
      }
    }
    Texture texture = loadPMA(toUse);
    toUse.dispose();
    putTexture(path, texture);
  }

  /**
   * Loads a premultiplied alpha texture from the given internal path.
   *
   * @param internalPath The internal path to the texture file.
   * @return The loaded Texture with premultiplied alpha, or null if the file was not found.
   */
  private static Texture loadPMA(String internalPath) {
    FileHandle file = Gdx.files.internal(internalPath);
    if (!file.exists()) {
      LOGGER.error("File not found: " + internalPath);
      return null;
    }

    Pixmap pixmap = new Pixmap(file);
    try {
      return loadPMA(pixmap);
    } finally {
      pixmap.dispose();
    }
  }

  /**
   * Converts the given pixmap to premultiplied alpha and returns it as a texture.
   *
   * @param pixmap The pixmap to convert.
   * @return The converted texture.
   */
  private static Texture loadPMA(Pixmap pixmap) {
    Pixmap corrected = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());
    Color c = new Color();
    try {
      for (int y = 0; y < pixmap.getHeight(); y++) {
        for (int x = 0; x < pixmap.getWidth(); x++) {
          int colorInt = pixmap.getPixel(x, y);

          Color.rgba8888ToColor(c, colorInt);

          c.r *= c.a;
          c.g *= c.a;
          c.b *= c.a;
          int rgba = Color.rgba8888(c);

          corrected.drawPixel(x, y, rgba);
        }
      }

      return new Texture(corrected);
    } finally {
      corrected.dispose();
    }
  }
}
