package core.utils.components.draw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
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
      // We still store the string in the map to make sure we only store each Texture once.
      // SimplePath("file.png").equals(SimplePath("file.png")) would return false, and so we
      // would add it twice in the map.
      // IPath cannot override the equals method because it's an interface, and it can't be
      // called. If it could be called, then the enums could not implement it.
//      put(path.pathString(), new Texture(path.pathString()));
      put(path.pathString(), loadPMA(path.pathString()));
    }

    return get(path.pathString());
  }

  public static Texture loadPMA(String internalPath) {
    FileHandle file = Gdx.files.internal(internalPath);
    if (!file.exists()) {
      LOGGER.error("File not found: " + internalPath);
      return null;
    }

    Pixmap pixmap = new Pixmap(file);
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

          pixmap.drawPixel(x, y, rgba);
        }
      }

      return new Texture(pixmap);
    } finally {
      pixmap.dispose();
    }
  }
}
