package engine.level.utils;

import engine.level.Tile;
import engine.level.elements.tile.PitTile;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;

/**
 * Resolves the texture path of a dungeon tile from its element, design and direct neighbours.
 *
 * <p>Every wall of a design resolves to the design's autotile atlas {@code wall/walls.png}; {@link
 * engine.rendering.LevelRenderer} picks the cell from the wall's eight neighbours when it draws the
 * level. Floors, exits, holes, pits and doors resolve to single textures; portals, glass walls and
 * grates choose their orientation from the surrounding walls. All paths lie below the design
 * directory {@code dungeon/<design>/}.
 */
public class TileTextureFactory {

  /** The autotile atlas of a design's walls, relative to the design directory. */
  public static final String WALL_ATLAS = "wall/walls.png";

  private enum Dir {
    UP(0, 1),
    DOWN(0, -1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Dir(int dx, int dy) {
      this.dx = dx;
      this.dy = dy;
    }
  }

  /**
   * Resolves the texture of a level part.
   *
   * @param levelPart a part of a level
   * @return path to the texture
   */
  public static IPath findTexturePath(LevelPart levelPart) {
    String design = designDir(levelPart.design());
    return switch (levelPart.element()) {
      case WALL -> new SimpleIPath(design + WALL_ATLAS);
      case FLOOR -> new SimpleIPath(design + "floor/floor_1.png");
      case EXIT -> new SimpleIPath(design + "floor/floor_ladder.png");
      case PIT -> new SimpleIPath(design + "floor/floor_damaged.png");
      case HOLE ->
          new SimpleIPath(
              design
                  + (neighborIs(levelPart, Dir.UP, LevelElement.HOLE)
                      ? "floor/floor_hole1.png"
                      : "floor/floor_hole.png"));
      case DOOR -> new SimpleIPath(doorDir(levelPart.design()) + doorTexture(levelPart) + ".png");
      case GITTER -> portalPath(levelPart, findGitterElement(levelPart));
      case GLASSWALL -> portalPath(levelPart, findGlasswallElement(levelPart));
      case PORTAL -> portalPath(levelPart, findPortalElement(levelPart));
      default -> new SimpleIPath(design + "floor/empty.png");
    };
  }

  /**
   * Checks which texture must be used for the passed tile based on the surrounding tiles.
   *
   * @param element Tile to check for
   * @param layout The level
   * @param elementType The type ot the tile if different than the attribute
   * @return Path to texture
   */
  public static IPath findTexturePath(Tile element, Tile[][] layout, LevelElement elementType) {
    return findTexturePath(element, layout, levelElementLayout(layout), elementType);
  }

  /**
   * Converts a tile layout to the element layout used for neighborhood-based texture resolution.
   *
   * @param layout the tile layout
   * @return the corresponding element layout
   */
  public static LevelElement[][] levelElementLayout(Tile[][] layout) {
    LevelElement[][] elementLayout = new LevelElement[layout.length][layout[0].length];
    for (int x = 0; x < layout[0].length; x++) {
      for (int y = 0; y < layout.length; y++) {
        elementLayout[y][x] = layout[y][x].levelElement();
      }
    }
    return elementLayout;
  }

  /**
   * Resolves a tile texture using a previously derived element layout.
   *
   * @param element tile to resolve
   * @param layout the tile layout, used for pit state
   * @param elementLayout the precomputed element layout
   * @param elementType the element type to resolve at the tile's coordinate
   * @return path to the texture
   */
  public static IPath findTexturePath(
      Tile element, Tile[][] layout, LevelElement[][] elementLayout, LevelElement elementType) {
    elementLayout[element.coordinate().y()][element.coordinate().x()] = elementType;

    IPath pitPath = findTexturePathPit(element, layout);
    if (pitPath != null) {
      return pitPath;
    }

    return findTexturePath(
        new LevelPart(elementType, element.designLabel(), elementLayout, element.coordinate()));
  }

  /**
   * Finds the texture path for a given level element and design label, without neighbours. Used for
   * previews such as the level editor's element buttons.
   *
   * @param levelElement The element to find the texture for
   * @param designLabel The design label of the element
   * @return The path to the texture
   */
  public static IPath findTexturePath(LevelElement levelElement, DesignLabel designLabel) {
    String design = designDir(designLabel);
    return switch (levelElement) {
      case FLOOR -> new SimpleIPath(design + "floor/floor_1.png");
      case EXIT -> new SimpleIPath(design + "floor/floor_ladder.png");
      case HOLE -> new SimpleIPath(design + "floor/floor_hole.png");
      case PIT -> new SimpleIPath(design + "floor/floor_damaged.png");
      case DOOR -> new SimpleIPath(doorDir(designLabel) + "door/top.png");
      case WALL, SKIP -> new SimpleIPath(design + WALL_ATLAS);
      default -> new SimpleIPath(design + "floor/empty.png");
    };
  }

