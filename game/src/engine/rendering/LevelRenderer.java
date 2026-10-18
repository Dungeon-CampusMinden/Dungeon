package engine.rendering;

import static engine.rendering.DesignTileset.CELL;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import engine.level.Tile;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.components.draw.ColorUtils;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.IPath;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Draws the level layer in two passes, so that entity shadows drawn in between lie on the floor and
 * walls cover them.
 *
 * <p>Walls are autotiled from their design's atlas, see {@link DesignTileset}: the cell follows the
 * wall's eight neighbours (Godot "3x3 minimal" blob layout), where walls, doors and the void
 * connect. Hidden doors look like wall while closed and like floor while open. Floors receive the
 * ambient occlusion of adjacent walls. Designs with floor variants also vary their floors and open
 * doorways, break off floor edges above the void and, if the level's {@link Atmosphere#wallDecor()}
 * asks for it, decorate wall faces with banners, cracks, torches and fountains. Every other tile is
 * drawn from the texture {@link TileTextures} resolves for it.
 *
 * <p>All decoration is derived from the tile coordinates, so every client renders the same level
 * without any synchronised state.
 */
public final class LevelRenderer {

  /** Neighbour bits of a wall cell's mask; north is +y in world space. */
  static final int N = 1, NE = 2, E = 4, SE = 8, S = 16, SW = 32, W = 64, NW = 128;

  /**
   * Neighbour mask of each 16x16 cell of a wall atlas, row-major 12x4; -1 marks the unused cell.
   */
  static final int[] ATLAS_MASKS = {
    16, 20, 84, 80, 213, 92, 116, 87, 28, 125, 124, 112, //
    17, 21, 85, 81, 29, 127, 253, 113, 31, 119, -1, 245, //
    1, 5, 69, 65, 23, 223, 247, 209, 95, 255, 221, 241, //
    0, 4, 68, 64, 117, 71, 197, 93, 7, 199, 215, 193
  };

  /** The cell of a wall surrounded by wall on all eight sides. */
  private static final int FILL_CELL = 33;

  private static final int[] CELL_BY_MASK = new int[256];

  static {
    Arrays.fill(CELL_BY_MASK, FILL_CELL);
    for (int i = 0; i < ATLAS_MASKS.length; i++) {
      if (ATLAS_MASKS[i] >= 0) CELL_BY_MASK[ATLAS_MASKS[i]] = i;
    }
  }

  private static final Color TORCH_LIGHT = new Color(1f, 0.58f, 0.26f, 1f);
  private static final Color LAVA_LIGHT = new Color(1f, 0.36f, 0.1f, 1f);
  private static final Color WATER_LIGHT = new Color(0.3f, 0.62f, 1f, 1f);
  private static final int TORCH_SPACING = 4;
  private static final float PIXEL = 1f / CELL;
  private static final int FLOOR_VARIANTS = 8;
  private static final int SKULL = 8;
  private static final int BANNERS = 4;
  private static final int CRACKS = 3;
  private static final int TORCH_FRAMES = 4;
  private static final int FOUNTAIN_FRAMES = 3;
  // Chances of the stable random decoration, in percent of the eligible tiles; skulls lie on one
  // floor in SKULL_ONE_IN.
  private static final int SKULL_ONE_IN = 90;
  private static final int PLAIN_FLOOR_PERCENT = 80;
  private static final int BANNER_PERCENT = 10;
  private static final int CRACK_PERCENT = 5;
  private static final int FOUNTAIN_PERCENT = 4;
  // Height of a torch's flame tip above the bottom of its wall tile, in tiles.
  private static final float FLAME_TIP = 17 / 16f;
  // Wall lights sit just in front of the wall, so the wall blocks them towards its back side.
  private static final float LIGHT_IN_FRONT = 0.15f;

