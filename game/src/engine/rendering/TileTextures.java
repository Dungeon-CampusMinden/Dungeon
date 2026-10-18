package engine.rendering;

import engine.level.Tile;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves the textures of level tiles while the level is drawn, from each tile's element, design
 * and direct neighbours. Tiles store no texture of their own.
 *
 * <p>All textures of a design lie below {@code dungeon/<design>/}. Walls use the design's autotile
 * atlas {@link #WALL_ATLAS}, see {@link LevelRenderer}; floors, exits and doors use single
 * textures, doors facing the room.
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

  private TileTextures() {}

  /**
   * Returns the texture of a tile that is not drawn from its design's wall atlas.
   *
   * @param tile the tile
   * @param layout the level layout, indexed [y][x]
   * @return the texture
   */
  public static IPath texture(Tile tile, Tile[][] layout) {
    if (tile instanceof DoorTile door) return door(door, layout);
    return preview(tile.levelElement(), tile.designLabel());
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
      case DOOR -> path(design, "door/top.png");
      case WALL -> wallAtlas(design);
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

  private static final String[] DOOR_OPEN = {
    "door/top.png", "door/right.png", "door/left.png", "door/bottom.png"
  };
  private static final String[] DOOR_CLOSED = {
    "door/top_closed.png", "door/right_closed.png", "door/left_closed.png", "door/bottom_closed.png"
  };

  private static IPath door(DoorTile door, Tile[][] layout) {
    // A door shows its frame on the side that faces the room; a hidden door is wall while closed
    // and floor while open.
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
    return path(door.designLabel(), (door.isOpen() ? DOOR_OPEN : DOOR_CLOSED)[side]);
  }

  private static boolean isInside(Tile[][] layout, int x, int y) {
    Tile t = LevelRenderer.at(layout, x, y);
    return t != null && t.levelElement().value();
  }
}
