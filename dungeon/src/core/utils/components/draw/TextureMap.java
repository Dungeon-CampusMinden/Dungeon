package core.utils.components.draw;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import core.platform.Platform;
import core.utils.components.path.IPath;
import core.utils.logging.DungeonLogger;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Maps Paths to libGDX {@link Texture}s, to reduce unnecessary loading of textures.
 *
 * <p>Uses {@link Platform#resources()} for resource access (no direct Gdx.files usage).
 */
public final class TextureMap extends HashMap<String, Texture> {
  private static final TextureMap INSTANCE = new TextureMap();
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TextureMap.class);

  /** Get the instance of the TextureMap. */
  public static TextureMap instance() {
    return INSTANCE;
  }

  /**
   * Returns the texture stored at the given path; loads and caches it if missing.
   *
   * <p>Note: Texture creation still requires a libGDX rendering-capable runtime.
   */
  public Texture textureAt(final IPath path) {
    final String key = path.pathString();

    // In non-libGDX hosts (e.g. LITIENGINE), texture creation must not be attempted.
    if (!Platform.runtime().supportsGdxRendering()) {
      return null;
    }

    if (!containsKey(key)) {
      final String resolvedPath = resolveImplicitFilePath(key);
      final Texture tex = loadPMA(resolvedPath);

      // IMPORTANT: do not cache null – otherwise callers using containsKey(...) will misbehave.
      if (tex != null) {
        put(key, tex);
      } else {
        return null;
      }
    }

    return get(key);
  }

  /**
   * Puts the given texture into the map at the given path. If there is already a texture at that
   * path, it is disposed of first.
   */
  public void putTexture(final IPath path, final Texture texture) {
    if (texture == null) {
      // Don’t store null textures; keep cache clean and predictable.
      remove(path.pathString());
      return;
    }

    if (containsKey(path.pathString())) {
      Texture oldTexture = get(path.pathString());
      if (oldTexture != null) {
        oldTexture.dispose();
      }
    }
    put(path.pathString(), texture);
  }

  /**
   * Puts the given pixmap as a premultiplied alpha texture into the map at the given path.
   *
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

    try {
      Texture texture = loadPMA(toUse);
      putTexture(path, texture);
    } finally {
      // IMPORTANT: dispose only the temp pixmap we created, not the caller-owned pixmap
      if (flipY) {
        toUse.dispose();
      }
    }
  }

  /**
   * Loads a premultiplied alpha texture from the given resource path via {@link Platform#resources()}.
   *
   * <p>This removes the hard dependency on {@code Gdx.files.internal(...)} so the same code path
   * can run in non-libGDX hosts without immediate NPEs.
   */
  private static Texture loadPMA(String resourcePath) {
    // Texture creation needs a libGDX rendering backend (SpriteBatch/Texture/Pixmap pipeline).
    // If someone calls this on LITIENGINE right now, it's a programming error (render path not ported yet).
    if (!Platform.runtime().supportsGdxRendering()) {
      LOGGER.warn(
        "Tried to load texture '{}' without libGDX rendering backend. Returning null.",
        resourcePath);
      return null;
    }

    if (!Platform.resources().exists(resourcePath)) {
      LOGGER.error("File not found: {}", resourcePath);
      return null;
    }

    Pixmap pixmap = null;
    try (InputStream in = Platform.resources().open(resourcePath)) {
      byte[] bytes = in.readAllBytes();
      pixmap = new Pixmap(bytes, 0, bytes.length);
      return loadPMA(pixmap);
    } catch (IOException e) {
      LOGGER.error("Failed to read texture bytes: {}", resourcePath, e);
      return null;
    } catch (RuntimeException e) {
      // Pixmap decoding errors etc.
      LOGGER.error("Failed to decode texture: {}", resourcePath, e);
      return null;
    } finally {
      if (pixmap != null) {
        pixmap.dispose();
      }
    }
  }

  /**
   * Converts the given pixmap to premultiplied alpha and returns it as a texture.
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

  /**
   * Matches the "folder name implies png" convention:
   * - "character/wizard/" -> "character/wizard/wizard.png"
   * - "character/wizard"  -> "character/wizard/wizard.png"
   * - "foo.png"           -> "foo.png"
   */
  private static String resolveImplicitFilePath(String pathString) {
    if (pathString == null || pathString.isEmpty()) {
      return pathString;
    }

    // Already explicit image file
    if (pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      return pathString;
    }

    // Folder or implicit base name
    String dir = pathString.replaceAll("/$", "");
    String baseName = dir.substring(dir.lastIndexOf('/') + 1);
    return dir + "/" + baseName + ".png";
  }
}
