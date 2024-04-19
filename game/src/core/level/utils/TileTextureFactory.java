package core.level.utils;

import core.level.Tile;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/** WTF? . */
public class TileTextureFactory {
  /**
   * Checks which texture must be used for the passed field based on the surrounding fields.
   *
   * @param levelPart a part of a level
   * @return Path to texture
   */
  public static IPath findTexturePath(LevelPart levelPart) {
    String prefixPath = "dungeon/" + levelPart.design().name().toLowerCase() + "/";

    IPath path = findTexturePathFloor(levelPart);
    if (path != null) {
      return new SimpleIPath(prefixPath + path.pathString() + ".png");
    }

    path = findTexturePathDoor(levelPart);
    if (path != null) {
      return new SimpleIPath(prefixPath + path.pathString() + ".png");
    }

    path = findTexturePathInnerCorner(levelPart);
    if (path != null) {
      return new SimpleIPath(prefixPath + path.pathString() + ".png");
    }

    path = findTexturePathOuterCorner(levelPart);
    if (path != null) {
      return new SimpleIPath(prefixPath + path.pathString() + ".png");
    }

    path = findTexturePathWall(levelPart);
    if (path != null) {
      return new SimpleIPath(prefixPath + path.pathString() + ".png");
    }

    // Error state
    return new SimpleIPath(prefixPath + "floor/empty.png");
  }

  /**
   * Checks which texture must be used for the passed tile based on the surrounding tiles.
   *
   * @param element Tile to check for
   * @param layout The level
   * @return Path to texture
   */
  public static IPath findTexturePath(Tile element, Tile[][] layout) {
    return findTexturePath(element, layout, element.levelElement());
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
    LevelElement[][] elementLayout = new LevelElement[layout.length][layout[0].length];
    for (int x = 0; x < layout[0].length; x++) {
      for (int y = 0; y < layout.length; y++) {
        elementLayout[y][x] = layout[y][x].levelElement();
      }
    }
    elementLayout[element.coordinate().y][element.coordinate().x] = elementType;
    return findTexturePath(
        new LevelPart(elementType, element.designLabel(), elementLayout, element.coordinate()));
  }

  /**
   * Finds the texture path for a given level element and design label.
   *
   * @param levelElement The element to find the texture for
   * @param designLabel The design label of the element
   * @return The path to the texture
   */
  public static IPath findTexturePath(LevelElement levelElement, DesignLabel designLabel) {
    String prefixPath = "dungeon/" + designLabel.name().toLowerCase() + "/";

    String elementPath;
    if (levelElement == LevelElement.SKIP) {
      elementPath = "floor/empty";
    } else if (levelElement == LevelElement.FLOOR) {
      elementPath = "floor/floor_1";
    } else if (levelElement == LevelElement.EXIT) {
      elementPath = "floor/floor_ladder";
    } else if (levelElement == LevelElement.HOLE) {
      elementPath = "floor/floor_hole";
    } else if (levelElement == LevelElement.PIT) {
      elementPath = "floor/floor_damaged";
    } else if (levelElement == LevelElement.DOOR) {
      elementPath = "door/top";
    } else if (levelElement == LevelElement.WALL) {
      elementPath = "wall/wall_right";
    } else {
      elementPath = "floor/empty";
    }

    return new SimpleIPath(prefixPath + elementPath + ".png");
  }

  private static IPath findTexturePathFloor(LevelPart levelPart) {
    if (levelPart.element() == LevelElement.SKIP) {
      return new SimpleIPath("floor/empty");
    } else if (levelPart.element() == LevelElement.FLOOR) {
      return new SimpleIPath("floor/floor_1");
    } else if (levelPart.element() == LevelElement.EXIT) {
      return new SimpleIPath("floor/floor_ladder");
    } else if (levelPart.element() == LevelElement.HOLE) {
      if (aboveIsHole(levelPart.position, levelPart.layout)) {
        return new SimpleIPath("floor/floor_hole1");
      } else {
        return new SimpleIPath("floor/floor_hole");
      }
    } else if (levelPart.element() == LevelElement.PIT) {
      return new SimpleIPath("floor/floor_damaged");
    }
    return null;
  }

