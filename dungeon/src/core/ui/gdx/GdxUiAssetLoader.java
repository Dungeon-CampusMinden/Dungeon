package core.ui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import core.platform.Platform;
import core.utils.components.path.IPath;
import java.util.Objects;

/**
 * GDX-only helper for loading Scene2D UI assets from engine-agnostic resource paths.
 *
 * <p>This keeps direct {@code Gdx.files} usage and Texture/Pixmap creation out of contrib HUD
 * classes.
 */
public final class GdxUiAssetLoader {
  private GdxUiAssetLoader() {}

  public static Skin loadSkin(IPath skinPath) {
    return new Skin(requireInternalFile(skinPath));
  }

  public static BitmapFont loadBitmapFont(IPath fntPath, IPath pngPath) {
    return new BitmapFont(requireInternalFile(fntPath), requireInternalFile(pngPath), false);
  }

  public static Texture loadTexture(IPath texturePath) {
    return new Texture(requireInternalFile(texturePath));
  }

  /**
   * Creates a solid-color texture for simple UI backgrounds.
   *
   * <p>This is a convenience wrapper around {@link #createHorizontalStripTexture(int...)} for the
   * common case of a single UI color.
   */
  public static Texture createSolidColorTexture(Color color) {
    Objects.requireNonNull(color, "color");
    return createHorizontalStripTexture(Color.rgba8888(color));
  }

  /**
   * Creates a 1-row texture strip where each pixel column represents one RGBA8888 color.
   *
   * <p>Example: passing two colors creates a 2x1 texture that can be split into two
   * {@code TextureRegion}s for normal/hover backgrounds.
   */
  public static Texture createHorizontalStripTexture(int... rgba8888Colors) {
    if (rgba8888Colors == null || rgba8888Colors.length == 0) {
      throw new IllegalArgumentException("At least one color is required");
    }

    Pixmap pixmap = new Pixmap(rgba8888Colors.length, 1, Pixmap.Format.RGBA8888);
    try {
      for (int i = 0; i < rgba8888Colors.length; i++) {
        pixmap.drawPixel(i, 0, rgba8888Colors[i]);
      }
      return new Texture(pixmap);
    } finally {
      pixmap.dispose();
    }
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
