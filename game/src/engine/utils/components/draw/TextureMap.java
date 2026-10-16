package engine.utils.components.draw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLogger;
import java.util.BitSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Maps Paths to libGDX {@link Texture}s, to reduce unnecessary loading of textures.
 *
 * <p>Use {@link #instance()} to get the only instance of the {@link TextureMap}, and use {@link
 * #textureAt(IPath)} to get the texture that is stored at the given path.
 */
public final class TextureMap extends HashMap<String, Texture> {
  private static final TextureMap INSTANCE = new TextureMap();
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TextureMap.class);
  private static final int OPAQUE_ALPHA = 128;

  // Opaque pixels of every texture this map built from pixels, recorded while converting to PMA.
  private final Map<Texture, BitSet> opaquePixels = new IdentityHashMap<>();

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
      Texture loadedTexture = loadPMA(path.pathString());
      if (loadedTexture != null) {
        put(path.pathString(), loadedTexture);
      }
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
      if (oldTexture != null) {
        opaquePixels.remove(oldTexture);
        oldTexture.dispose();
      }
    }
    put(path.pathString(), texture);
  }

  @Override
  public Texture remove(Object path) {
    Texture removed = super.remove(path);
    if (removed != null) opaquePixels.remove(removed);
    return removed;
  }

  /**
   * Returns which pixels of a texture are at least half opaque. Only textures this map created from
   * an image file or pixmap have this information; textures handed in through {@link
   * #putTexture(IPath, Texture)} do not.
   *
   * @param texture a texture stored in this map
   * @return the opaque pixels, row-major from the image's top row with one bit per pixel, or empty
   */
  public Optional<BitSet> opaquePixels(Texture texture) {
    return Optional.ofNullable(opaquePixels.get(texture));
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
   * Creates an independent copy of the texture at the given path by rendering it into a
   * framebuffer. The returned texture has its own GL handle and can have different filter settings
   * without affecting the original.
   *
   * @param path The path of the texture in this map.
   * @return A new Texture containing the same image data, wrapped in a Y-corrected TextureRegion.
   */
  public TextureRegion cloneTexture(String path) {
    Texture original = textureAt(new SimpleIPath(path));
    int w = original.getWidth();
    int h = original.getHeight();

    FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
    SpriteBatch batch = new SpriteBatch();

    try {
      batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
      fbo.begin();
      batch.begin();
      batch.draw(original, 0, 0, w, h);
      batch.end();
      TextureRegion region = new TextureRegion(fbo.getColorBufferTexture());
      region.flip(false, true);
      return region;
    } finally {
      fbo.end();
      batch.dispose();
    }
  }

  /**
   * Loads a premultiplied alpha texture from the given internal path.
   *
   * @param internalPath The internal path to the texture file.
   * @return The loaded Texture with premultiplied alpha, or null if the file was not found.
   */
  private Texture loadPMA(String internalPath) {
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
  private Texture loadPMA(Pixmap pixmap) {
    Pixmap corrected = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());
    BitSet opaque = new BitSet(pixmap.getWidth() * pixmap.getHeight());
    Color c = new Color();
    try {
      for (int y = 0; y < pixmap.getHeight(); y++) {
        for (int x = 0; x < pixmap.getWidth(); x++) {
          int colorInt = pixmap.getPixel(x, y);
          if ((colorInt & 0xff) >= OPAQUE_ALPHA) opaque.set(y * pixmap.getWidth() + x);

          Color.rgba8888ToColor(c, colorInt);

          c.r *= c.a;
          c.g *= c.a;
          c.b *= c.a;
          int rgba = Color.rgba8888(c);

          corrected.drawPixel(x, y, rgba);
        }
      }

      Texture texture = new Texture(corrected);
      opaquePixels.put(texture, opaque);
      return texture;
    } finally {
      corrected.dispose();
    }
  }
}
