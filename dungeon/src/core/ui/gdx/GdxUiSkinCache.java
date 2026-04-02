package core.ui.gdx;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * Lazily loads and caches the default libGDX Scene2D skin.
 *
 * <p>This class is intentionally GDX-specific because {@link Skin} is part of the libGDX
 * Scene2D UI toolkit and must not leak through generic HUD utility classes.
 */
public final class GdxUiSkinCache {

  private static final IPath DEFAULT_SKIN_PATH = new SimpleIPath("skin/uiskin.json");

  private static Skin defaultSkin;

  private GdxUiSkinCache() {}

  /**
   * Returns the default Scene2D skin for the libGDX backend.
   *
   * @return cached default skin
   * @throws IllegalStateException if the skin cannot be loaded
   */
  public static Skin defaultSkin() {
    if (defaultSkin == null) {
      try {
        defaultSkin = GdxUiAssetLoader.loadSkin(DEFAULT_SKIN_PATH);
      } catch (RuntimeException e) {
        throw new IllegalStateException(
          "Could not load default skin. Are you running without the libGDX UI backend?", e);
      }
    }

    return defaultSkin;
  }
}
