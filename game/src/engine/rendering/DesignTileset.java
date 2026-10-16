package engine.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import engine.level.utils.DesignLabel;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.IPath;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * What {@link LevelRenderer} draws for a design, derived from the textures in the design's
 * directory rather than from code, so a design gains a feature by adding its texture:
 *
 * <ul>
 *   <li>{@code wall/walls.png}, the wall autotile atlas: 12x4 cells of 16x16 pixels in the Godot
 *       "3x3 minimal" blob layout. Every design has one.
 *   <li>{@code floor/variants.png}, optional floor variants: cells 0 to 7 are floors, cell 8 a
 *       skull on the floor and cell 12 the broken edge of a floor above the void. With them, floors
 *       pick a stable random variant, walls stand on floor and the engine's tint fades, since the
 *       floor already shows its seams.
 *   <li>{@code wall/decor.png}, {@code wall/torch.png} and {@code wall/fountain.png}, optional wall
 *       decoration for levels whose atmosphere asks for it.
 * </ul>
 *
 * <p>Where the floor's wall shadow begins is measured from the opaque pixels of each atlas cell, so
 * the shadow touches the wall in every art style.
 *
 * @param walls the wall autotile atlas
 * @param floorVariants the floor variants, if the design has them
 * @param decor whether the design has wall decoration
 * @param insets per atlas cell the transparent columns on its left and right and the transparent
 *     rows at its bottom, in tiles: {@code [cell][0..2]}
 */
record DesignTileset(IPath walls, Optional<IPath> floorVariants, boolean decor, float[][] insets) {

  private static final int CELLS = 48;
  private static final Map<DesignLabel, DesignTileset> TILESETS = new EnumMap<>(DesignLabel.class);

  /**
   * Returns the tileset of a design, built on first use.
   *
   * @param design the design
   * @return the tileset
   */
  static DesignTileset of(DesignLabel design) {
    DesignTileset tileset = TILESETS.get(design);
    if (tileset == null) {
      tileset = load(design);
      TILESETS.put(design, tileset);
    }
    return tileset;
  }

  /**
   * Transparent columns on the left of a wall cell, in tiles.
   *
   * @param cell the atlas cell
   * @return the inset
   */
  float leftInset(int cell) {
    return insets[cell][0];
  }

  /**
   * Transparent columns on the right of a wall cell, in tiles.
   *
   * @param cell the atlas cell
   * @return the inset
   */
  float rightInset(int cell) {
    return insets[cell][1];
  }

  /**
   * Transparent rows at the bottom of a wall cell, in tiles.
   *
   * @param cell the atlas cell
   * @return the inset
   */
  float bottomInset(int cell) {
    return insets[cell][2];
  }

  private static DesignTileset load(DesignLabel design) {
    IPath walls = TileTextures.wallAtlas(design);
    IPath variants = TileTextures.path(design, "floor/variants.png");
    boolean decor =
        exists(TileTextures.path(design, "wall/decor.png"))
            && exists(TileTextures.path(design, "wall/torch.png"))
            && exists(TileTextures.path(design, "wall/fountain.png"));
    return new DesignTileset(
        walls,
        exists(variants) ? Optional.of(variants) : Optional.empty(),
        decor,
        measureInsets(walls));
  }

  private static boolean exists(IPath path) {
    return Gdx.files.internal(path.pathString()).exists();
  }

  private static float[][] measureInsets(IPath walls) {
    float[][] insets = new float[CELLS][3];
    Texture texture = TextureMap.instance().textureAt(walls);
    Optional<BitSet> opaque = TextureMap.instance().opaquePixels(texture);
    if (opaque.isEmpty()) return insets;
    int width = texture.getWidth();
    for (int cell = 0; cell < CELLS; cell++) {
      int x0 = (cell % 12) * 16;
      int y0 = (cell / 12) * 16;
      int left = 16;
      int right = -1;
      int bottom = -1;
      for (int y = 0; y < 16; y++) {
        for (int x = 0; x < 16; x++) {
          if (!opaque.get().get((y0 + y) * width + x0 + x)) continue;
          left = Math.min(left, x);
          right = Math.max(right, x);
          bottom = Math.max(bottom, y);
        }
      }
      if (right < 0) continue; // an empty cell casts no shadow
      insets[cell][0] = left / 16f;
      insets[cell][1] = (15 - right) / 16f;
      insets[cell][2] = (15 - bottom) / 16f;
    }
    return insets;
  }
}
