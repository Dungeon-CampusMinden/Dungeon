package engine.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import engine.level.utils.DesignLabel;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.IPath;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What {@link LevelRenderer} draws for a design, derived from the textures in the design's
 * directory rather than from code, so a design gains a feature by adding its texture:
 *
 * <ul>
 *   <li>{@code wall/walls.png}, the wall autotile atlas: 12x4 cells of 16x16 pixels in the Godot
 *       "3x3 minimal" blob layout. Every design has one. The inside of a wall mass is transparent,
 *       so it shows the void.
 *   <li>{@code floor/variants.png}, optional floor variants: cells 0 to 7 are floors and cell 8 a
 *       skull on the floor. With them, floors pick a stable random variant, walls stand on floor
 *       where their art leaves room for the ground beside them, and the engine's tint fades, since
 *       the floor already shows its seams.
 *   <li>{@code wall/decor.png}, {@code wall/torch.png} and {@code wall/fountain.png}, optional wall
 *       decoration for levels whose atmosphere asks for it.
 * </ul>
 *
 * <p>Where the floor's wall shadow begins is measured from the opaque pixels of each atlas cell, so
 * the shadow touches the wall in every art style. The ground beside a wall is the transparent part
 * of its cell that reaches a side or corner the cell leaves open; the rest is the inside of the
 * wall mass.
 *
 * @param walls the wall autotile atlas
 * @param floorVariants the floor variants, if the design has them
 * @param decor whether the design has wall decoration
 * @param insets per atlas cell the transparent columns on its left and right and the transparent
 *     rows at its bottom, in tiles: {@code [cell][0..2]}
 * @param floorRects per atlas cell the ground beside the wall, see {@link #floorRects(int)}
 */
record DesignTileset(
    IPath walls,
    Optional<IPath> floorVariants,
    boolean decor,
    float[][] insets,
    int[][] floorRects) {

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

  /**
   * The ground beside the wall art of a cell, as rectangles in pixels from the cell's top left: x,
   * y, width and height, four values per rectangle.
   *
   * @param cell the atlas cell
   * @return the rectangles, empty if the art covers the whole cell
   */
  int[] floorRects(int cell) {
    return floorRects[cell];
  }

  private static DesignTileset load(DesignLabel design) {
    IPath walls = TileTextures.wallAtlas(design);
    IPath variants = TileTextures.path(design, "floor/variants.png");
    boolean decor =
        exists(TileTextures.path(design, "wall/decor.png"))
            && exists(TileTextures.path(design, "wall/torch.png"))
            && exists(TileTextures.path(design, "wall/fountain.png"));
    Texture texture = TextureMap.instance().textureAt(walls);
    BitSet opaque = TextureMap.instance().opaquePixels(texture).orElseGet(BitSet::new);
    int width = texture.getWidth();
    return new DesignTileset(
        walls,
        exists(variants) ? Optional.of(variants) : Optional.empty(),
        decor,
        measureInsets(opaque, width),
        measureFloors(opaque, width));
  }

  private static boolean exists(IPath path) {
    return Gdx.files.internal(path.pathString()).exists();
  }

  private static float[][] measureInsets(BitSet opaque, int width) {
    float[][] insets = new float[CELLS][3];
    for (int cell = 0; cell < CELLS; cell++) {
      int x0 = (cell % 12) * 16;
      int y0 = (cell / 12) * 16;
      int left = 16;
      int right = -1;
      int bottom = -1;
      for (int y = 0; y < 16; y++) {
        for (int x = 0; x < 16; x++) {
          if (!opaque.get((y0 + y) * width + x0 + x)) continue;
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

  // Fills the transparent pixels of each cell from the border pixels whose side or corner the cell
  // leaves open, then splits the filled area into rectangles.
  private static int[][] measureFloors(BitSet opaque, int width) {
    int[][] rects = new int[CELLS][];
    for (int cell = 0; cell < CELLS; cell++) {
      int mask = Math.max(LevelRenderer.ATLAS_MASKS[cell], 0);
      int x0 = (cell % 12) * 16;
      int y0 = (cell / 12) * 16;
      boolean[] ground = new boolean[256];
      ArrayDeque<Integer> open = new ArrayDeque<>();
      for (int i = 0; i < 16; i++) {
        if ((mask & LevelRenderer.N) == 0) open.add(i);
        if ((mask & LevelRenderer.S) == 0) open.add(15 * 16 + i);
        if ((mask & LevelRenderer.W) == 0) open.add(i * 16);
        if ((mask & LevelRenderer.E) == 0) open.add(i * 16 + 15);
      }
      if ((mask & LevelRenderer.NW) == 0) open.add(0);
      if ((mask & LevelRenderer.NE) == 0) open.add(15);
      if ((mask & LevelRenderer.SW) == 0) open.add(15 * 16);
      if ((mask & LevelRenderer.SE) == 0) open.add(255);
      while (!open.isEmpty()) {
        int p = open.poll();
        int x = p % 16;
        int y = p / 16;
        if (ground[p] || opaque.get((y0 + y) * width + x0 + x)) continue;
        ground[p] = true;
        if (x > 0) open.add(p - 1);
        if (x < 15) open.add(p + 1);
        if (y > 0) open.add(p - 16);
        if (y < 15) open.add(p + 16);
      }
      rects[cell] = rectangles(ground);
    }
    return rects;
  }

  // Runs of ground in each row, merged with the same run in the rows below.
  private static int[] rectangles(boolean[] ground) {
    boolean[] taken = new boolean[256];
    List<Integer> out = new ArrayList<>();
    for (int y = 0; y < 16; y++) {
      for (int x = 0; x < 16; x++) {
        if (!ground[y * 16 + x] || taken[y * 16 + x]) continue;
        int w = 1;
        while (x + w < 16 && ground[y * 16 + x + w] && !taken[y * 16 + x + w]) w++;
        int h = 1;
        while (y + h < 16 && run(ground, taken, x, y + h, w)) h++;
        for (int dy = 0; dy < h; dy++) {
          for (int dx = 0; dx < w; dx++) taken[(y + dy) * 16 + x + dx] = true;
        }
        out.addAll(List.of(x, y, w, h));
      }
    }
    return out.stream().mapToInt(Integer::intValue).toArray();
  }

  private static boolean run(boolean[] ground, boolean[] taken, int x, int y, int w) {
    for (int dx = 0; dx < w; dx++) {
      if (!ground[y * 16 + x + dx] || taken[y * 16 + x + dx]) return false;
    }
    return true;
  }
}