  /**
   * Draws the floor pass inside the given tile bounds: floors, everything under walls, the edges of
   * floors above the void and the ambient occlusion walls cast onto the floor.
   *
   * @param batch the batch, already begun with the world projection
   * @param layout the level layout, indexed [y][x]
   * @param minX first column to draw
   * @param maxX last column to draw
   * @param minY first row to draw
   * @param maxY last row to draw
   */
  public void drawFloor(
      SpriteBatch batch, Tile[][] layout, int minX, int maxX, int minY, int maxY) {
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        Tile t = layout[y][x];
        if (!drawable(t)) {
          Tile above = at(layout, x, y + 1);
          if (isAbyssEdge(above)) drawAbyssEdge(batch, layout, above, x, y);
          continue;
        }
        batch.setColor(tint(t));
        if (isAutoWall(t)) {
          // Walls of designs with floor variants stand on floor where their art leaves room for the
          // ground beside them, the others on the void.
          if (hasVariants(t)) drawFloorBeside(batch, t, wallCell(layout, x, y), x, y);
        } else if (isAutoFloor(t)) {
          drawCell(batch, variants(t), floorVariant(x, y), x, y);
          if (hash(x, y, 3) % SKULL_ONE_IN == 0) drawCell(batch, variants(t), SKULL, x, y);
        } else if (!isUpright(t)) {
          batch.draw(TextureMap.instance().textureAt(TileTextures.texture(t, layout)), x, y, 1, 1);
        }
      }
    }

    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        Tile t = layout[y][x];
        if (!drawable(t)) continue;
        batch.setColor(tint(t));
        if (t.isAccessible() && (t.levelElement() != LevelElement.DOOR || isAutoFloor(t))) {
          drawFloorShadows(batch, layout, x, y);
        }
      }
    }
    batch.setColor(Color.WHITE);
  }

  /**
   * Draws the wall pass inside the given tile bounds: walls, doors and, if asked for, the wall
   * decoration.
   *
   * @param batch the batch, already begun with the world projection
   * @param layout the level layout, indexed [y][x]
   * @param minX first column to draw
   * @param maxX last column to draw
   * @param minY first row to draw
   * @param maxY last row to draw
   * @param time seconds since start, drives the torch animation
   * @param decor whether wall faces get decoration, torches and fountains
   */
  public void drawWalls(
      SpriteBatch batch,
      Tile[][] layout,
      int minX,
      int maxX,
      int minY,
      int maxY,
      float time,
      boolean decor) {
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        Tile t = layout[y][x];
        if (!drawable(t)) {
          Optional<Tile> corner = wallCorner(layout, x, y);
          if (corner.isPresent()) {
            batch.setColor(tint(corner.get()));
            drawCell(batch, tileset(corner.get()).walls(), wallCell(layout, x, y), x, y);
          }
          continue;
        }
        batch.setColor(tint(t));
        if (isAutoWall(t)) {
          drawCell(batch, tileset(t).walls(), wallCell(layout, x, y), x, y);
        } else if (!isAutoFloor(t) && isUpright(t)) {
          batch.draw(TextureMap.instance().textureAt(TileTextures.texture(t, layout)), x, y, 1, 1);
        }
      }
    }

    if (decor) {
      for (int y = minY; y <= maxY; y++) {
        for (int x = minX; x <= maxX; x++) {
          Tile t = layout[y][x];
          if (!drawable(t) || !isFace(layout, x, y)) continue;
          batch.setColor(tint(t));
          drawWallDecor(batch, layout, x, y, time);
        }
      }
    }
    batch.setColor(Color.WHITE);
  }

  /**
   * Adds the lights of the wall torches and fountains and the flames of the torches inside the
   * given tile bounds. Call it only for levels with wall decoration.
   *
   * @param layout the level layout, indexed [y][x]
   * @param minX first column
   * @param maxX last column
   * @param minY first row
   * @param maxY last row
   * @param lights receives the lights
   * @param flames receives the flame tips, in world units
   */
  public void collectLights(
      Tile[][] layout,
      int minX,
      int maxX,
      int minY,
      int maxY,
      List<Light> lights,
      List<Point> flames) {
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        Point front = new Point(x + 0.5f, y - LIGHT_IN_FRONT);
        if (isTorch(layout, x, y)) {
          lights.add(new Light(front, TORCH_LIGHT, 6.5f, 0.9f, 0.14f));
          flames.add(new Point(x + 0.5f, y + FLAME_TIP));
        } else if (isFountain(layout, x, y)) {
          boolean lava = isLava(x, y);
          lights.add(
              new Light(front, lava ? LAVA_LIGHT : WATER_LIGHT, 4.5f, 0.85f, lava ? 0.1f : 0f));
        }
      }
    }
  }

  private void drawFloorShadows(SpriteBatch batch, Tile[][] layout, int x, int y) {
    // Each shadow starts where the wall's art does.
    if (solid(layout, x, y + 1)) {
      float inset = wallInset(layout, x, y + 1, Side.BOTTOM);
      batch.draw(RenderTextures.wallShadowTop(), x, y + inset, 1, 1);
    }
    Texture side = RenderTextures.wallShadowSide();
    if (solid(layout, x - 1, y)) {
      batch.draw(side, x - wallInset(layout, x - 1, y, Side.RIGHT), y, 1, 1);
    }
    if (solid(layout, x + 1, y)) {
      float inset = wallInset(layout, x + 1, y, Side.LEFT);
      batch.draw(side, x + inset, y, 1, 1, 0, 0, 16, 16, true, false);
    }
  }

  private enum Side {
    LEFT,
    RIGHT,
    BOTTOM
  }

  private static float wallInset(Tile[][] layout, int x, int y, Side side) {
    Tile t = at(layout, x, y);
    if (t == null || !isAutoWall(t)) return 0;
    DesignTileset tileset = tileset(t);
    int cell = wallCell(layout, x, y);
    return switch (side) {
      case LEFT -> tileset.leftInset(cell);
      case RIGHT -> tileset.rightInset(cell);
      case BOTTOM -> tileset.bottomInset(cell);
    };
  }

  private void drawWallDecor(SpriteBatch batch, Tile[][] layout, int x, int y, float time) {
    Tile t = layout[y][x];
    if (isTorch(layout, x, y)) {
      Texture torch =
          TextureMap.instance().textureAt(TileTextures.path(t.designLabel(), "wall/torch.png"));
      // Sconce: 8x10 texels, bottom one texel above the tile's bottom edge.
      batch.draw(torch, x + 4 / 16f, y + 1 / 16f, 0.5f, 10 / 16f, 0, 26, 8, 10, false, false);
      int frame = (int) (time * 9 + hash(x, y, 7) % TORCH_FRAMES) % TORCH_FRAMES;
      // Flame: 8x9 texels on top of the sconce, reaching three texels into the tile above.
      batch.draw(torch, x + 4 / 16f, y + 10 / 16f, 0.5f, 9 / 16f, frame * 8, 0, 8, 9, false, false);
      return;
    }
    if (isFountain(layout, x, y)) {
      int frame = (int) (time * 6 + hash(x, y, 7) % FOUNTAIN_FRAMES) % FOUNTAIN_FRAMES;
      IPath fountain = TileTextures.path(t.designLabel(), "wall/fountain.png");
      // Lava frames first, then water.
      drawCell(batch, fountain, (isLava(x, y) ? 0 : FOUNTAIN_FRAMES) + frame, x, y);
      return;
    }
    int roll = hash(x, y, 11) % 100;
    IPath decor = TileTextures.path(t.designLabel(), "wall/decor.png");
    if (roll < BANNER_PERCENT) {
      // One banner colour per stretch of wall, so neighbouring banners match.
      drawCell(batch, decor, hash(x / 8, y, 13) % BANNERS, x, y);
    } else if (roll < BANNER_PERCENT + CRACK_PERCENT) {
      drawCell(batch, decor, BANNERS + roll % CRACKS, x, y);
    }
  }

  private static void drawCell(SpriteBatch batch, IPath atlas, int cell, int x, int y) {
    Texture t = TextureMap.instance().textureAt(atlas);
    int columns = t.getWidth() / CELL;
    batch.draw(
        t, x, y, 1, 1, (cell % columns) * CELL, (cell / columns) * CELL, CELL, CELL, false, false);
  }

  private static void drawFloorBeside(SpriteBatch batch, Tile wall, int cell, int x, int y) {
    Texture t = TextureMap.instance().textureAt(variants(wall));
    int columns = t.getWidth() / CELL;
    int variant = floorVariant(x, y);
    int[] rects = tileset(wall).floorRects(cell);
    for (int i = 0; i < rects.length; i += 4) {
      int rx = rects[i];
      int ry = rects[i + 1];
      int w = rects[i + 2];
      int h = rects[i + 3];
      batch.draw(
          t,
          x + rx * PIXEL,
          y + (CELL - ry - h) * PIXEL,
          w * PIXEL,
          h * PIXEL,
          (variant % columns) * CELL + rx,
          (variant / columns) * CELL + ry,
          w,
          h,
          false,
          false);
    }
  }

  private static boolean drawable(Tile t) {
    return t != null && t.levelElement() != LevelElement.SKIP && t.visible();
  }

  private static DesignTileset tileset(Tile t) {
    return DesignTileset.of(t.designLabel());
  }

  private static boolean hasVariants(Tile t) {
    return tileset(t).floorVariants().isPresent();
  }

  private static IPath variants(Tile t) {
    return tileset(t).floorVariants().orElseThrow();
  }

  private static Color tint(Tile t) {
    // Autotiled tiles of designs with floor variants fade their tint to a third: their floor
    // already shows its seams, so the engine's checker pattern only needs to hint at the grid.
    if (t.tintColor() == -1) return Color.WHITE;
    if (!hasVariants(t) || (!isAutoWall(t) && !isAutoFloor(t))) {
      return ColorUtils.pmaColor(t.tintColor());
    }
    Color c = new Color(t.tintColor());
    c.set(1 - (1 - c.r) / 3f, 1 - (1 - c.g) / 3f, 1 - (1 - c.b) / 3f, c.a);
    return ColorUtils.pmaColor(c);
  }

  private static int floorVariant(int x, int y) {
    int roll = hash(x, y, 1) % 100;
    return roll < PLAIN_FLOOR_PERCENT ? 0 : 1 + roll % (FLOOR_VARIANTS - 1);
  }

  /**
   * Whether a tile is drawn from its design's wall atlas: a wall or a closed hidden door.
   *
   * @param t the tile
   * @return true if it is
   */
  static boolean isAutoWall(Tile t) {
    if (t instanceof DoorTile door && door.hidden()) return !door.isOpen();
    return t.levelElement() == LevelElement.WALL;
  }

  private static boolean isAutoFloor(Tile t) {
    if (!hasVariants(t)) return false;
    return switch (t.levelElement()) {
      case FLOOR -> true;
      case DOOR -> t.isAccessible();
      default -> false;
    };
  }

  private static boolean isHiddenDoor(Tile t) {
    return t instanceof DoorTile door && door.hidden();
  }

  private static boolean isUpright(Tile t) {
    return t.levelElement() == LevelElement.WALL || t.levelElement() == LevelElement.DOOR;
  }

  private static boolean isFace(Tile[][] layout, int x, int y) {
    Tile t = at(layout, x, y);
    Tile below = at(layout, x, y - 1);
    return t != null
        && t.visible()
        && !hiddenByTint(t)
        && t.levelElement() == LevelElement.WALL
        && isAutoWall(t)
        && tileset(t).decor()
        && below != null
        && below.isAccessible()
        && solid(layout, x - 1, y)
        && solid(layout, x + 1, y);
  }

  private static boolean isFountain(Tile[][] layout, int x, int y) {
    return hash(x, y, 17) % 100 < FOUNTAIN_PERCENT
        && !isTorch(layout, x, y)
        && isFace(layout, x, y);
  }

  private static boolean isLava(int x, int y) {
    return hash(x, y, 19) % 2 == 0;
  }

  private static boolean isAbyssEdge(Tile above) {
    return drawable(above) && above.isAccessible() && !hiddenByTint(above);
  }

  private static boolean hiddenByTint(Tile t) {
    // A fully transparent tint hides the tile, for example while fog of war covers it.
    return (t.tintColor() & 0xff) == 0;
  }

  private static void drawAbyssEdge(SpriteBatch batch, Tile[][] layout, Tile above, int x, int y) {
    batch.setColor(tint(above));
    if (isAutoFloor(above)) {
      drawCell(batch, variants(above), floorVariant(x, y + 1), x, y);
    } else {
      IPath side = TileTextures.path(above.designLabel(), "floor/floor_1.png");
      batch.draw(TextureMap.instance().textureAt(side), x, y, 1, 1);
    }
    batch.draw(RenderTextures.abyssFade(), x, y, 1, 1);
  }

  private static boolean isTorch(Tile[][] layout, int x, int y) {
    return Math.floorMod(x + 2 * y, TORCH_SPACING) == 0 && isFace(layout, x, y);
  }

  /**
   * The corner a void tile carries where a wall steps back: the floor touches the void only
   * diagonally, between two walls, so the void shows the corner that joins them.
   *
   * @param layout the level layout, indexed [y][x]
   * @param x the void tile's column
   * @param y the void tile's row
   * @return one of the two walls, whose design draws the corner, or empty if there is no corner
   */
  private static Optional<Tile> wallCorner(Tile[][] layout, int x, int y) {
    for (int dx = -1; dx <= 1; dx += 2) {
      for (int dy = -1; dy <= 1; dy += 2) {
        Tile diagonal = at(layout, x + dx, y + dy);
        if (diagonal != null
            && diagonal.visible()
            && !solid(layout, x + dx, y + dy)
            && autoWallAt(layout, x + dx, y)
            && autoWallAt(layout, x, y + dy)) return Optional.of(at(layout, x + dx, y));
      }
    }
    return Optional.empty();
  }

  private static boolean autoWallAt(Tile[][] layout, int x, int y) {
    Tile t = at(layout, x, y);
    return t != null && t.visible() && isAutoWall(t);
  }

  private static boolean solid(Tile[][] layout, int x, int y) {
    // The void counts as solid, so outer walls blend into the surrounding dark.
    Tile t = at(layout, x, y);
    return t == null
        || t.levelElement() == LevelElement.WALL
        || t.levelElement() == LevelElement.SKIP
        || (isHiddenDoor(t) && !t.isAccessible());
  }

  private static boolean connects(Tile[][] layout, int x, int y) {
    // Walls run on through doors, which sit inside the wall line.
    Tile t = at(layout, x, y);
    return solid(layout, x, y) || t.levelElement() == LevelElement.DOOR;
  }

  /**
   * The atlas cell of the wall at the given position, chosen from its eight neighbours.
   *
   * @param layout the level layout, indexed [y][x]
   * @param x the wall's column
   * @param y the wall's row
   * @return the cell index in the design's wall atlas
   */
  static int wallCell(Tile[][] layout, int x, int y) {
    boolean n = connects(layout, x, y + 1);
    boolean e = connects(layout, x + 1, y);
    boolean s = connects(layout, x, y - 1);
    boolean w = connects(layout, x - 1, y);
    int mask = (n ? N : 0) | (e ? E : 0) | (s ? S : 0) | (w ? W : 0);
    // "Minimal" blob rule: a diagonal only counts when both adjacent sides connect.
    if (n && e && connects(layout, x + 1, y + 1)) mask |= NE;
    if (s && e && connects(layout, x + 1, y - 1)) mask |= SE;
    if (s && w && connects(layout, x - 1, y - 1)) mask |= SW;
    if (n && w && connects(layout, x - 1, y + 1)) mask |= NW;
    return CELL_BY_MASK[mask];
  }

  /**
   * The tile at the given position.
   *
   * @param layout the level layout, indexed [y][x]
   * @param x the column
   * @param y the row
   * @return the tile, or null outside the layout or where the layout holds none
   */
  static Tile at(Tile[][] layout, int x, int y) {
    if (y < 0 || y >= layout.length || x < 0 || x >= layout[y].length) return null;
    return layout[y][x];
  }

  private static int hash(int x, int y, int salt) {
    int h = x * 374761393 + y * 668265263 + salt * 1442695041;
    h = (h ^ (h >>> 13)) * 1274126177;
    return (h ^ (h >>> 16)) & 0x7fffffff;
  }
}
