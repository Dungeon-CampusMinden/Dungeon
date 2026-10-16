package engine.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import engine.level.Tile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.level.utils.TileTextureFactory;
import engine.utils.Point;
import engine.utils.components.draw.ColorUtils;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import java.util.List;

/**
 * Draws the level layer in two passes, so that entity shadows drawn in between lie on the floor and
 * walls cover them.
 *
 * <p>Walls of every design are autotiled from the design's atlas {@link
 * TileTextureFactory#WALL_ATLAS}: the cell follows the wall's eight neighbours (Godot "3x3 minimal"
 * blob layout), where walls, doors and the void connect. Floors receive the ambient occlusion of
 * adjacent walls.
 *
 * <p>The {@link DesignLabel#DEFAULT} design adds to that: floors and open doorways pick a stable
 * random floor variant, floor edges above the void break off, and if the level's {@link
 * Atmosphere#wallDecor()} asks for it, wall faces get banners, cracks, torches and fountains. Its
 * doors with room-specific art, such as gates, look like wall while closed and like floor while
 * open. Every other tile, including closed or locked doors and walls with a custom texture, is
 * drawn from its own texture.
 *
 * <p>All decoration is derived from the tile coordinates, so every client renders the same level
 * without any synchronised state.
 */
public final class LevelRenderer {

  private static final String DUNGEON_PREFIX = "dungeon/";
  private static final String DEFAULT_FLOOR = "dungeon/default/floor/floor_1.png";
  // The default design uses the doors of the old default design; open ones show its floor.
  private static final java.util.Set<String> OPEN_DOORS =
      java.util.Set.of(
          "dungeon/old_default/door/top.png",
          "dungeon/old_default/door/bottom.png",
          "dungeon/old_default/door/left.png",
          "dungeon/old_default/door/right.png");
  private static final IPath DEFAULT_WALLS =
      new SimpleIPath("dungeon/default/" + TileTextureFactory.WALL_ATLAS);
  private static final IPath FLOORS = new SimpleIPath("dungeon/default/floor/variants.png");
  private static final IPath DECOR = new SimpleIPath("dungeon/default/wall/decor.png");
  private static final IPath TORCH = new SimpleIPath("dungeon/default/wall/torch.png");
  private static final IPath FOUNTAIN = new SimpleIPath("dungeon/default/wall/fountain.png");

  // Neighbour bits; north is +y in world space.
  private static final int N = 1, NE = 2, E = 4, SE = 8, S = 16, SW = 32, W = 64, NW = 128;

  // Neighbour mask of each 16x16 cell of walls.png, row-major 12x4; -1 marks the unused cell.
  private static final int[] ATLAS_MASKS = {
    16, 20, 84, 80, 213, 92, 116, 87, 28, 125, 124, 112, //
    17, 21, 85, 81, 29, 127, 253, 113, 31, 119, -1, 245, //
    1, 5, 69, 65, 23, 223, 247, 209, 95, 255, 221, 241, //
    0, 4, 68, 64, 117, 71, 197, 93, 7, 199, 215, 193
  };

  private static final int[] CELL_BY_MASK = new int[256];

  static {
    java.util.Arrays.fill(CELL_BY_MASK, 9 + 2 * 12); // solid block as fallback
    for (int i = 0; i < ATLAS_MASKS.length; i++) {
      if (ATLAS_MASKS[i] >= 0) CELL_BY_MASK[ATLAS_MASKS[i]] = i;
    }
  }

  private static final Color TORCH_LIGHT = new Color(1f, 0.58f, 0.26f, 1f);
  private static final Color LAVA_LIGHT = new Color(1f, 0.36f, 0.1f, 1f);
  private static final Color WATER_LIGHT = new Color(0.3f, 0.62f, 1f, 1f);
  private static final int TORCH_SPACING = 4;
  private static final int FLOOR_VARIANTS = 8;
  private static final int SKULL = 8;
  private static final int LEDGE = 12;
  private static final int BANNERS = 4;
  // Floor visible on each side of a pillar of the wall atlas, in tiles.
  private static final float PILLAR_INSET = 5 / 16f;

