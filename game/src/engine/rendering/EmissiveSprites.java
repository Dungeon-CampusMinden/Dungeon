package engine.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import engine.components.DrawComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The glowing pixels of screens and displays. A sprite whose texture path contains one of the
 * {@link #SCREENS} parts glows wherever its colour is bright and saturated, like the symbols, text
 * and status lamps on a screen; its casing and other dull colours stay lit by the scene. The
 * atmosphere renderer lights these pixels fully, so screens stay readable in the dark.
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

  // Masks by texture path; empty for textures that do not glow.
  private static final Map<String, Optional<Texture>> MASKS = new HashMap<>();

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
    return dc.currentAnimation()
        .sourcePath()
        .flatMap(path -> MASKS.computeIfAbsent(path.pathString(), EmissiveSprites::create));
  }

  private static Optional<Texture> create(String path) {
    String lower = path.toLowerCase();
    if (SCREENS.stream().noneMatch(lower::contains)) return Optional.empty();
    Pixmap source = new Pixmap(Gdx.files.internal(path));
    Pixmap mask = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
    mask.setBlending(Pixmap.Blending.None);
    Color c = new Color();
    float[] hsv = new float[3];
    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        Color.rgba8888ToColor(c, source.getPixel(x, y));
        if (c.a * 255 < OPAQUE_ALPHA) continue;
        c.toHsv(hsv);
        boolean glows = hsv[1] >= MIN_SATURATION && hsv[2] >= MIN_VALUE;
        mask.drawPixel(x, y, glows ? 0xFFFFFFFF : 0x000000FF);
      }
    }
    source.dispose();
    Texture texture = new Texture(mask);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    mask.dispose();
    return Optional.of(texture);
  }
}
