package engine.rendering;

import engine.level.Tile;
import engine.level.elements.tile.DoorTile;
import engine.level.elements.tile.PitTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves the textures of level tiles while the level is drawn, from each tile's element, design
 * and direct neighbours. Tiles store no texture of their own unless a room gives them a {@link
 * Tile#customTexture() custom one}.
 *
 * <p>All textures of a design lie below {@code dungeon/<design>/}. Walls use the design's autotile
 * atlas {@link #WALL_ATLAS}, see {@link LevelRenderer}; floors, exits, holes, pits and doors use
 * single textures; portals, glass walls and grates choose their orientation from the surrounding
 * walls.
 */
public final class TileTextures {

  /** The autotile atlas of a design's walls, relative to the design directory. */
  public static final String WALL_ATLAS = "wall/walls.png";

  /** Left edge of the straight horizontal wall piece in a wall atlas, in pixels. */
  public static final int STRAIGHT_WALL_X = 32;

  /** Top edge of the straight horizontal wall piece in a wall atlas, in pixels. */
  public static final int STRAIGHT_WALL_Y = 48;

  // Paths are built once per design and name, so drawing a frame allocates none.
  private static final Map<DesignLabel, Map<String, IPath>> PATHS =
      new EnumMap<>(DesignLabel.class);
  private static final Map<IPath, IPath> CLOSED_CUSTOM = new HashMap<>();

  private TileTextures() {}

  /**
   * Returns the texture of a tile that is not drawn from its design's wall atlas.
   *
   * @param tile the tile
   * @param layout the level layout, indexed [y][x]
   * @return the texture
   */
  public static IPath texture(Tile tile, Tile[][] layout) {
    if (tile.customTexture().isPresent()) {
      IPath custom = tile.customTexture().get();
      return tile instanceof DoorTile door && !door.isOpen() ? closed(custom) : custom;
    }
    DesignLabel design = tile.designLabel();
    int x = tile.coordinate().x();
    int y = tile.coordinate().y();
    return switch (tile.levelElement()) {
      case WALL -> wallAtlas(design);
      case FLOOR -> path(design, "floor/floor_1.png");
      case EXIT -> path(design, "floor/floor_ladder.png");
      case HOLE ->
          path(
              design,
              element(layout, x, y + 1) == LevelElement.HOLE
                  ? "floor/floor_hole1.png"
                  : "floor/floor_hole.png");
      case PIT -> pit((PitTile) tile, layout);
      case DOOR -> door((DoorTile) tile, layout);
      case GITTER -> portalPath(design, gitter(layout, x, y));
      case GLASSWALL -> portalPath(design, glasswall(layout, x, y));
      case PORTAL -> portalPath(design, portal(layout, x, y));
      case SKIP -> path(design, "floor/empty.png");
    };
  }

  /**
   * Returns the texture that represents an element of a design without neighbours, for previews
   * such as the level editor's element buttons. Walls return the whole atlas; show its {@link
   * #STRAIGHT_WALL_X straight wall piece}.
   *
   * @param element the element
   * @param design the design
   * @return the texture
   */
  public static IPath preview(LevelElement element, DesignLabel design) {
    return switch (element) {
      case FLOOR -> path(design, "floor/floor_1.png");
      case EXIT -> path(design, "floor/floor_ladder.png");
      case HOLE -> path(design, "floor/floor_hole.png");
      case PIT -> path(design, "floor/floor_damaged.png");
      case DOOR -> path(doorDesign(design), "door/top.png");
      case WALL -> wallAtlas(design);
      case GITTER -> portalPath(design, "portal/gutter/gutter_horizontal.png");
      case GLASSWALL -> portalPath(design, "portal/glasswall/glasswall_horizontal.png");
      case PORTAL -> portalPath(design, "portal/portal_block.png");
      case SKIP -> path(design, "floor/empty.png");
    };
  }

  /**
   * Returns the wall autotile atlas of a design.
   *
   * @param design the design
   * @return the atlas
   */
  public static IPath wallAtlas(DesignLabel design) {
    return path(design, WALL_ATLAS);
  }

  /**
   * Returns a texture of a design.
   *
   * @param design the design
   * @param name the texture path below the design directory
   * @return the texture
   */
  static IPath path(DesignLabel design, String name) {
    Map<String, IPath> paths = PATHS.computeIfAbsent(design, d -> new HashMap<>());
    IPath path = paths.get(name);
    if (path == null) {
      path = new SimpleIPath("dungeon/" + design.name().toLowerCase() + "/" + name);
      paths.put(name, path);
    }
    return path;
  }

  private static IPath closed(IPath custom) {
    IPath closed = CLOSED_CUSTOM.get(custom);
    if (closed == null) {
      String path = custom.pathString();
      int dot = path.lastIndexOf('.');
      closed = new SimpleIPath(path.substring(0, dot) + "_closed" + path.substring(dot));
      CLOSED_CUSTOM.put(custom, closed);
    }
    return closed;
  }

  // The default design has no doors of its own yet and keeps those of the old default design.
  private static DesignLabel doorDesign(DesignLabel design) {
    return design == DesignLabel.DEFAULT ? DesignLabel.OLD_DEFAULT : design;
  }

  // Portals, glass walls and grates are shared, except in the System Recovery design.
  private static IPath portalPath(DesignLabel design, String name) {
    return path(design == DesignLabel.SYSTEM_RECOVERY ? design : DesignLabel.OLD_DEFAULT, name);
  }

  private static final String[] DOOR_OPEN = {
    "door/top.png", "door/right.png", "door/left.png", "door/bottom.png"
  };
  private static final String[] DOOR_CLOSED = {
    "door/top_closed.png", "door/right_closed.png", "door/left_closed.png", "door/bottom_closed.png"
  };

  // A door shows its frame on the side that faces the room. A hidden door is wall while closed
  // and floor while open.
  private static IPath door(DoorTile door, Tile[][] layout) {
    if (door.hidden()) {
      return door.isOpen()
          ? path(door.designLabel(), "floor/floor_1.png")
          : wallAtlas(door.designLabel());
    }
    int x = door.coordinate().x();
    int y = door.coordinate().y();
    int side;
    if (isInside(layout, x, y - 1)) side = 0;
    else if (isInside(layout, x - 1, y)) side = 1;
    else if (isInside(layout, x + 1, y)) side = 2;
    else if (isInside(layout, x, y + 1)) side = 3;
    else side = 0;
    return path(doorDesign(door.designLabel()), (door.isOpen() ? DOOR_OPEN : DOOR_CLOSED)[side]);
  }

  // A closed pit looks damaged, or like floor if it opens late. An open pit below the void, the
  // inside of a wall or another open pit shows the dark; below anything else its open edge.
  private static IPath pit(PitTile pit, Tile[][] layout) {
    DesignLabel design = pit.designLabel();
    if (pit.looksLikeFloor()) return path(design, "floor/floor_1.png");
    if (!pit.isOpen()) return path(design, "floor/floor_damaged.png");
    IPath empty = path(design, "wall/empty.png");
    int x = pit.coordinate().x();
    int y = pit.coordinate().y();
    Tile above = at(layout, x, y + 1);
    if (above == null) return empty;
    if (above instanceof PitTile abovePit && abovePit.isOpen()) return empty;
    if (above.levelElement() == LevelElement.WALL && isWallInside(layout, x, y + 1)) return empty;
    return switch (above.levelElement()) {
      case FLOOR, HOLE, PIT, WALL, SKIP, DOOR -> path(design, "floor/pit_open.png");
      default -> empty;
    };
  }

  // A wall with only walls and void around it, which shows the dark inside of the wall.
  private static boolean isWallInside(Tile[][] layout, int x, int y) {
    for (int dy = -1; dy <= 1; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        LevelElement e = element(layout, x + dx, y + dy);
        if (e != null && e != LevelElement.WALL && e != LevelElement.SKIP) return false;
      }
    }
    return true;
  }

  private static String gitter(Tile[][] layout, int x, int y) {
    if (isWallLike(layout, x - 1, y)
            && isWallLike(layout, x + 1, y)
            && isFloorLike(layout, x, y - 1)
        || isFloorLike(layout, x, y + 1)) {
      return "portal/gutter/gutter_horizontal.png";
    }
    if (isWallLike(layout, x, y + 1)
            && isWallLike(layout, x, y - 1)
            && isFloorLike(layout, x - 1, y)
        || isFloorLike(layout, x + 1, y)) {
      return "portal/gutter/gutter_vertical.png";
    }
    return "portal/gutter/gutter_horizontal.png";
  }

  private static String glasswall(Tile[][] layout, int x, int y) {
    if (isWallLike(layout, x - 1, y)
            && isWallLike(layout, x + 1, y)
            && isFloorOrDoor(layout, x, y - 1)
        || isFloorOrDoor(layout, x, y + 1)) {
      return "portal/glasswall/glasswall_horizontal.png";
    }
    if (isWallLike(layout, x, y + 1)
        && isWallLike(layout, x, y - 1)
        && (isFloorOrDoor(layout, x - 1, y) || isFloorOrDoor(layout, x + 1, y))) {
      return "portal/glasswall/glasswall_vertical.png";
    }
    return "portal/glasswall/glasswall_horizontal.png";
  }

  private static String portal(Tile[][] layout, int x, int y) {
    boolean horizontalWall = isWallLike(layout, x - 1, y) && isWallLike(layout, x + 1, y);
    boolean verticalWall = isWallLike(layout, x, y + 1) && isWallLike(layout, x, y - 1);
    boolean up = isInside(layout, x, y + 1);
    boolean down = isInside(layout, x, y - 1);
    boolean left = isInside(layout, x - 1, y);
    boolean right = isInside(layout, x + 1, y);
    if (horizontalWall && down && !up) return "portal/portal_wall_top.png";
    if (horizontalWall && !down && up) return "portal/portal_wall_bottom.png";
    if (verticalWall && !left && right) return "portal/portal_wall_right.png";
    if (verticalWall && left && !right) return "portal/portal_wall_left.png";
    if (horizontalWall && isFloorOrDoor(layout, x, y - 1) || isFloorOrDoor(layout, x, y + 1)) {
      return "portal/portal_wall_horizontal.png";
    }
    if (verticalWall && (isFloorOrDoor(layout, x - 1, y) || isFloorOrDoor(layout, x + 1, y))) {
      return "portal/portal_wall_vertical.png";
    }
    return "portal/portal_block.png";
  }

  private static Tile at(Tile[][] layout, int x, int y) {
    if (y < 0 || y >= layout.length || x < 0 || x >= layout[y].length) return null;
    return layout[y][x];
  }

  private static LevelElement element(Tile[][] layout, int x, int y) {
    Tile t = at(layout, x, y);
    return t == null ? null : t.levelElement();
  }

  // An accessible tile or floor-like void: the side of a wall or door that faces the room.
  private static boolean isInside(Tile[][] layout, int x, int y) {
    LevelElement e = element(layout, x, y);
    return e != null && (e.value() || isFloorLike(layout, x, y));
  }

  private static boolean isFloorOrDoor(Tile[][] layout, int x, int y) {
    LevelElement e = element(layout, x, y);
    return isFloorLike(layout, x, y) || e == LevelElement.DOOR || e == LevelElement.EXIT;
  }

  // Void counts as floor where it borders floor, so open edges of a room still face inwards.
  private static boolean isFloorLike(Tile[][] layout, int x, int y) {
    LevelElement e = element(layout, x, y);
    if (e == null) return false;
    if (e == LevelElement.SKIP) return skipIsFloorLike(layout, x, y);
    return isBaseFloor(e);
  }

  private static boolean isWallLike(Tile[][] layout, int x, int y) {
    LevelElement e = element(layout, x, y);
    if (e == null) return false;
    if (e == LevelElement.SKIP) return !skipIsFloorLike(layout, x, y);
    return e == LevelElement.WALL
        || e == LevelElement.PORTAL
        || e == LevelElement.GLASSWALL
        || e == LevelElement.GITTER;
  }

  private static final int[][] SIDES = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

  private static boolean skipIsFloorLike(Tile[][] layout, int x, int y) {
    if (hasBaseFloorAround(layout, x, y)) return true;
    for (int[] d : SIDES) {
      int nx = x + d[0];
      int ny = y + d[1];
      if (element(layout, nx, ny) == LevelElement.SKIP && hasBaseFloorAround(layout, nx, ny)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasBaseFloorAround(Tile[][] layout, int x, int y) {
    for (int[] d : SIDES) {
      if (isBaseFloor(element(layout, x + d[0], y + d[1]))) return true;
    }
    return false;
  }

  private static boolean isBaseFloor(LevelElement e) {
    return e == LevelElement.FLOOR || e == LevelElement.HOLE || e == LevelElement.PIT;
  }
}
