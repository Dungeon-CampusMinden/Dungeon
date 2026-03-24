package core.ui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import core.platform.Platform;
import core.utils.components.path.IPath;
import java.util.Objects;

/**
 * GDX-only helper for loading Scene2D UI assets from engine-agnostic resource paths.
 *
 * <p>This keeps direct {@code Gdx.files} usage out of contrib HUD classes.
 */
public final class GdxUiAssetLoader {
  private GdxUiAssetLoader() {}

  public static Skin loadSkin(IPath skinPath) {
    return new Skin(requireInternalFile(skinPath));
  }

  public static BitmapFont loadBitmapFont(IPath fntPath, IPath pngPath) {
    return new BitmapFont(requireInternalFile(fntPath), requireInternalFile(pngPath), false);
  }

  public static FileHandle requireInternalFile(IPath path) {
    Objects.requireNonNull(path, "path");
    String raw = path.pathString();

    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("UI resource path must not be blank");
    }

    if (!Platform.resources().exists(raw)) {
      throw new IllegalStateException("UI resource not found: " + raw);
    }

    if (Gdx.files == null) {
      throw new IllegalStateException(
        "libGDX file backend not available for UI resource: " + raw);
    }

    return Gdx.files.internal(raw);
  }
}
