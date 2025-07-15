package core.utils.components.draw;

import com.badlogic.gdx.graphics.Texture;
import core.utils.components.path.IPath;
import java.util.HashMap;

/**
 * Maps Paths to libGDX {@link Texture}s, to reduce unnecessary loading of textures.
 *
 * <p>Use {@link #instance()} to get the only instance of the {@link TextureMap}, and use {@link
 * #textureAt(IPath)} to get the texture that is stored at the given path.
 *
 * @see Painter
 */
public final class TextureMap extends HashMap<String, Texture> {
  private static final TextureMap INSTANCE = new TextureMap();

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
      put(path.pathString(), new Texture(path.pathString()));
    }

    return get(path.pathString());
  }
}
