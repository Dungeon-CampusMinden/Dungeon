package core.utils.components.draw;

import com.badlogic.gdx.graphics.Texture;
import core.utils.AssetPathUtils;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Maps Paths to libGDX {@link Texture}s, to reduce unnecessary loading of textures.
 *
 * <p>Use {@link #instance()} to get the only instance of the {@link TextureMap}, and use {@link
 * #textureAt(IPath)} to get the texture that is stored at the given path.
 *
 * @see Painter
 */
public final class TextureMap extends HashMap<String, Texture> {

  private final Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());
  private static final TextureMap INSTANCE = new TextureMap();
  private static final IPath MISSING_TEXTURE =
      AssetPathUtils.completeAssetPath(new SimpleIPath("missing_texture.png"))
          .orElseThrow(() -> new NullPointerException("Can't load Texture: missing_texture"));

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
   * <p>Note: This function uses {@link AssetPathUtils#completeAssetPath(IPath)} to complete the
   * given path if it's not already complete.
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

      Optional<IPath> completePath = AssetPathUtils.completeAssetPath(path);
      if (completePath.isPresent())
        put(path.pathString(), new Texture(completePath.get().pathString()));
      else {
        LOGGER.warning(
            "Could not find Texture at "
                + path.pathString()
                + ". Make sure the given Path is correct. Will use Missing Texture as a replacement.");

        put(path.pathString(), new Texture(MISSING_TEXTURE.pathString()));
      }
    }
    return get(path.pathString());
  }
}
