package engine.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import engine.components.DrawComponent;
import engine.utils.components.path.IPath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The glowing pixels of screens and displays. A sprite whose texture path contains one of the
 * {@link #SCREENS} parts glows wherever its colour is bright and saturated, like the symbols, text
 * and status lamps on a screen; its casing and other dull colours stay lit by the scene. The
 * atmosphere renderer shows these pixels in their own colour whatever the light, and the screen
 * casts light in the colour it currently shows.
 */
public final class EmissiveSprites {

  private static final List<String> SCREENS =
      List.of(
          "objects/tech/screen_",
          "objects/tech/computer_",
          "objects/tech/hand_scanner",
          "objects/tech/digital_archive_shelf",
          "desk_with_pc");
  private static final float MIN_SATURATION = 0.45f;
  private static final float MIN_VALUE = 0.35f;
  private static final int OPAQUE_ALPHA = 128;

  // Glow data by texture path; empty for textures that do not glow.
  private static final Map<String, Optional<Glow>> GLOWS = new HashMap<>();

  /**
   * The glow of one texture.
   *
   * @param mask the glow mask, see {@link #mask}
   * @param colors the texture's colours (RGBA8888) where it glows, 0 elsewhere, row by row
   * @param width the texture's width in pixels
   * @param lightColors the light colour of each region of the texture shown so far
   */
  private record Glow(
      Texture mask, int[] colors, int width, Map<Long, Optional<Color>> lightColors) {}

  private EmissiveSprites() {}

  /**
   * Returns the glow mask of the texture an entity shows: white where the texture glows, black
   * elsewhere, with the texture's coverage as alpha. It matches the texture pixel for pixel, so it
   * can be drawn with the sprite's region and transform.
   *
   * @param dc the entity's draw component
   * @return the mask, premultiplied, or empty if the texture does not glow
   */
  public static Optional<Texture> mask(DrawComponent dc) {
    return glow(dc).map(Glow::mask);
  }

  /**
   * Returns the colour of the light an entity's screen casts: the average colour of the glowing
   * pixels of the frame it shows, at full brightness.
   *
   * @param dc the entity's draw component
   * @return the light colour, or empty if the frame does not glow
   */
  public static Optional<Color> lightColor(DrawComponent dc) {
    Optional<Glow> glow = glow(dc);
    if (glow.isEmpty()) return Optional.empty();
    Sprite sprite = dc.getSprite();
    long region =
        (long) sprite.getRegionX() << 48
            | (long) sprite.getRegionY() << 32
            | (long) sprite.getRegionWidth() << 16
            | sprite.getRegionHeight();
    return glow.get().lightColors().computeIfAbsent(region, r -> average(glow.get(), sprite));
  }

  private static Optional<Glow> glow(DrawComponent dc) {
    return dc.currentAnimation()
        .sourcePath()
        .flatMap(path -> GLOWS.computeIfAbsent(path.pathString(), EmissiveSprites::create));
  }

  private static Optional<Color> average(Glow glow, Sprite sprite) {
    float r = 0;
    float g = 0;
    float b = 0;
    int count = 0;
    Color c = new Color();
    for (int y = sprite.getRegionY(); y < sprite.getRegionY() + sprite.getRegionHeight(); y++) {
      for (int x = sprite.getRegionX(); x < sprite.getRegionX() + sprite.getRegionWidth(); x++) {
        int rgba = glow.colors()[y * glow.width() + x];
        if (rgba == 0) continue;
        Color.rgba8888ToColor(c, rgba);
        r += c.r;
        g += c.g;
        b += c.b;
        count++;
      }
    }
    if (count == 0) return Optional.empty();
    float max = Math.max(r, Math.max(g, b));
    return Optional.of(new Color(r / max, g / max, b / max, 1f));
  }

  /**
   * Makes a generated texture glow like a screen, such as a display drawn at runtime. Call it again
   * whenever the texture changes.
   *
   * @param path the path the texture is stored under in the texture map
   * @param pixels the texture's pixels; not disposed
   */
  public static void glow(IPath path, Pixmap pixels) {
    Optional<Glow> old = GLOWS.put(path.pathString(), Optional.of(glow(pixels)));
    if (old != null) old.ifPresent(g -> g.mask().dispose());
  }

  private static Optional<Glow> create(String path) {
    String lower = path.toLowerCase();
    if (SCREENS.stream().noneMatch(lower::contains)) return Optional.empty();
    Pixmap source = new Pixmap(Gdx.files.internal(path));
    Glow glow = glow(source);
    source.dispose();
    return Optional.of(glow);
  }

  private static Glow glow(Pixmap source) {
    Pixmap mask = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
    mask.setBlending(Pixmap.Blending.None);
    int[] colors = new int[source.getWidth() * source.getHeight()];
    Color c = new Color();
    float[] hsv = new float[3];
    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        int rgba = source.getPixel(x, y);
        Color.rgba8888ToColor(c, rgba);
        if (c.a * 255 < OPAQUE_ALPHA) continue;
        c.toHsv(hsv);
        boolean glows = hsv[1] >= MIN_SATURATION && hsv[2] >= MIN_VALUE;
        mask.drawPixel(x, y, glows ? 0xFFFFFFFF : 0x000000FF);
        if (glows) colors[y * source.getWidth() + x] = rgba;
      }
    }
    Texture texture = new Texture(mask);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    mask.dispose();
    return new Glow(texture, colors, source.getWidth(), new HashMap<>());
  }
}
