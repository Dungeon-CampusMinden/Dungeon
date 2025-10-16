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

    path = findTexturePathWall(levelPart);
    if (path != null) {
      return new SimpleIPath(prefixPath + path.pathString() + ".png");
    }

    path = findTexturePathTJunction(levelPart);
    if (path != null) return new SimpleIPath(prefixPath + path.pathString() + ".png");

    path = findTexturePathInnerCorner(levelPart);
    if (path != null) {
      return new SimpleIPath(prefixPath + path.pathString() + ".png");
    }

    path = findTexturePathOuterCorner(levelPart);
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
    elementLayout[element.coordinate().y()][element.coordinate().x()] = elementType;
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

  private static IPath findTexturePathDoor(LevelPart levelPart) {
    if (levelPart.element() != LevelElement.DOOR) return null;

    if (belowIsAccessible(levelPart.position, levelPart.layout)) {
      return new SimpleIPath("door/top");
    } else if (leftIsAccessible(levelPart.position, levelPart.layout)) {
      return new SimpleIPath("door/right");
    } else if (rightIsAccessible(levelPart.position, levelPart.layout)) {
      return new SimpleIPath("door/left");
    } else if (aboveIsAccessible(levelPart.position, levelPart.layout)) {
      return new SimpleIPath("door/bottom");
    }

    return new SimpleIPath("door/top");
  }

  private static IPath findTexturePathWall(LevelPart levelPart) {
    if (levelPart.element() != LevelElement.WALL) return null;

    Coordinate p = levelPart.position();
    LevelElement[][] layout = levelPart.layout();

    IPath inner = selectInnerVerticalWallTexture(p, layout);
    if (inner != null) return inner;

    IPath innerH = selectInnerHorizontalWallTexture(p, layout);
    if (innerH != null) return innerH;

    IPath crossEmpty = selectCrossEmptyOfStems(p, layout);
    if (crossEmpty != null) return crossEmpty;

    IPath tEmptyBoth = selectVerticalTEmptyBothTexture(p, layout);
    if (tEmptyBoth != null) return tEmptyBoth;

    IPath hEmptyBoth = selectHorizontalTEmptyBothTexture(p, layout);
    if (hEmptyBoth != null) return hEmptyBoth;

    IPath edgeDouble = selectEdgeDoubleVertical(p, layout);
    if (edgeDouble != null) return edgeDouble;

    if (isInnerTopWall(p, layout)) {
      return new SimpleIPath("wall/wall_inner_top");
    } else if (isOuterBottomWall(p, layout)) {
      return new SimpleIPath("wall/bottom");
    } else if (isOuterTopWall(p, layout)) {
      return new SimpleIPath("wall/wall_outer_top");
    } else if (isLeftWall(p, layout)) {
      return new SimpleIPath("wall/left");
    } else if (isRightWall(p, layout)) {
      return new SimpleIPath("wall/right");
    }
    return null;
  }

  private static IPath selectCrossEmptyOfStems(Coordinate p, LevelElement[][] layout) {
    if (!isVerticalStem(p, layout)) return null;
    Coordinate l = new Coordinate(p.x() - 1, p.y());
    Coordinate r = new Coordinate(p.x() + 1, p.y());
    Coordinate u = new Coordinate(p.x(), p.y() + 1);
    Coordinate d = new Coordinate(p.x(), p.y() - 1);
    boolean hasLR = isVerticalStem(l, layout) && isVerticalStem(r, layout);
    boolean hasUD = isHorizontalStem(u, layout) && isHorizontalStem(d, layout);
    if (hasLR && hasUD) return new SimpleIPath("wall/empty");
    return null;
  }

  private static IPath selectEdgeDoubleVertical(Coordinate p, LevelElement[][] layout) {
    if (!isVerticalStem(p, layout)) return null;

    if (selectHorizontalTEmptyBothTexture(p, layout) != null
        || selectVerticalTEmptyBothTexture(p, layout) != null) {
      return null;
    }

    boolean leftStem = isVerticalStem(new Coordinate(p.x() - 1, p.y()), layout);
    boolean rightStem = isVerticalStem(new Coordinate(p.x() + 1, p.y()), layout);

    if (leftStem && !rightStem) return new SimpleIPath("wall/right_double");
    if (rightStem && !leftStem) return new SimpleIPath("wall/left_double");
    return null;
  }

  private static IPath selectVerticalTEmptyBothTexture(Coordinate p, LevelElement[][] layout) {
    if (isStemCrossCenter(p, layout)) return null;
    if (rendersEmptyAt(p, layout)) return null;

    Coordinate up = new Coordinate(p.x(), p.y() + 1);
    Coordinate down = new Coordinate(p.x(), p.y() - 1);

    boolean sidesAreWallsOrDoors =
        (leftIsWall(p, layout) || leftIsDoor(p, layout))
            && (rightIsWall(p, layout) || rightIsDoor(p, layout));

    if (sidesAreWallsOrDoors
        && aboveIsWall(p, layout)
        && isVerticalStem(up, layout)
        && isEmptyForTJunctionOpen(down, layout)
        && !isInnerVerticalGroup(p, layout)) {
      return new SimpleIPath("wall/t_inner_top_empty_left_right");
    }

    if (sidesAreWallsOrDoors
        && belowIsWall(p, layout)
        && isVerticalStem(down, layout)
        && isEmptyForTJunctionOpen(up, layout)
        && !isInnerVerticalGroup(p, layout)) {
      return new SimpleIPath("wall/t_inner_bottom_empty_left_right");
    }
    return null;
  }

  private static IPath selectHorizontalTEmptyBothTexture(Coordinate p, LevelElement[][] layout) {
    if (rendersEmptyUDAt(p, layout)) return null;

    if (isInnerVerticalGroup(p, layout)) return null;

    boolean upDownAreWallsOrDoors =
        (aboveIsWall(p, layout) || aboveIsDoor(p, layout))
            && (belowIsWall(p, layout) || belowIsDoor(p, layout));

    Coordinate left = new Coordinate(p.x() - 1, p.y());
    Coordinate right = new Coordinate(p.x() + 1, p.y());

    if (upDownAreWallsOrDoors
        && leftIsWall(p, layout)
        && isHorizontalStem(left, layout)
        && isEmptyForTJunctionOpen(right, layout)
        && !isInnerHorizontalGroup(p, layout)) {
      return new SimpleIPath("wall/t_inner_left_empty_top_bottom");
    }

    if (upDownAreWallsOrDoors
        && rightIsWall(p, layout)
        && isHorizontalStem(right, layout)
        && isEmptyForTJunctionOpen(left, layout)
        && !isInnerHorizontalGroup(p, layout)) {
      return new SimpleIPath("wall/t_inner_right_empty_top_bottom");
    }

    return null;
  }

  private static IPath selectInnerVerticalWallTexture(Coordinate p, LevelElement[][] layout) {
    if (!isInnerVerticalGroup(p, layout)) return null;

    boolean leftStem = isVerticalStem(new Coordinate(p.x() - 1, p.y()), layout);
    boolean rightStem = isVerticalStem(new Coordinate(p.x() + 1, p.y()), layout);

    if (leftStem && rightStem) return new SimpleIPath("wall/empty");
    if (leftStem) return new SimpleIPath("wall/right_double");
    if (rightStem) return new SimpleIPath("wall/left_double");
    return new SimpleIPath("wall/wall_right");
  }

  private static IPath selectInnerHorizontalWallTexture(Coordinate p, LevelElement[][] layout) {
    if (!isInnerHorizontalGroup(p, layout)) return null;
    boolean upStem = isHorizontalStem(new Coordinate(p.x(), p.y() + 1), layout);
    boolean downStem = isHorizontalStem(new Coordinate(p.x(), p.y() - 1), layout);
    if (upStem && downStem) return new SimpleIPath("wall/empty");
    if (upStem) return new SimpleIPath("wall/t_inner_top_empty");
    if (downStem) return new SimpleIPath("wall/t_inner_bottom_empty");
    return null;
  }

  private static IPath findTexturePathInnerCorner(LevelPart levelPart) {
    Coordinate p = levelPart.position();
    LevelElement[][] layout = levelPart.layout();

    if (isUpperLeftEmptyCross(p, layout)) {
      return new SimpleIPath("wall/corner_upper_left_empty_cross");
    } else if (isUpperRightEmptyCross(p, layout)) {
      return new SimpleIPath("wall/corner_upper_right_empty_cross");
    } else if (isBottomRightEmptyCross(p, layout)) {
      return new SimpleIPath("wall/corner_bottom_left_empty_cross");
    } else if (isBottomLeftEmptyCross(p, layout)) {
      return new SimpleIPath("wall/corner_bottom_right_empty_cross");
    }

    if (isCrossUpperLeftBottomRight(p, layout)) {
      return new SimpleIPath("wall/wall_cross_upper_left_bottom_right");
    } else if (isCrossUpperRightBottomLeft(p, layout)) {
      return new SimpleIPath("wall/wall_cross_upper_right_bottom_left");
    }

    if (isBottomLeftInnerCorner(p, layout)) {
      Coordinate t = new Coordinate(p.x(), p.y() + 1);
      Coordinate r = new Coordinate(p.x() + 1, p.y());
      if (rendersLeftDoubleAt(t, layout)
          || rendersRightDoubleAt(t, layout)
          || rendersTopDoubleAt(r, layout)
          || rendersBottomDoubleAt(r, layout)) {
        return new SimpleIPath("wall/wall_inner_corner_bottom_left_double");
      }
      return new SimpleIPath("wall/wall_inner_corner_bottom_left");
    } else if (isBottomRightInnerCorner(p, layout)) {
      Coordinate t = new Coordinate(p.x(), p.y() + 1);
      Coordinate l = new Coordinate(p.x() - 1, p.y());
      if (rendersLeftDoubleAt(t, layout)
          || rendersRightDoubleAt(t, layout)
          || rendersTopDoubleAt(l, layout)
          || rendersBottomDoubleAt(l, layout)) {
        return new SimpleIPath("wall/wall_inner_corner_bottom_right_double");
      }
      return new SimpleIPath("wall/wall_inner_corner_bottom_right");
    } else if (isUpperRightInnerCorner(p, layout)) {
      Coordinate b = new Coordinate(p.x(), p.y() - 1);
      Coordinate l = new Coordinate(p.x() - 1, p.y());

      boolean doubleDueToVertical =
          isVerticalStem(b, layout)
              && (endsWithInside(b, layout, 1) ^ endsWithInside(b, layout, -1));

      boolean doubleDueToHorizontal =
          isHorizontalStem(l, layout)
              && (endsWithInsideUD(l, layout, 1) ^ endsWithInsideUD(l, layout, -1));

      if (doubleDueToVertical
          || doubleDueToHorizontal
          || rendersLeftDoubleAt(b, layout)
          || rendersRightDoubleAt(b, layout)
          || rendersTopDoubleAt(l, layout)
          || rendersBottomDoubleAt(l, layout)) {
        return new SimpleIPath("wall/wall_inner_corner_upper_right_double");
      }
      return new SimpleIPath("wall/wall_inner_corner_upper_right");

    } else if (isUpperLeftInnerCorner(p, layout)) {
      Coordinate b = new Coordinate(p.x(), p.y() - 1);
      Coordinate r = new Coordinate(p.x() + 1, p.y());

      boolean doubleDueToVertical =
          isVerticalStem(b, layout)
              && (endsWithInside(b, layout, 1) ^ endsWithInside(b, layout, -1));

      boolean doubleDueToHorizontal =
          isHorizontalStem(r, layout)
              && (endsWithInsideUD(r, layout, 1) ^ endsWithInsideUD(r, layout, -1));

      if (doubleDueToVertical
          || doubleDueToHorizontal
          || rendersLeftDoubleAt(b, layout)
          || rendersRightDoubleAt(b, layout)
          || rendersTopDoubleAt(r, layout)
          || rendersBottomDoubleAt(r, layout)) {
        return new SimpleIPath("wall/wall_inner_corner_upper_left_double");
      }
      return new SimpleIPath("wall/wall_inner_corner_upper_left");
    }

    return null;
  }

  private static IPath findTexturePathOuterCorner(LevelPart levelPart) {
    if (isBottomLeftOuterCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/corner_bottom_left");
    } else if (isBottomRightOuterCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/corner_bottom_right");
    } else if (isUpperRightOuterCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/corner_upper_right");
    } else if (isUpperLeftOuterCorner(levelPart.position(), levelPart.layout())) {
      return new SimpleIPath("wall/corner_upper_left");
    }
    return null;
  }

  private static IPath findTexturePathTJunction(LevelPart lp) {
    Coordinate p = lp.position();
    LevelElement[][] layout = lp.layout();

    if (isInnerTJunctionTop(p, layout)) {
      return new SimpleIPath(selectTopTJunctionTexture(p, layout));
    }
    if (isInnerTJunctionBottom(p, layout)) {
      return new SimpleIPath(selectBottomTJunctionTexture(p, layout));
    }
    if (isInnerTJunctionLeft(p, layout)) {
      return new SimpleIPath(selectLeftTJunctionTexture(p, layout));
    }
    if (isInnerTJunctionRight(p, layout)) {
      return new SimpleIPath(selectRightTJunctionTexture(p, layout));
    }
    if (isTopTJunction(p, layout)) {
      return new SimpleIPath("wall/t_cross_top");
    }
    return null;
  }

  private static String selectTopTJunctionTexture(Coordinate p, LevelElement[][] layout) {
    Coordinate left = new Coordinate(p.x() - 1, p.y());
    Coordinate right = new Coordinate(p.x() + 1, p.y());
    Coordinate up = new Coordinate(p.x(), p.y() + 1);

    boolean leftTriggersTop =
        isTTopEmptyAt(left, layout)
            || isBottomLeftCornerDoubleAt(left, layout)
            || isTopEmptyBothAt(left, layout);

    boolean rightTriggersTop =
        isTTopEmptyAt(right, layout)
            || isBottomRightCornerDoubleAt(right, layout)
            || isTopEmptyBothAt(right, layout);

    if (leftTriggersTop && rendersRightDoubleAt(up, layout)) {
      return "wall/t_inner_top_empty_left";
    }
    if (rightTriggersTop && rendersLeftDoubleAt(up, layout)) {
      return "wall/t_inner_top_empty_right";
    }
    if (isEmptyForTJunctionOpen(up, layout)) {
      return "wall/t_inner_top_empty";
    }
    return "wall/t_inner_top";
  }

  private static String selectBottomTJunctionTexture(Coordinate p, LevelElement[][] layout) {
    Coordinate left = new Coordinate(p.x() - 1, p.y());
    Coordinate right = new Coordinate(p.x() + 1, p.y());
    Coordinate down = new Coordinate(p.x(), p.y() - 1);

    if (hasAdjacentInnerBottomTJunction(p, layout)) return "wall/t_inner_bottom_empty";

    if (isFloorAbove(p, layout)
        && (isUpperLeftCornerDoubleExactAt(left, layout)
            || isUpperRightCornerDoubleExactAt(right, layout))) {
      return "wall/t_inner_bottom_empty";
    }

    boolean leftTriggers =
        isAnyTBottomEmptyAt(left, layout)
            || isUpperLeftCornerDoubleExactAt(left, layout)
            || isTopEmptyBothAt(left, layout);

    boolean rightTriggers =
        isAnyTBottomEmptyAt(right, layout)
            || isUpperRightCornerDoubleExactAt(right, layout)
            || isTopEmptyBothAt(right, layout);

    boolean belowLeftDouble = rendersLeftDoubleAt(down, layout);
    boolean belowRightDouble = rendersRightDoubleAt(down, layout);
    boolean openDownEmpty = isEmptyForTJunctionOpen(down, layout);

    if (leftTriggers && belowRightDouble) return "wall/t_inner_bottom_empty_left";
    if (rightTriggers && belowLeftDouble) return "wall/t_inner_bottom_empty_right";
    if (openDownEmpty) return "wall/t_inner_bottom_empty";
    return "wall/t_inner_bottom";
  }

  private static String selectLeftTJunctionTexture(Coordinate p, LevelElement[][] layout) {
    Coordinate left = new Coordinate(p.x() - 1, p.y());
    if (isInnerHorizontalGroup(left, layout)) {
      boolean hasRowBelow = isHorizontalStem(new Coordinate(left.x(), left.y() - 1), layout);
      boolean hasRowAbove = isHorizontalStem(new Coordinate(left.x(), left.y() + 1), layout);
      if (hasRowBelow && !hasRowAbove) return "wall/t_inner_left_empty_top";
      if (hasRowAbove && !hasRowBelow) return "wall/t_inner_left_empty_bottom";
    }

    Coordinate right = new Coordinate(p.x() + 1, p.y());
    Coordinate down = new Coordinate(p.x(), p.y() - 1);

    boolean belowRightDouble = rendersRightDoubleAt(down, layout);
    boolean rightTopDoubleH = rendersTopDoubleAt(right, layout);

    boolean leftMatches =
        isUpperLeftCornerDoubleAt(left, layout)
            || isTBottomEmptyRightAt(left, layout)
            || isTBottomEmptyAt(left, layout);

    if ((belowRightDouble || rightTopDoubleH) && leftMatches) {
      return "wall/t_inner_left_empty_top";
    }

    Coordinate up = new Coordinate(p.x(), p.y() + 1);
    boolean aboveRightDouble = rendersRightDoubleAt(up, layout);
    boolean rightBottomDoubleH = rendersBottomDoubleAt(right, layout);

    boolean leftMatchesTop =
        isBottomLeftCornerDoubleAt(left, layout)
            || isTTopEmptyAt(left, layout)
            || isTopEmptyBothAt(left, layout);

    if ((aboveRightDouble || rightBottomDoubleH) && leftMatchesTop) {
      return "wall/t_inner_left_empty_bottom";
    }

    return "wall/t_inner_left";
  }

  private static String selectRightTJunctionTexture(Coordinate p, LevelElement[][] layout) {
    Coordinate right = new Coordinate(p.x() + 1, p.y());
    if (isInnerHorizontalGroup(right, layout)) {
      boolean hasRowBelow = isHorizontalStem(new Coordinate(right.x(), right.y() - 1), layout);
      boolean hasRowAbove = isHorizontalStem(new Coordinate(right.x(), right.y() + 1), layout);
      if (hasRowBelow && !hasRowAbove) return "wall/t_inner_right_empty_top";
      if (hasRowAbove && !hasRowBelow) return "wall/t_inner_right_empty_bottom";
    }

    Coordinate left = new Coordinate(p.x() - 1, p.y());
    Coordinate down = new Coordinate(p.x(), p.y() - 1);

    boolean belowLeftDouble = rendersLeftDoubleAt(down, layout);
    boolean leftTopDoubleH = rendersTopDoubleAt(left, layout);

    boolean rightMatches =
        isUpperRightCornerDoubleAt(right, layout)
            || isTBottomEmptyLeftAt(right, layout)
            || isTBottomEmptyAt(right, layout);

    if ((belowLeftDouble || leftTopDoubleH) && rightMatches) {
      return "wall/t_inner_right_empty_top";
    }

    Coordinate up = new Coordinate(p.x(), p.y() + 1);
    boolean aboveLeftDouble = rendersLeftDoubleAt(up, layout);
    boolean leftBottomDoubleH = rendersBottomDoubleAt(left, layout);

    boolean rightMatchesTop =
        isBottomRightCornerDoubleAt(right, layout)
            || isTTopEmptyAt(right, layout)
            || isTopEmptyBothAt(right, layout);

    if ((aboveLeftDouble || leftBottomDoubleH) && rightMatchesTop) {
      return "wall/t_inner_right_empty_bottom";
    }

    return "wall/t_inner_right";
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
    return aboveIsWall(p, layout)
        && rightIsWall(p, layout)
        && !leftIsWall(p, layout)
        && !leftIsDoor(p, layout)
        && upperRightIsInside(p, layout)
        && !belowIsWall(p, layout);
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
    return aboveIsWall(p, layout)
        && leftIsWall(p, layout)
        && !rightIsWall(p, layout)
        && !rightIsDoor(p, layout)
        && upperLeftIsInside(p, layout)
        && !belowIsWall(p, layout);
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
    return belowIsWall(p, layout)
        && leftIsWall(p, layout)
        && !rightIsWall(p, layout)
        && !rightIsDoor(p, layout)
        && bottomLeftIsInside(p, layout)
        && !aboveIsWall(p, layout);
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
    return belowIsWall(p, layout)
        && rightIsWall(p, layout)
        && !leftIsWall(p, layout)
        && !leftIsDoor(p, layout)
        && bottomRightIsInside(p, layout)
        && !aboveIsWall(p, layout);
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
    return ((aboveIsWall(p, layout) || aboveIsDoor(p, layout))
        && (rightIsWall(p, layout) || rightIsDoor(p, layout))
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
    return ((aboveIsWall(p, layout) || aboveIsDoor(p, layout))
        && (leftIsWall(p, layout) || leftIsDoor(p, layout))
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
    return ((belowIsWall(p, layout) || belowIsDoor(p, layout))
        && (leftIsWall(p, layout) || leftIsDoor(p, layout))
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
    return ((belowIsWall(p, layout) || belowIsDoor(p, layout))
        && (rightIsWall(p, layout) || rightIsDoor(p, layout))
        && (leftIsInside(p, layout) && upperRightIsInside(p, layout)
            || aboveIsInside(p, layout) && bottomLeftIsInside(p, layout)
            || aboveIsInside(p, layout) && leftIsInside(p, layout)));
  }

  private static boolean isInsideLayout(int x, int y, LevelElement[][] layout) {
    int h = layout.length;
    int w = layout[0].length;
    return y >= 0 && y < h && x >= 0 && x < w;
  }

  private static LevelElement get(LevelElement[][] layout, int x, int y) {
    return isInsideLayout(x, y, layout) ? layout[y][x] : null;
  }

  private static boolean isBarrier(LevelElement e) {
    return e == LevelElement.WALL || e == LevelElement.DOOR;
  }

  private static boolean isInside(LevelElement e) {
    return e != null && (e.value() || e == LevelElement.PIT || e == LevelElement.HOLE);
  }

  private static boolean wallOrDoorAt(LevelElement[][] layout, int x, int y, int dx, int dy) {
    LevelElement e = get(layout, x + dx, y + dy);
    return isBarrier(e);
  }

  private static boolean insideAt(LevelElement[][] layout, int x, int y, int dx, int dy) {
    LevelElement e = get(layout, x + dx, y + dy);
    return isInside(e);
  }

  private static boolean hasBarrierLR(LevelElement[][] layout, int x, int y) {
    return wallOrDoorAt(layout, x, y, -1, 0) && wallOrDoorAt(layout, x, y, 1, 0);
  }

  private static boolean hasBarrierUD(LevelElement[][] layout, int x, int y) {
    return wallOrDoorAt(layout, x, y, 0, 1) && wallOrDoorAt(layout, x, y, 0, -1);
  }

  private static boolean rendersEmptyAt(Coordinate p, LevelElement[][] layout) {
    if (!isInnerVerticalGroup(p, layout)) return false;
    boolean leftStem = isVerticalStem(new Coordinate(p.x() - 1, p.y()), layout);
    boolean rightStem = isVerticalStem(new Coordinate(p.x() + 1, p.y()), layout);
    return leftStem && rightStem;
  }

  private static boolean rendersEmptyUDAt(Coordinate p, LevelElement[][] layout) {
    if (!isInnerHorizontalGroup(p, layout)) return false;
    boolean upStem = isHorizontalStem(new Coordinate(p.x(), p.y() + 1), layout);
    boolean downStem = isHorizontalStem(new Coordinate(p.x(), p.y() - 1), layout);
    return upStem && downStem;
  }

  private static boolean rendersDoubleAt(Coordinate p, LevelElement[][] layout, int ox, int oy) {
    if (ox == 0) {
      if (!isInnerHorizontalGroup(p, layout)) return false;
      boolean posStem = isHorizontalStem(new Coordinate(p.x() + ox, p.y() + oy), layout);
      boolean negStem = isHorizontalStem(new Coordinate(p.x() - ox, p.y() - oy), layout);
      return posStem && !negStem;
    } else {
      if (!isInnerVerticalGroup(p, layout)) return false;
      boolean posStem = isVerticalStem(new Coordinate(p.x() + ox, p.y() + oy), layout);
      boolean negStem = isVerticalStem(new Coordinate(p.x() - ox, p.y() - oy), layout);
      return posStem && !negStem;
    }
  }

  private static boolean rendersLeftDoubleAt(Coordinate p, LevelElement[][] layout) {
    return rendersDoubleAt(p, layout, 1, 0);
  }

  private static boolean rendersRightDoubleAt(Coordinate p, LevelElement[][] layout) {
    return rendersDoubleAt(p, layout, -1, 0);
  }

  private static boolean rendersTopDoubleAt(Coordinate p, LevelElement[][] layout) {
    return rendersDoubleAt(p, layout, 0, 1);
  }

  private static boolean rendersBottomDoubleAt(Coordinate p, LevelElement[][] layout) {
    return rendersDoubleAt(p, layout, 0, -1);
  }

  private static boolean isVerticalStem(Coordinate p, LevelElement[][] layout) {
    int x = p.x();
    int y = p.y();
    LevelElement self = get(layout, x, y);
    return self == LevelElement.WALL && hasBarrierUD(layout, x, y);
  }

  private static boolean isHorizontalStem(Coordinate p, LevelElement[][] layout) {
    int x = p.x();
    int y = p.y();
    LevelElement self = get(layout, x, y);
    return self == LevelElement.WALL && hasBarrierLR(layout, x, y);
  }

  private static boolean isEmptyCross(
      Coordinate p, LevelElement[][] layout, int vdx, int vdy, int hdx, int hdy, int ddx, int ddy) {
    Coordinate v = new Coordinate(p.x() + vdx, p.y() + vdy);
    Coordinate h = new Coordinate(p.x() + hdx, p.y() + hdy);
    Coordinate d = new Coordinate(p.x() + ddx, p.y() + ddy);
    return isVerticalStem(v, layout) && isHorizontalStem(h, layout) && rendersEmptyAt(d, layout);
  }

  private static boolean isUpperLeftEmptyCross(Coordinate p, LevelElement[][] layout) {
    return isEmptyCross(p, layout, 0, 1, 1, 0, -1, -1);
  }

  private static boolean isUpperRightEmptyCross(Coordinate p, LevelElement[][] layout) {
    return isEmptyCross(p, layout, 0, 1, -1, 0, 1, -1);
  }

  private static boolean isBottomRightEmptyCross(Coordinate p, LevelElement[][] layout) {
    return isEmptyCross(p, layout, 0, -1, -1, 0, 1, 1);
  }

  private static boolean isBottomLeftEmptyCross(Coordinate p, LevelElement[][] layout) {
    return isEmptyCross(p, layout, 0, -1, 1, 0, -1, 1);
  }

  private static boolean isStemCrossCenter(Coordinate p, LevelElement[][] layout) {
    if (!isVerticalStem(p, layout)) return false;
    Coordinate l = new Coordinate(p.x() - 1, p.y());
    Coordinate r = new Coordinate(p.x() + 1, p.y());
    Coordinate u = new Coordinate(p.x(), p.y() + 1);
    Coordinate d = new Coordinate(p.x(), p.y() - 1);
    boolean hasLR = isVerticalStem(l, layout) && isVerticalStem(r, layout);
    boolean hasUD = isHorizontalStem(u, layout) && isHorizontalStem(d, layout);
    return hasLR && hasUD;
  }

  private static boolean isEmptyForTJunctionOpen(Coordinate p, LevelElement[][] layout) {
    return rendersEmptyAt(p, layout) || rendersEmptyUDAt(p, layout) || isStemCrossCenter(p, layout);
  }

  private static boolean endsWithInside(Coordinate p, LevelElement[][] layout, int horizontalStep) {
    int x = p.x();
    int y = p.y();
    while (true) {
      x += horizontalStep;
      if (!isInsideLayout(x, y, layout)) return false;
      boolean stemHere = get(layout, x, y) == LevelElement.WALL && hasBarrierUD(layout, x, y);
      if (!stemHere) return isInside(get(layout, x, y));
    }
  }

  private static boolean endsWithInsideUD(Coordinate p, LevelElement[][] layout, int verticalStep) {
    int x = p.x();
    int y = p.y();
    while (true) {
      y += verticalStep;
      if (!isInsideLayout(x, y, layout)) return false;
      boolean stemHere = get(layout, x, y) == LevelElement.WALL && hasBarrierLR(layout, x, y);
      if (!stemHere) return isInside(get(layout, x, y));
    }
  }

  private static boolean isInnerVerticalGroup(Coordinate p, LevelElement[][] layout) {
    return isVerticalStem(p, layout)
        && endsWithInside(p, layout, -1)
        && endsWithInside(p, layout, 1);
  }

  private static boolean isInnerHorizontalGroup(Coordinate p, LevelElement[][] layout) {
    return isHorizontalStem(p, layout)
        && endsWithInsideUD(p, layout, -1)
        && endsWithInsideUD(p, layout, 1);
  }

  private static boolean isInnerTJunction(Coordinate p, LevelElement[][] layout, int ix, int iy) {
    int x = p.x();
    int y = p.y();
    if (!insideAt(layout, x, y, ix, iy)) return false;
    boolean openIsWall = wallOrDoorAt(layout, x, y, ix, iy);
    int walls = 0;
    if (wallOrDoorAt(layout, x, y, 0, 1)) walls++;
    if (wallOrDoorAt(layout, x, y, 1, 0)) walls++;
    if (wallOrDoorAt(layout, x, y, 0, -1)) walls++;
    if (wallOrDoorAt(layout, x, y, -1, 0)) walls++;
    return !openIsWall && walls == 3;
  }

  private static boolean isInnerTJunctionTop(Coordinate p, LevelElement[][] layout) {
    return isInnerTJunction(p, layout, 0, -1);
  }

  private static boolean isInnerTJunctionBottom(Coordinate p, LevelElement[][] layout) {
    return isInnerTJunction(p, layout, 0, 1);
  }

  private static boolean isInnerTJunctionLeft(Coordinate p, LevelElement[][] layout) {
    return isInnerTJunction(p, layout, 1, 0);
  }

  private static boolean isInnerTJunctionRight(Coordinate p, LevelElement[][] layout) {
    return isInnerTJunction(p, layout, -1, 0);
  }

  private static boolean isOuterTJunctionOpenVert(
      Coordinate p, LevelElement[][] layout, int openSign) {
    int x = p.x();
    int y = p.y();
    boolean sides = hasBarrierLR(layout, x, y);
    boolean stem = wallOrDoorAt(layout, x, y, 0, -openSign);
    boolean open = !wallOrDoorAt(layout, x, y, 0, openSign);
    boolean insideSides =
        insideAt(layout, x, y, -1, -openSign) && insideAt(layout, x, y, 1, -openSign);
    return sides && stem && open && insideSides;
  }

  private static boolean isTopTJunction(Coordinate p, LevelElement[][] layout) {
    return isOuterTJunctionOpenVert(p, layout, 1);
  }

  private static boolean isEmptyBothAt(Coordinate p, LevelElement[][] layout, int sign) {
    int x = p.x();
    int y = p.y();
    if (!hasBarrierLR(layout, x, y)) return false;
    if (isInnerVerticalGroup(p, layout)) return false;
    boolean wallInSignDir = (sign > 0) ? aboveIsWall(p, layout) : belowIsWall(p, layout);
    Coordinate toward = new Coordinate(x, y + sign);
    Coordinate opposite = new Coordinate(x, y - sign);
    return wallInSignDir && isVerticalStem(toward, layout) && rendersEmptyAt(opposite, layout);
  }

  private static boolean isTopEmptyBothAt(Coordinate p, LevelElement[][] layout) {
    return isEmptyBothAt(p, layout, 1);
  }

  private static boolean isBottomEmptyBothAt(Coordinate p, LevelElement[][] layout) {
    return isEmptyBothAt(p, layout, -1);
  }

  private static boolean isTEmptyAt(Coordinate p, LevelElement[][] layout, int sign) {
    if (!isInsideLayout(p.x(), p.y(), layout)) return false;
    boolean t = (sign > 0) ? isInnerTJunctionTop(p, layout) : isInnerTJunctionBottom(p, layout);
    if (!t) return false;
    Coordinate c = new Coordinate(p.x(), p.y() + sign);
    return rendersEmptyAt(c, layout);
  }

  private static boolean isTTopEmptyAt(Coordinate p, LevelElement[][] layout) {
    return isTEmptyAt(p, layout, 1);
  }

  private static boolean isTBottomEmptyAt(Coordinate p, LevelElement[][] layout) {
    return isTEmptyAt(p, layout, -1);
  }

  private static boolean isCornerDoubleAt(Coordinate p, LevelElement[][] layout, int sx, int sy) {
    if (!isInsideLayout(p.x(), p.y(), layout)) return false;
    boolean isCorner =
        (sx == -1 && sy == 1 && isUpperLeftInnerCorner(p, layout))
            || (sx == 1 && sy == 1 && isUpperRightInnerCorner(p, layout))
            || (sx == -1 && sy == -1 && isBottomLeftInnerCorner(p, layout))
            || (sx == 1 && sy == -1 && isBottomRightInnerCorner(p, layout));
    if (!isCorner) return false;
    Coordinate n = new Coordinate(p.x(), p.y() - sy);
    return rendersAnyDoubleAt(n, layout);
  }

  private static boolean isUpperLeftCornerDoubleAt(Coordinate p, LevelElement[][] layout) {
    return isCornerDoubleAt(p, layout, -1, 1);
  }

  private static boolean isUpperRightCornerDoubleAt(Coordinate p, LevelElement[][] layout) {
    return isCornerDoubleAt(p, layout, 1, 1);
  }

  private static boolean isBottomLeftCornerDoubleAt(Coordinate p, LevelElement[][] layout) {
    return isCornerDoubleAt(p, layout, -1, -1);
  }

  private static boolean isBottomRightCornerDoubleAt(Coordinate p, LevelElement[][] layout) {
    return isCornerDoubleAt(p, layout, 1, -1);
  }

  private static boolean rendersAnyDoubleAt(Coordinate p, LevelElement[][] layout) {
    return rendersLeftDoubleAt(p, layout) || rendersRightDoubleAt(p, layout);
  }

  private static boolean isTBottomEmptySideAt(Coordinate p, LevelElement[][] layout, int sideSign) {
    if (!isInsideLayout(p.x(), p.y(), layout)) return false;
    if (!isInnerTJunctionBottom(p, layout)) return false;
    Coordinate side = new Coordinate(p.x() + sideSign, p.y());
    Coordinate down = new Coordinate(p.x(), p.y() - 1);
    boolean cornerDouble =
        (sideSign == 1 && isUpperRightCornerDoubleAt(side, layout))
            || (sideSign == -1 && isUpperLeftCornerDoubleAt(side, layout));
    boolean sideTriggers =
        isTBottomEmptyAt(side, layout) || cornerDouble || isTopEmptyBothAt(side, layout);
    return sideTriggers
        && (sideSign == 1 ? rendersLeftDoubleAt(down, layout) : rendersRightDoubleAt(down, layout));
  }

  private static boolean isTBottomEmptyRightAt(Coordinate p, LevelElement[][] layout) {
    return isTBottomEmptySideAt(p, layout, 1);
  }

  private static boolean isTBottomEmptyLeftAt(Coordinate p, LevelElement[][] layout) {
    return isTBottomEmptySideAt(p, layout, -1);
  }

  private static boolean isFloorAbove(Coordinate p, LevelElement[][] layout) {
    LevelElement a = get(layout, p.x(), p.y() + 1);
    return a == LevelElement.FLOOR;
  }

  private static boolean isAnyTBottomEmptyAt(Coordinate c, LevelElement[][] layout) {
    return isTBottomEmptyAt(c, layout)
        || isTBottomEmptyLeftAt(c, layout)
        || isTBottomEmptyRightAt(c, layout);
  }

  private static boolean xorEndsVertical(Coordinate stem, LevelElement[][] layout) {
    return isVerticalStem(stem, layout)
        && (endsWithInside(stem, layout, 1) ^ endsWithInside(stem, layout, -1));
  }

  private static boolean xorEndsHorizontal(Coordinate stem, LevelElement[][] layout) {
    return isHorizontalStem(stem, layout)
        && (endsWithInsideUD(stem, layout, 1) ^ endsWithInsideUD(stem, layout, -1));
  }

  private static boolean isUpperLeftCornerDoubleExactAt(Coordinate c, LevelElement[][] layout) {
    Coordinate b = new Coordinate(c.x(), c.y() - 1);
    Coordinate r = new Coordinate(c.x() + 1, c.y());
    boolean v = xorEndsVertical(b, layout);
    boolean h = xorEndsHorizontal(r, layout);
    boolean rendersDouble =
        rendersLeftDoubleAt(b, layout)
            || rendersRightDoubleAt(b, layout)
            || rendersTopDoubleAt(r, layout)
            || rendersBottomDoubleAt(r, layout);
    return isUpperLeftInnerCorner(c, layout) && (v || h || rendersDouble);
  }

  private static boolean isUpperRightCornerDoubleExactAt(Coordinate c, LevelElement[][] layout) {
    Coordinate b = new Coordinate(c.x(), c.y() - 1);
    Coordinate l = new Coordinate(c.x() - 1, c.y());
    boolean v = xorEndsVertical(b, layout);
    boolean h = xorEndsHorizontal(l, layout);
    boolean rendersDouble =
        rendersLeftDoubleAt(b, layout)
            || rendersRightDoubleAt(b, layout)
            || rendersTopDoubleAt(l, layout)
            || rendersBottomDoubleAt(l, layout);
    return isUpperRightInnerCorner(c, layout) && (v || h || rendersDouble);
  }

  private static boolean hasAdjacentInnerBottomTJunction(Coordinate p, LevelElement[][] layout) {
    Coordinate left = new Coordinate(p.x() - 1, p.y());
    Coordinate right = new Coordinate(p.x() + 1, p.y());
    return isInnerTJunctionBottom(left, layout) || isInnerTJunctionBottom(right, layout);
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
    return ((aboveIsWall(p, layout) || aboveIsDoor(p, layout))
            && (belowIsWall(p, layout) || belowIsDoor(p, layout)))
        && !rightIsInside(p, layout)
        && !rightIsWall(p, layout)
        && !rightIsDoor(p, layout)
        && (leftIsInside(p, layout) || leftIsWall(p, layout) || leftIsDoor(p, layout));
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
    return ((aboveIsWall(p, layout) || aboveIsDoor(p, layout))
            && (belowIsWall(p, layout) || belowIsDoor(p, layout)))
        && !leftIsInside(p, layout)
        && !leftIsWall(p, layout)
        && !leftIsDoor(p, layout)
        && (rightIsInside(p, layout) || rightIsWall(p, layout) || rightIsDoor(p, layout));
  }

  /**
   * Checks if tile with coordinate p should be a top wall. Tile has to have walls to the left and
   * right and an inside tile (accessible or hole) below.
   *
   * @param p coordinate to check
   * @param layout The level
   * @return true if all conditions are met
   */
  public static boolean isOuterTopWall(Coordinate p, LevelElement[][] layout) {
    return (leftIsWall(p, layout) || leftIsDoor(p, layout))
        && (rightIsWall(p, layout) || rightIsDoor(p, layout))
        && !aboveIsInside(p, layout)
        && !aboveIsWall(p, layout)
        && !aboveIsDoor(p, layout)
        && belowIsInside(p, layout);
  }

  private static boolean isInnerTopWall(Coordinate p, LevelElement[][] layout) {
    return (leftIsWall(p, layout) || leftIsDoor(p, layout))
        && (rightIsWall(p, layout) || rightIsDoor(p, layout))
        && aboveIsInside(p, layout)
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
  public static boolean isOuterBottomWall(Coordinate p, LevelElement[][] layout) {
    return (leftIsWall(p, layout) || leftIsDoor(p, layout))
        && (rightIsWall(p, layout) || rightIsDoor(p, layout))
        && !belowIsInside(p, layout)
        && !belowIsWall(p, layout)
        && !belowIsDoor(p, layout)
        && (aboveIsInside(p, layout)
            || (aboveIsWall(p, layout)
                && (upperLeftIsInside(p, layout) || upperRightIsInside(p, layout))));
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
      return layout[p.y() + 1][p.x()] == LevelElement.WALL;

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
      return layout[p.y() - 1][p.x()] == LevelElement.WALL;

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
      return layout[p.y()][p.x() - 1] == LevelElement.WALL;

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
      return layout[p.y()][p.x() + 1] == LevelElement.WALL;

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
      return layout[p.y() + 1][p.x()] == LevelElement.DOOR;

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
      return layout[p.y() - 1][p.x()] == LevelElement.DOOR;

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
      return layout[p.y()][p.x() - 1] == LevelElement.DOOR;

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
      return layout[p.y()][p.x() + 1] == LevelElement.DOOR;

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
      return layout[p.y() + 1][p.x()].value() || layout[p.y() + 1][p.x()] == LevelElement.PIT;

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
      return layout[p.y()][p.x() - 1].value() || layout[p.y()][p.x() - 1] == LevelElement.PIT;

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
      return layout[p.y()][p.x() + 1].value() || layout[p.y()][p.x() + 1] == LevelElement.PIT;

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
      return layout[p.y() - 1][p.x()].value() || layout[p.y() - 1][p.x()] == LevelElement.PIT;

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
      return layout[p.y() + 1][p.x() + 1].value()
          || layout[p.y() + 1][p.x() + 1] == LevelElement.PIT;

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
      return layout[p.y() - 1][p.x() + 1].value()
          || layout[p.y() - 1][p.x() + 1] == LevelElement.PIT;

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
      return layout[p.y() - 1][p.x() - 1].value()
          || layout[p.y() - 1][p.x() - 1] == LevelElement.PIT;

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
      return layout[p.y() + 1][p.x() - 1].value()
          || layout[p.y() + 1][p.x() - 1] == LevelElement.PIT;

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
      return layout[p.y() + 1][p.x()] == LevelElement.HOLE
          || layout[p.y() + 1][p.x()] == LevelElement.PIT;

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
      return layout[p.y()][p.x() - 1] == LevelElement.HOLE
          || layout[p.y()][p.x() - 1] == LevelElement.PIT;

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
      return layout[p.y()][p.x() + 1] == LevelElement.HOLE
          || layout[p.y()][p.x() + 1] == LevelElement.PIT;

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
      return layout[p.y() - 1][p.x()] == LevelElement.HOLE
          || layout[p.y() - 1][p.x()] == LevelElement.PIT;

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
      return layout[p.y() + 1][p.x() + 1] == LevelElement.HOLE
          || layout[p.y() + 1][p.x() + 1] == LevelElement.PIT;

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
      return layout[p.y() - 1][p.x() + 1] == LevelElement.HOLE
          || layout[p.y() - 1][p.x() + 1] == LevelElement.PIT;

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
      return layout[p.y() - 1][p.x() - 1] == LevelElement.HOLE
          || layout[p.y() - 1][p.x() - 1] == LevelElement.PIT;

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
      return layout[p.y() + 1][p.x() - 1] == LevelElement.HOLE
          || layout[p.y() + 1][p.x() - 1] == LevelElement.PIT;

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