  /**
   * Returns the element at the given position.
   *
   * @param layout the level grid
   * @param x the x-index (column)
   * @param y the y-index (row)
   * @return the element at (x, y), or {@code null} if outside the layout
   */
  public static LevelElement get(LevelElement[][] layout, int x, int y) {
    return isInsideLayout(x, y, layout) ? layout[y][x] : null;
  }

  private static String designDir(DesignLabel design) {
    return "dungeon/" + design.name().toLowerCase() + "/";
  }

  // The default design has no doors of its own yet and keeps those of the old default design.
  private static String doorDir(DesignLabel design) {
    return designDir(design == DesignLabel.DEFAULT ? DesignLabel.OLD_DEFAULT : design);
  }

  // Portals, glass walls and grates are shared, except in the System Recovery design.
  private static IPath portalPath(LevelPart levelPart, String path) {
    String prefix =
        levelPart.design() == DesignLabel.SYSTEM_RECOVERY
            ? designDir(DesignLabel.SYSTEM_RECOVERY)
            : designDir(DesignLabel.OLD_DEFAULT);
    return new SimpleIPath(prefix + path + ".png");
  }

  /**
   * Resolves the texture path for open {@link PitTile} instances by inspecting the tile directly
   * above. If the pit is closed or the element is not a pit, this method returns {@code null} so
   * that generic resolution can proceed. Open pits below the void, the inside of a wall or another
   * open pit show {@code wall/empty.png}, so the pit reads as a hole into the dark; below floor,
   * walls, doors and closed pits they use {@code floor/pit_open.png}.
   *
   * @param element Tile to check for
   * @param layout The level
   * @return a texture path for open pits, or {@code null} if no pit-specific rule applies
   */
  private static IPath findTexturePathPit(Tile element, Tile[][] layout) {
    if (!(element instanceof PitTile pit) || !pit.isOpen()) {
      return null;
    }

    String base = designDir(element.designLabel());
    IPath pitOpen = new SimpleIPath(base + "floor/pit_open.png");
    IPath empty = new SimpleIPath(base + "wall/empty.png");

    Coordinate pos = element.coordinate();
    int x = pos.x();
    int y = pos.y();
    if (y + 1 >= layout.length || x < 0 || x >= layout[0].length) return empty;

    Tile aboveTile = layout[y + 1][x];
    if (aboveTile == null) return empty;
    if (aboveTile instanceof PitTile abovePit && abovePit.isOpen()) return empty;
    LevelElement above = aboveTile.levelElement();
    if (above == LevelElement.WALL && isWallInside(layout, x, y + 1)) return empty;
    return switch (above) {
      case FLOOR, HOLE, PIT, WALL, SKIP, DOOR -> pitOpen;
      default -> empty;
    };
  }

  // A wall with only walls and void around it, which shows the dark inside of the wall.
  private static boolean isWallInside(Tile[][] layout, int x, int y) {
    for (int dy = -1; dy <= 1; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        int nx = x + dx;
        int ny = y + dy;
        if (ny < 0 || ny >= layout.length || nx < 0 || nx >= layout[ny].length) continue;
        LevelElement e = layout[ny][nx] == null ? null : layout[ny][nx].levelElement();
        if (e != null && e != LevelElement.WALL && e != LevelElement.SKIP) return false;
      }
    }
    return true;
  }

  private static String doorTexture(LevelPart levelPart) {
    if (isInsideDir(levelPart, Dir.DOWN)) return "door/top";
    if (isInsideDir(levelPart, Dir.LEFT)) return "door/right";
    if (isInsideDir(levelPart, Dir.RIGHT)) return "door/left";
    if (isInsideDir(levelPart, Dir.UP)) return "door/bottom";
    return "door/top";
  }

  private static String findGitterElement(LevelPart levelPart) {
    Neighbors n = new Neighbors(levelPart.position, levelPart.layout);
    if (isWallLike(n.getLeft(), levelPart.layout)
            && isWallLike(n.getRight(), levelPart.layout)
            && isFloorLike(n.getDown(), levelPart.layout)
        || isFloorLike(n.getUp(), levelPart.layout)) {
      return "portal/gutter/gutter_horizontal";
    }
    if (isWallLike(n.getUp(), levelPart.layout)
            && isWallLike(n.getDown(), levelPart.layout)
            && isFloorLike(n.getLeft(), levelPart.layout)
        || isFloorLike(n.getRight(), levelPart.layout)) {
      return "portal/gutter/gutter_vertical";
    }
    return "portal/gutter/gutter_horizontal";
  }

  private static String findGlasswallElement(LevelPart levelPart) {
    Neighbors n = new Neighbors(levelPart.position, levelPart.layout);
    if (isWallLike(n.getLeft(), levelPart.layout)
            && isWallLike(n.getRight(), levelPart.layout)
            && isFloorOrDoor(n.getDown(), levelPart.layout)
        || isFloorOrDoor(n.getUp(), levelPart.layout)) {
      return "portal/glasswall/glasswall_horizontal";
    }
    if (isWallLike(n.getUp(), levelPart.layout)
        && isWallLike(n.getDown(), levelPart.layout)
        && (isFloorOrDoor(n.getLeft(), levelPart.layout)
            || isFloorOrDoor(n.getRight(), levelPart.layout))) {
      return "portal/glasswall/glasswall_vertical";
    }
    return "portal/glasswall/glasswall_horizontal";
  }