  public static SimpleIPath getEmptyFloorPath() {
    return new SimpleIPath("floor/empty");
  }

  private static IPath findTexturePathDoor(LevelPart levelPart) {
    if (levelPart.element() == LevelElement.DOOR) {
      if (belowIsAccessible(levelPart.position, levelPart.layout)) {
        return new SimpleIPath("door/top");
      } else if (leftIsAccessible(levelPart.position, levelPart.layout)) {
        return new SimpleIPath("door/right");
      } else if (rightIsAccessible(levelPart.position, levelPart.layout)) {
        return new SimpleIPath("door/left");
      } else if (aboveIsAccessible(levelPart.position, levelPart.layout)) {
        return new SimpleIPath("door/bottom");
      }
    }
    return null;
  }

  private static IPath findTexturePathWall(LevelPart levelPart) {
    if (isRightWall(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_right");
    } else if (isLeftWall(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_left");
    } else if (isTopWall(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_top");
    } else if (isBottomWall(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_bottom");
    }
    return null;
  }

  private static IPath findTexturePathInnerCorner(LevelPart levelPart) {
    if (isCrossUpperLeftBottomRight(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_cross_upper_left_bottom_right");
    } else if (isCrossUpperRightBottomLeft(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_cross_upper_right_bottom_left");
    } else if (isBottomLeftInnerCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_inner_corner_bottom_left");
    } else if (isBottomRightInnerCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_inner_corner_bottom_right");
    } else if (isUpperRightInnerCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_inner_corner_upper_right");
    } else if (isUpperLeftInnerCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_inner_corner_upper_left");
    }
    return null;
  }

  private static IPath findTexturePathOuterCorner(LevelPart levelPart) {
    if (isBottomLeftOuterCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_outer_corner_bottom_left");
    } else if (isBottomRightOuterCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_outer_corner_bottom_right");
    } else if (isUpperRightOuterCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_outer_corner_upper_right");
    } else if (isUpperLeftOuterCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/wall_outer_corner_upper_left");
    }
    return null;
  }

  /**
   * Checks if tile with coordinate p is surrounded by walls.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if surrounded by walls
   */
  private static boolean isInSpaceWall(Coordinate p, LevelElement[][] layout) {
    return belowIsWall(p, layout)
        && aboveIsWall(p, layout)
        && leftIsWall(p, layout)
        && rightIsWall(p, layout);
  }

  /**
   * Checks if tile with coordinate p should be a crossUpperLeftBottomRight wall. Tile has to be
   * surrounded by walls and have accessible tiles in the upper left and bottom right.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isCrossUpperLeftBottomRight(Coordinate p, LevelElement[][] layout) {
    return (isInSpaceWall(p, layout)
        && (upperLeftIsAccessible(p, layout) || upperLeftIsHole(p, layout))
        && (bottomRightIsAccessible(p, layout) || bottomRightIsHole(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a crossUpperRightBottomLeft wall. Tile has to be
   * surrounded by walls and have accessible tiles in the upper right and bottom left.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isCrossUpperRightBottomLeft(Coordinate p, LevelElement[][] layout) {
    return (isInSpaceWall(p, layout)
        && (upperRightIsAccessible(p, layout) || upperRightIsHole(p, layout))
        && (bottomLeftIsAccessible(p, layout) || bottomLeftIsHole(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a bottomLeftOuterCorner wall. Tile has to have walls
   * above and to the right and an accessible tile to the upper right.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isBottomLeftOuterCorner(Coordinate p, LevelElement[][] layout) {
    return (aboveIsWall(p, layout)
        && rightIsWall(p, layout)
        && (upperRightIsAccessible(p, layout) || upperRightIsHole(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a bottomRightOuterCorner wall. Tile has to have
   * walls above and to the left and an accessible tile to the upper left.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isBottomRightOuterCorner(Coordinate p, LevelElement[][] layout) {
    return (aboveIsWall(p, layout)
        && leftIsWall(p, layout)
        && (upperLeftIsAccessible(p, layout) || upperLeftIsHole(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a upperRightOuterCorner wall. Tile has to have walls
   * below and to the left and an accessible tile to the bottom left.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isUpperRightOuterCorner(Coordinate p, LevelElement[][] layout) {
    return (belowIsWall(p, layout)
        && leftIsWall(p, layout)
        && (bottomLeftIsAccessible(p, layout) || bottomLeftIsHole(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a upperLeftOuterCorner wall. Tile has to have walls
   * below and to the right and an accessible tile to the bottom right.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isUpperLeftOuterCorner(Coordinate p, LevelElement[][] layout) {
    return (belowIsWall(p, layout)
        && rightIsWall(p, layout)
        && (bottomRightIsAccessible(p, layout) || bottomRightIsHole(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a bottomLeftInnerCorner wall. Tile has to have walls
   * above and to the right and inside tiles (accessible or hole) either to the left and bottom
   * right, below and to the upper left or below and to the left.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isBottomLeftInnerCorner(Coordinate p, LevelElement[][] layout) {
    return (aboveIsWall(p, layout)
        && rightIsWall(p, layout)
        && (leftIsInside(p, layout) && bottomRightIsInside(p, layout)
            || belowIsInside(p, layout) && upperLeftIsInside(p, layout)
            || belowIsInside(p, layout) && leftIsInside(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a bottomRightInnerCorner wall. Tile has to have
   * walls above and to the left and inside tiles (accessible or hole) either to the right and
   * bottom left, below and to the upper right or below and to the right.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isBottomRightInnerCorner(Coordinate p, LevelElement[][] layout) {
    return (aboveIsWall(p, layout)
        && leftIsWall(p, layout)
        && (rightIsInside(p, layout) && bottomLeftIsInside(p, layout)
            || belowIsInside(p, layout) && upperRightIsInside(p, layout)
            || belowIsInside(p, layout) && rightIsInside(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a upperRightInnerCorner wall. Tile has to have walls
   * below and to the left and inside tiles (accessible or hole) either to the right and upper left,
   * above and to the bottom right or above and to the right.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isUpperRightInnerCorner(Coordinate p, LevelElement[][] layout) {
    return (belowIsWall(p, layout)
        && leftIsWall(p, layout)
        && (rightIsInside(p, layout) && upperLeftIsInside(p, layout)
            || aboveIsInside(p, layout) && bottomRightIsInside(p, layout)
            || aboveIsInside(p, layout) && rightIsInside(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a upperLeftInnerCorner wall. Tile has to have walls
   * below and to the right and inside tiles (accessible or hole) either to the left and upper
   * right, above and to the bottom left or above and to the left.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  private static boolean isUpperLeftInnerCorner(Coordinate p, LevelElement[][] layout) {
    return (belowIsWall(p, layout)
        && rightIsWall(p, layout)
        && (leftIsInside(p, layout) && upperRightIsInside(p, layout)
            || aboveIsInside(p, layout) && bottomLeftIsInside(p, layout)
            || aboveIsInside(p, layout) && leftIsInside(p, layout)));
  }

  /**
   * Checks if tile with coordinate p should be a right wall. Tile has to have walls above and below
   * and an inside tile (accessible or hole) to the left.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  public static boolean isRightWall(Coordinate p, LevelElement[][] layout) {
    return (aboveIsWall(p, layout) || aboveIsDoor(p, layout))
        && (belowIsWall(p, layout) || belowIsDoor(p, layout))
        && leftIsInside(p, layout);
  }

  /**
   * Checks if tile with coordinate p should be a left wall. Tile has to have walls above and below
   * and an inside tile (accessible or hole) to the right.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  public static boolean isLeftWall(Coordinate p, LevelElement[][] layout) {
    return (aboveIsWall(p, layout) || aboveIsDoor(p, layout))
        && (belowIsWall(p, layout) || belowIsDoor(p, layout))
        && rightIsInside(p, layout);
  }

  /**
   * Checks if tile with coordinate p should be a top wall. Tile has to have walls to the left and
   * right and an inside tile (accessible or hole) below.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  public static boolean isTopWall(Coordinate p, LevelElement[][] layout) {
    return (leftIsWall(p, layout) || leftIsDoor(p, layout))
        && (rightIsWall(p, layout) || rightIsDoor(p, layout))
        && belowIsInside(p, layout);
  }

  /**
   * Checks if tile with coordinate p should be a bottom wall. Tile has to have walls to the left
   * and right and an inside tile (accessible or hole) above.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  public static boolean isBottomWall(Coordinate p, LevelElement[][] layout) {
    return (leftIsWall(p, layout) || leftIsDoor(p, layout))
        && (rightIsWall(p, layout) || rightIsDoor(p, layout))
        && aboveIsInside(p, layout);
  }

  /**
   * Checks if tile above the coordinate p is a wall.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if above is a wall
   */
  private static boolean aboveIsWall(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y + 1][p.x] == LevelElement.WALL;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile below the coordinate p is a wall.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if below is a wall
   */
  private static boolean belowIsWall(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y - 1][p.x] == LevelElement.WALL;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the left of the coordinate p is a wall.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if left is a wall
   */
  private static boolean leftIsWall(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y][p.x - 1] == LevelElement.WALL;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the right of the coordinate p is a wall.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if right is a wall
   */
  private static boolean rightIsWall(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y][p.x + 1] == LevelElement.WALL;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile above the coordinate p is a door.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if above is a door
   */
  private static boolean aboveIsDoor(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y + 1][p.x] == LevelElement.DOOR;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile below the coordinate p is a door.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if below is a door
   */
  private static boolean belowIsDoor(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y - 1][p.x] == LevelElement.DOOR;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the left of the coordinate p is a door.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if left is a door
   */
  private static boolean leftIsDoor(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y][p.x - 1] == LevelElement.DOOR;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the right of the coordinate p is a door.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if right is a door
   */
  private static boolean rightIsDoor(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y][p.x + 1] == LevelElement.DOOR;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile above the coordinate p is accessible.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if above is accessible
   */
  private static boolean aboveIsAccessible(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y + 1][p.x].value() || layout[p.y + 1][p.x] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the left of the coordinate p is accessible.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if left is accessible
   */
  private static boolean leftIsAccessible(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y][p.x - 1].value() || layout[p.y][p.x - 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the right of the coordinate p is accessible.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if right is accessible
   */
  private static boolean rightIsAccessible(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y][p.x + 1].value() || layout[p.y][p.x + 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile below the coordinate p is accessible.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if below is accessible
   */
  private static boolean belowIsAccessible(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y - 1][p.x].value() || layout[p.y - 1][p.x] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the upper right of the coordinate p is accessible.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if upper right is accessible
   */
  private static boolean upperRightIsAccessible(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y + 1][p.x + 1].value() || layout[p.y + 1][p.x + 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the bottom right of the coordinate p is accessible.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if bottom right is accessible
   */
  private static boolean bottomRightIsAccessible(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y - 1][p.x + 1].value() || layout[p.y - 1][p.x + 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the bottom left of the coordinate p is accessible.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if bottom left is accessible
   */
  private static boolean bottomLeftIsAccessible(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y - 1][p.x - 1].value() || layout[p.y - 1][p.x - 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the upper left of the coordinate p is accessible.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if upper left is accessible
   */
  private static boolean upperLeftIsAccessible(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y + 1][p.x - 1].value() || layout[p.y + 1][p.x - 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile above the coordinate p is a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if above is a hole
   */
  private static boolean aboveIsHole(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y + 1][p.x] == LevelElement.HOLE || layout[p.y + 1][p.x] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the left of the coordinate p is a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if left is a hole
   */
  private static boolean leftIsHole(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y][p.x - 1] == LevelElement.HOLE || layout[p.y][p.x - 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the right of the coordinate p is a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if right is a hole
   */
  private static boolean rightIsHole(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y][p.x + 1] == LevelElement.HOLE || layout[p.y][p.x + 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile below the coordinate p is a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if below is a hole
   */
  private static boolean belowIsHole(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y - 1][p.x] == LevelElement.HOLE || layout[p.y - 1][p.x] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the upper right of the coordinate p is a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if upper right is a hole
   */
  private static boolean upperRightIsHole(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y + 1][p.x + 1] == LevelElement.HOLE
          || layout[p.y + 1][p.x + 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the bottom right of the coordinate p is a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if bottom right is a hole
   */
  private static boolean bottomRightIsHole(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y - 1][p.x + 1] == LevelElement.HOLE
          || layout[p.y - 1][p.x + 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the bottom left of the coordinate p is a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if bottom left is a hole
   */
  private static boolean bottomLeftIsHole(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y - 1][p.x - 1] == LevelElement.HOLE
          || layout[p.y - 1][p.x - 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the upper left of the coordinate p is a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if upper left is a hole
   */
  private static boolean upperLeftIsHole(Coordinate p, LevelElement[][] layout) {
    try {
      return layout[p.y + 1][p.x - 1] == LevelElement.HOLE
          || layout[p.y + 1][p.x - 1] == LevelElement.PIT;

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile above the coordinate p is either accessible or a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if conditions are met
   */
  private static boolean aboveIsInside(Coordinate p, LevelElement[][] layout) {
    try {
      return (aboveIsAccessible(p, layout) || aboveIsHole(p, layout));

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the left of the coordinate p is either accessible or a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if conditions are met
   */
  private static boolean leftIsInside(Coordinate p, LevelElement[][] layout) {
    try {
      return (leftIsAccessible(p, layout) || leftIsHole(p, layout));

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the right of the coordinate p is either accessible or a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if conditions are met
   */
  private static boolean rightIsInside(Coordinate p, LevelElement[][] layout) {
    try {
      return (rightIsAccessible(p, layout) || rightIsHole(p, layout));

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile below the coordinate p is either accessible or a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if conditions are met
   */
  private static boolean belowIsInside(Coordinate p, LevelElement[][] layout) {
    try {
      return (belowIsAccessible(p, layout) || belowIsHole(p, layout));

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the upper right of the coordinate p is either accessible or a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if conditions are met
   */
  private static boolean upperRightIsInside(Coordinate p, LevelElement[][] layout) {
    try {
      return (upperRightIsAccessible(p, layout) || upperRightIsHole(p, layout));

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the bottom right of the coordinate p is either accessible or a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if conditions are met
   */
  private static boolean bottomRightIsInside(Coordinate p, LevelElement[][] layout) {
    try {
      return (bottomRightIsAccessible(p, layout) || bottomRightIsHole(p, layout));

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the bottom left of the coordinate p is either accessible or a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if conditions are met
   */
  private static boolean bottomLeftIsInside(Coordinate p, LevelElement[][] layout) {
    try {
      return (bottomLeftIsAccessible(p, layout) || bottomLeftIsHole(p, layout));

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Checks if tile to the upper left of the coordinate p is either accessible or a hole.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if conditions are met
   */
  private static boolean upperLeftIsInside(Coordinate p, LevelElement[][] layout) {
    try {
      return (upperLeftIsAccessible(p, layout) || upperLeftIsHole(p, layout));

    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
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