  /**
   * Draws the floor pass inside the given tile bounds: floors, everything under walls, broken floor
   * edges and the ambient occlusion walls cast onto the floor.
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
          if (isLedge(layout, x, y)) {
            batch.setColor(Color.WHITE);
            drawCell(batch, FLOORS, LEDGE, x, y);
          }
          continue;
        }
        batch.setColor(tint(t));
        if (isAutoWall(t)) {
          // Default walls stand on floor; the walls of the other designs on the void.
          if (isDefault(t)) drawCell(batch, FLOORS, floorVariant(x, y), x, y);
        } else if (isAutoFloor(t)) {
          drawCell(batch, FLOORS, floorVariant(x, y), x, y);
          if (hash(x, y, 3) % 90 == 0) drawCell(batch, FLOORS, SKULL, x, y);
        } else if (!isUpright(t)) {
          batch.draw(TextureMap.instance().textureAt(t.texturePath()), x, y, 1, 1);
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
          Tile corner = wallCorner(layout, x, y);
          if (corner != null) {
            batch.setColor(Color.WHITE);
            drawCell(batch, wallAtlas(corner), CELL_BY_MASK[wallMask(layout, x, y)], x, y);
          }
          continue;
        }
        batch.setColor(tint(t));
        if (isAutoWall(t)) {
          drawCell(batch, wallAtlas(t), CELL_BY_MASK[wallMask(layout, x, y)], x, y);
        } else if (!isAutoFloor(t) && isUpright(t)) {
          batch.draw(TextureMap.instance().textureAt(t.texturePath()), x, y, 1, 1);
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
   * Adds the lights of the wall torches and fountains inside the given tile bounds. Call it only
   * for levels with wall decoration.
   *
   * @param layout the level layout, indexed [y][x]
   * @param minX first column
   * @param maxX last column
   * @param minY first row
   * @param maxY last row
   * @param out receives the lights
   */
  public void collectLights(
      Tile[][] layout, int minX, int maxX, int minY, int maxY, List<Light> out) {
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        // Lights sit just in front of the wall, so the wall blocks them towards its back side.
        if (isTorch(layout, x, y)) {
          out.add(new Light(new Point(x + 0.5f, y - 0.15f), TORCH_LIGHT, 6.5f, 0.9f, 0.14f));
        } else if (isFountain(layout, x, y)) {
          boolean lava = isLava(x, y);
          Color color = lava ? LAVA_LIGHT : WATER_LIGHT;
          out.add(new Light(new Point(x + 0.5f, y - 0.15f), color, 4.5f, 0.85f, lava ? 0.1f : 0f));
        }
      }
    }
  }

  /**
   * Returns the world positions of the torch flames inside the given tile bounds. Call it only for
   * levels with wall decoration.
   *
   * @param layout the level layout, indexed [y][x]
   * @param minX first column
   * @param maxX last column
   * @param minY first row
   * @param maxY last row
   * @param out receives the flame tips, in world units
   */
  public void collectFlames(
      Tile[][] layout, int minX, int maxX, int minY, int maxY, List<Point> out) {
    for (int y = minY; y <= maxY; y++) {
      for (int x = minX; x <= maxX; x++) {
        if (isTorch(layout, x, y)) out.add(new Point(x + 0.5f, y + 17 / 16f));
      }
    }
  }

  private void drawFloorShadows(SpriteBatch batch, Tile[][] layout, int x, int y) {
    if (solid(layout, x, y + 1)) batch.draw(RenderTextures.wallShadowTop(), x, y, 1, 1);
    Texture side = RenderTextures.wallShadowSide();
    if (solid(layout, x - 1, y)) batch.draw(side, x - pillarInset(layout, x - 1, y), y, 1, 1);
    if (solid(layout, x + 1, y)) {
      batch.draw(side, x + pillarInset(layout, x + 1, y), y, 1, 1, 0, 0, 16, 16, true, false);
    }
  }

  // A one tile wide vertical default wall is a pillar in the middle of its tile with floor on both
  // sides; its shadow starts at the pillar, this far inside the wall tile.
  private static float pillarInset(Tile[][] layout, int x, int y) {
    Tile t = at(layout, x, y);
    if (t == null || !isAutoWall(t) || !isDefault(t)) return 0;
    int mask = wallMask(layout, x, y);
    return (mask & (E | W)) == 0 && (mask & (N | S)) != 0 ? PILLAR_INSET : 0;
  }

  private void drawWallDecor(SpriteBatch batch, Tile[][] layout, int x, int y, float time) {
    if (isTorch(layout, x, y)) {
      Texture torch = TextureMap.instance().textureAt(TORCH);
      // Sconce: 8x10 texels, bottom one texel above the tile's bottom edge.
      batch.draw(torch, x + 4 / 16f, y + 1 / 16f, 0.5f, 10 / 16f, 0, 26, 8, 10, false, false);
      int frame = (int) (time * 9 + hash(x, y, 7) % 4) % 4;
      // Flame: 8x9 texels on top of the sconce, reaching three texels into the tile above.
      batch.draw(torch, x + 4 / 16f, y + 10 / 16f, 0.5f, 9 / 16f, frame * 8, 0, 8, 9, false, false);
      return;
    }
    if (isFountain(layout, x, y)) {
      int frame = (int) (time * 6 + hash(x, y, 7) % 3) % 3;
      drawCell(batch, FOUNTAIN, (isLava(x, y) ? 0 : 3) + frame, x, y);
      return;
    }
    int roll = hash(x, y, 11) % 100;
    if (roll < 10) {
      // One banner colour per stretch of wall, so neighbouring banners match.
      drawCell(batch, DECOR, hash(x / 8, y, 13) % BANNERS, x, y);
    } else if (roll < 15) {
      drawCell(batch, DECOR, BANNERS + roll % 3, x, y);
    }
  }

  private static void drawCell(SpriteBatch batch, IPath atlas, int cell, int x, int y) {
    Texture t = TextureMap.instance().textureAt(atlas);
    int columns = t.getWidth() / 16;
    batch.draw(t, x, y, 1, 1, (cell % columns) * 16, (cell / columns) * 16, 16, 16, false, false);
  }

  private static boolean drawable(Tile t) {
    return t != null && t.levelElement() != LevelElement.SKIP && t.visible();
  }

  // Autotiled default tiles fade their tint to a third of its strength: their floor already shows
  // its tile seams, so the engine's checker pattern only needs to hint at the grid.
  private static Color tint(Tile t) {
    if (t.tintColor() == -1) return Color.WHITE;
    if (!isDefault(t) || (!isAutoWall(t) && !isAutoFloor(t))) {
      return ColorUtils.pmaColor(t.tintColor());
    }
    Color c = new Color(t.tintColor());
    c.set(1 - (1 - c.r) / 3f, 1 - (1 - c.g) / 3f, 1 - (1 - c.b) / 3f, c.a);
    return ColorUtils.pmaColor(c);
  }

  private static int floorVariant(int x, int y) {
    int roll = hash(x, y, 1) % 100;
    return roll < 80 ? 0 : 1 + roll % (FLOOR_VARIANTS - 1);
  }

  // A wall that shows its design's atlas rather than a custom texture, or a closed gate.
  private static boolean isAutoWall(Tile t) {
    if (isGate(t)) return !t.isAccessible();
    String path = t.texturePath().pathString();
    return t.levelElement() == LevelElement.WALL
        && path.startsWith(DUNGEON_PREFIX)
        && path.endsWith(TileTextureFactory.WALL_ATLAS);
  }

  private static IPath wallAtlas(Tile t) {
    return isGate(t) ? DEFAULT_WALLS : t.texturePath();
  }

  private static boolean isDefault(Tile t) {
    return t.designLabel() == DesignLabel.DEFAULT;
  }

  // A floor or open doorway of the default design, drawn from its floor variants.
  private static boolean isAutoFloor(Tile t) {
    if (isGate(t)) return t.isAccessible();
    if (!isDefault(t)) return false;
    String path = t.texturePath().pathString();
    return (t.levelElement() == LevelElement.FLOOR && path.equals(DEFAULT_FLOOR))
        || (t.levelElement() == LevelElement.DOOR && OPEN_DOORS.contains(path));
  }

  // A door of the default design with room-specific art, such as a gate or sluice.
  private static boolean isGate(Tile t) {
    return t.levelElement() == LevelElement.DOOR
        && t.designLabel() == DesignLabel.DEFAULT
        && !t.texturePath().pathString().startsWith(DUNGEON_PREFIX);
  }

  // Walls and doors stand upright; entity shadows end at them.
  private static boolean isUpright(Tile t) {
    return t.levelElement() == LevelElement.WALL || t.levelElement() == LevelElement.DOOR;
  }

  // A wall face: an autotiled wall in a horizontal run whose front looks onto walkable floor.
  private static boolean isFace(Tile[][] layout, int x, int y) {
    Tile t = at(layout, x, y);
    Tile below = at(layout, x, y - 1);
    return t != null
        && t.visible()
        // A fully transparent tint hides the tile, for example while fog of war covers it.
        && (t.tintColor() & 0xff) != 0
        && t.levelElement() == LevelElement.WALL
        && isDefault(t)
        && isAutoWall(t)
        && below != null
        && below.isAccessible()
        && solid(layout, x - 1, y)
        && solid(layout, x + 1, y);
  }

  // A rare wall fountain spouting lava or water.
  private static boolean isFountain(Tile[][] layout, int x, int y) {
    return hash(x, y, 17) % 100 < 4 && !isTorch(layout, x, y) && isFace(layout, x, y);
  }

  private static boolean isLava(int x, int y) {
    return hash(x, y, 19) % 2 == 0;
  }

  // Void right below an autotiled floor shows the floor's broken-off edge.
  private static boolean isLedge(Tile[][] layout, int x, int y) {
    Tile t = at(layout, x, y);
    Tile above = at(layout, x, y + 1);
    return (t == null || t.levelElement() == LevelElement.SKIP)
        && above != null
        && above.visible()
        && isAutoFloor(above);
  }

  private static boolean isTorch(Tile[][] layout, int x, int y) {
    return Math.floorMod(x + 2 * y, TORCH_SPACING) == 0 && isFace(layout, x, y);
  }

  // Void where a wall steps back: the floor touches it only diagonally, between two walls, so it
  // carries the corner that joins them. Returns one of those walls, or null.
  private static Tile wallCorner(Tile[][] layout, int x, int y) {
    for (int dx = -1; dx <= 1; dx += 2) {
      for (int dy = -1; dy <= 1; dy += 2) {
        Tile diagonal = at(layout, x + dx, y + dy);
        if (diagonal != null
            && diagonal.visible()
            && !solid(layout, x + dx, y + dy)
            && autoWallAt(layout, x + dx, y)
            && autoWallAt(layout, x, y + dy)) return at(layout, x + dx, y);
      }
    }
    return null;
  }

  private static boolean autoWallAt(Tile[][] layout, int x, int y) {
    Tile t = at(layout, x, y);
    return t != null && t.visible() && isAutoWall(t);
  }

  // Walls, closed gates and void count as solid rock, so outer walls blend into the surrounding
  // dark.
  private static boolean solid(Tile[][] layout, int x, int y) {
    Tile t = at(layout, x, y);
    return t == null
        || t.levelElement() == LevelElement.WALL
        || t.levelElement() == LevelElement.SKIP
        || (isGate(t) && !t.isAccessible());
  }

  // Walls run on through doors, which sit inside the wall line.
  private static boolean connects(Tile[][] layout, int x, int y) {
    Tile t = at(layout, x, y);
    return solid(layout, x, y) || t.levelElement() == LevelElement.DOOR;
  }

  private static int wallMask(Tile[][] layout, int x, int y) {
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
    return mask;
  }

  private static Tile at(Tile[][] layout, int x, int y) {
    if (y < 0 || y >= layout.length || x < 0 || x >= layout[y].length) return null;
    return layout[y][x];
  }

  private static int hash(int x, int y, int salt) {
    int h = x * 374761393 + y * 668265263 + salt * 1442695041;
    h = (h ^ (h >>> 13)) * 1274126177;
    return (h ^ (h >>> 16)) & 0x7fffffff;
  }
}