  private static String findPortalElement(LevelPart levelPart) {
    Neighbors n = new Neighbors(levelPart.position, levelPart.layout);
    LevelElement[][] layout = levelPart.layout;
    boolean horizontalWall = isWallLike(n.getLeft(), layout) && isWallLike(n.getRight(), layout);
    boolean verticalWall = isWallLike(n.getUp(), layout) && isWallLike(n.getDown(), layout);
    if (horizontalWall && isInside(n.getDown(), layout) && !isInside(n.getUp(), layout)) {
      return "portal/portal_wall_top";
    }
    if (horizontalWall && !isInside(n.getDown(), layout) && isInside(n.getUp(), layout)) {
      return "portal/portal_wall_bottom";
    }
    if (verticalWall && !isInside(n.getLeft(), layout) && isInside(n.getRight(), layout)) {
      return "portal/portal_wall_right";
    }
    if (verticalWall && isInside(n.getLeft(), layout) && !isInside(n.getRight(), layout)) {
      return "portal/portal_wall_left";
    }
    if (horizontalWall && isFloorOrDoor(n.getDown(), layout) || isFloorOrDoor(n.getUp(), layout)) {
      return "portal/portal_wall_horizontal";
    }
    if (verticalWall
        && (isFloorOrDoor(n.getLeft(), layout) || isFloorOrDoor(n.getRight(), layout))) {
      return "portal/portal_wall_vertical";
    }
    return "portal/portal_block";
  }

  private static boolean neighborIs(LevelPart levelPart, Dir d, LevelElement element) {
    Coordinate p = levelPart.position();
    return get(levelPart.layout(), p.x() + d.dx, p.y() + d.dy) == element;
  }

  private static boolean isInsideLayout(int x, int y, LevelElement[][] layout) {
    return y >= 0 && y < layout.length && x >= 0 && x < layout[0].length;
  }

  // An accessible tile or floor-like void: the side of a wall or door that faces the room.
  private static boolean isInside(Coordinate p, LevelElement[][] layout) {
    LevelElement e = get(layout, p.x(), p.y());
    return e != null && (e.value() || isFloorLike(p, layout));
  }

  private static boolean isInsideDir(LevelPart levelPart, Dir d) {
    Coordinate p = levelPart.position();
    return isInside(new Coordinate(p.x() + d.dx, p.y() + d.dy), levelPart.layout());
  }

  private static boolean isFloorOrDoor(Coordinate p, LevelElement[][] layout) {
    LevelElement e = get(layout, p.x(), p.y());
    return isFloorLike(p, layout) || e == LevelElement.DOOR || e == LevelElement.EXIT;
  }

  // Void counts as floor where it borders floor, so open edges of a room still face inwards.
  private static boolean isFloorLike(Coordinate p, LevelElement[][] layout) {
    LevelElement e = get(layout, p.x(), p.y());
    if (e == null) return false;
    if (e == LevelElement.SKIP) return skipIsFloorLike(p, layout);
    return isBaseFloor(e);
  }

  private static boolean isWallLike(Coordinate p, LevelElement[][] layout) {
    LevelElement e = get(layout, p.x(), p.y());
    if (e == null) return false;
    if (e == LevelElement.SKIP) return !skipIsFloorLike(p, layout);
    return e == LevelElement.WALL
        || e == LevelElement.PORTAL
        || e == LevelElement.GLASSWALL
        || e == LevelElement.GITTER;
  }

  private static boolean skipIsFloorLike(Coordinate p, LevelElement[][] layout) {
    if (get(layout, p.x(), p.y()) != LevelElement.SKIP) return false;
    if (hasBaseFloorAround(p, layout)) return true;
    for (Dir d : Dir.values()) {
      Coordinate c = new Coordinate(p.x() + d.dx, p.y() + d.dy);
      if (get(layout, c.x(), c.y()) == LevelElement.SKIP && hasBaseFloorAround(c, layout)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasBaseFloorAround(Coordinate c, LevelElement[][] layout) {
    for (Dir d : Dir.values()) {
      if (isBaseFloor(get(layout, c.x() + d.dx, c.y() + d.dy))) return true;
    }
    return false;
  }

  private static boolean isBaseFloor(LevelElement e) {
    return e == LevelElement.FLOOR || e == LevelElement.HOLE || e == LevelElement.PIT;
  }

  /**
   * Helper record class for {@link TileTextureFactory}.
   *
   * @param element Element to check for
   * @param design Design of the element
   * @param layout The level
   * @param position Position of the element.
   */
  public record LevelPart(
      LevelElement element, DesignLabel design, LevelElement[][] layout, Coordinate position) {}
}
