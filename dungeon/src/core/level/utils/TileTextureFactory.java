package core.level.utils;

import core.level.Tile;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/** WTF? . */
public class TileTextureFactory {

  public enum Axis {
    VERTICAL,
    HORIZONTAL
  }

  public enum Corner {
    UR,
    UL,
    BR,
    BL
  }

  /**
   * Checks which texture must be used for the passed field based on the surrounding fields.
   *
   * @param levelPart a part of a level
   * @return Path to texture
   */
  public static IPath findTexturePath(LevelPart levelPart) {
    IPath resolved = resolvePrimaryPath(levelPart);
    return applyIsolatedWallFallback(levelPart, resolved);
  }

  private static IPath resolvePrimaryPath(LevelPart levelPart) {
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

    return new SimpleIPath(prefixPath + "floor/empty.png");
  }

  private static IPath applyIsolatedWallFallback(LevelPart levelPart, IPath resolvedFullPath) {
    if (resolvedFullPath == null) return resolvedFullPath;
    String s = resolvedFullPath.pathString();
    if (s != null
        && (s.endsWith("/wall/cross.png") || s.matches(".*/wall/corner_.*_empty_cross\\.png$")))
      return resolvedFullPath;

    if (!isVisibleWallPath(resolvedFullPath)) return resolvedFullPath;

    LevelElement[][] layout = levelPart.layout();
    Coordinate p = levelPart.position();
    int[][] dirs = new int[][] {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

    boolean hasVisibleWallNeighbor = false;
    for (int[] d : dirs) {
      int nx = p.x() + d[0];
      int ny = p.y() + d[1];
      if (!isInsideLayout(nx, ny, layout)) continue;
      LevelElement ne = get(layout, nx, ny);
      if (ne == null) continue;
      LevelPart neighbor = new LevelPart(ne, levelPart.design(), layout, new Coordinate(nx, ny));
      IPath neighborPath = resolvePrimaryPath(neighbor);
      if (isVisibleWallPath(neighborPath)) {
        hasVisibleWallNeighbor = true;
        break;
      }
    }

    if (hasVisibleWallNeighbor) return resolvedFullPath;

    String basePrefix = "dungeon/" + levelPart.design().name().toLowerCase() + "/";
    return new SimpleIPath(basePrefix + "floor/empty.png");
  }

  private static boolean isVisibleWallPath(IPath fullPath) {
    if (fullPath == null) return false;
    String s = fullPath.pathString();
    if (s == null) return false;
    boolean isWall = s.contains("/wall/");
    boolean isEmptyWall = s.endsWith("/wall/empty.png");
    return isWall && !isEmptyWall;
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

    if (isDiagonalFloorCross(p, layout)) {
      return new SimpleIPath("wall/cross");
    }

    int holeCount = trueHolesAround(p, layout);
    if (holeCount >= 1
        && orthoOnlyWallDoorExitOrTrueHole(p, layout)
        && !hasPitOrthogonally(p, layout)) {
      return new SimpleIPath("wall/cross");
    }

    if (isInnerEmptyCorner(p, layout, Corner.BR)) {
      return new SimpleIPath("wall/corner_bottom_right_inner_empty");
    }
    if (isInnerEmptyCorner(p, layout, Corner.BL)) {
      return new SimpleIPath("wall/corner_bottom_left_inner_empty");
    }
    if (isInnerEmptyCorner(p, layout, Corner.UL)) {
      return new SimpleIPath("wall/corner_upper_left_inner_empty");
    }
    if (isInnerEmptyCorner(p, layout, Corner.UR)) {
      return new SimpleIPath("wall/corner_upper_right_inner_empty");
    }

    if (isTopOutsideAndBelowNotFloor(p, layout))
      return new SimpleIPath("wall/t_inner_bottom_empty_left_right");

    if (isBottomOutsideAndAboveNotFloor(p, layout))
      return new SimpleIPath("wall/t_inner_top_empty_left_right");

    if (isRightOutsideAndLeftNotFloor(p, layout))
      return new SimpleIPath("wall/t_inner_left_empty_top_bottom");

    if (isLeftOutsideAndRightNotFloor(p, layout))
      return new SimpleIPath("wall/t_inner_right_empty_top_bottom");

    if (isAtBorder(p, layout) && !hasAdjacentFloor(p, layout)) {
      return new SimpleIPath("wall/empty");
    }

    if (topEmptyLeftRightCase(p, layout))
      return new SimpleIPath("wall/t_inner_top_empty_left_right");

    if (isBottomEmptyLeftRightCase(p, layout))
      return new SimpleIPath("wall/t_inner_bottom_empty_left_right");

    if (isLeftTEmptyTopBottomCase(p, layout))
      return new SimpleIPath("wall/t_inner_left_empty_top_bottom");

    if (isRightTEmptyTopBottomCase(p, layout))
      return new SimpleIPath("wall/t_inner_right_empty_top_bottom");

    IPath inner = selectInnerWallTexture(p, layout, Axis.VERTICAL);
    if (inner != null) return inner;

    IPath innerH = selectInnerWallTexture(p, layout, Axis.HORIZONTAL);
    if (innerH != null) return innerH;

    IPath crossEmpty = selectCrossEmptyOfStems(p, layout);
    if (crossEmpty != null) return crossEmpty;

    if (isInnerTopWall(p, layout)) {
      return new SimpleIPath("wall/wall_inner_top");
    }
    return null;
  }

  private static IPath selectCrossEmptyOfStems(Coordinate p, LevelElement[][] layout) {
    if (!isStem(p, layout, Axis.VERTICAL)) return null;

    Neighbors n = Neighbors.of(p, layout);
    boolean hasLR =
        isStem(n.getLeft(), layout, Axis.VERTICAL) && isStem(n.getRight(), layout, Axis.VERTICAL);
    boolean hasUD =
        isStem(n.getUp(), layout, Axis.HORIZONTAL) && isStem(n.getDown(), layout, Axis.HORIZONTAL);

    if (hasLR && hasUD) {
      return new SimpleIPath("wall/empty");
    } else {
      return null;
    }
  }

  private static IPath selectInnerWallTexture(Coordinate p, LevelElement[][] layout, Axis axis) {
    if (!isInnerGroup(p, layout, axis)) return null;

    Neighbors n = Neighbors.of(p, layout);

    if (axis == Axis.VERTICAL) {
      boolean leftStem = isStem(n.getLeft(), layout, Axis.VERTICAL);
      boolean rightStem = isStem(n.getRight(), layout, Axis.VERTICAL);

      if (!leftStem && !rightStem) {
        boolean innerRightT =
            !isInside(n.getRightE())
                && isNotFloor(n.getUpE())
                && isNotFloor(n.getDownE())
                && isFloorOrDoor(n.getLeftE())
                && isFloorOrDoor(n.getUpRightE())
                && isFloorOrDoor(n.getDownRightE());

        boolean innerLeftT =
            !isInside(n.getLeftE())
                && isNotFloor(n.getUpE())
                && isNotFloor(n.getDownE())
                && isFloorOrDoor(n.getRightE())
                && isFloorOrDoor(n.getUpLeftE())
                && isFloorOrDoor(n.getDownLeftE());

        if (innerRightT || innerLeftT) return null;
      }

      String tex;
      if (leftStem && rightStem) {
        tex = "wall/empty";
      } else if (leftStem) {
        tex = "wall/right_double";
      } else if (rightStem) {
        tex = "wall/left_double";
      } else {
        tex = "wall/wall_right";
      }
      return new SimpleIPath(tex);
    } else {
      boolean upStem = isStem(n.getUp(), layout, Axis.HORIZONTAL);
      boolean downStem = isStem(n.getDown(), layout, Axis.HORIZONTAL);

      String tex = null;
      if (upStem && downStem) {
        tex = "wall/empty";
      } else if (upStem) {
        tex = "wall/t_inner_top_empty";
      } else if (downStem) {
        tex = "wall/t_inner_bottom_empty";
      }

      return tex != null ? new SimpleIPath(tex) : null;
    }
  }

  private static IPath findTexturePathInnerCorner(LevelPart levelPart) {
    Coordinate p = levelPart.position();
    LevelElement[][] layout = levelPart.layout();
    Neighbors n = Neighbors.of(p, layout);

    boolean blDouble =
        isFloorOrDoor(n.getLeftE())
            && isFloorOrDoor(n.getDownE())
            && isNotFloor(n.getRightE())
            && isNotFloor(n.getUpE())
            && !isInside(n.getUpRightE());

    boolean brDouble =
        isFloorOrDoor(n.getRightE())
            && isFloorOrDoor(n.getDownE())
            && isNotFloor(n.getLeftE())
            && isNotFloor(n.getUpE())
            && !isInside(n.getUpLeftE());

    boolean urDouble =
        isFloorOrDoor(n.getUpE())
            && isFloorOrDoor(n.getRightE())
            && isNotFloor(n.getDownE())
            && isNotFloor(n.getLeftE())
            && !isInside(n.getDownLeftE());

    boolean ulDouble =
        isFloorOrDoor(n.getUpE())
            && isFloorOrDoor(n.getLeftE())
            && isNotFloor(n.getDownE())
            && isNotFloor(n.getRightE())
            && !isInside(n.getDownRightE());

    if (blDouble) return new SimpleIPath("wall/wall_inner_corner_bottom_left_double");
    if (brDouble) return new SimpleIPath("wall/wall_inner_corner_bottom_right_double");
    if (urDouble) return new SimpleIPath("wall/wall_inner_corner_upper_right_double");
    if (ulDouble) return new SimpleIPath("wall/wall_inner_corner_upper_left_double");

    if (isEmptyCross(p, layout, Corner.UL))
      return new SimpleIPath("wall/corner_upper_left_empty_cross");
    if (isEmptyCross(p, layout, Corner.UR))
      return new SimpleIPath("wall/corner_upper_right_empty_cross");
    if (isEmptyCross(p, layout, Corner.BR))
      return new SimpleIPath("wall/corner_bottom_right_empty_cross");
    if (isEmptyCross(p, layout, Corner.BL))
      return new SimpleIPath("wall/corner_bottom_left_empty_cross");

    if (isBottomLeftInnerCorner(p, layout))
      return selectInnerCornerTexture(p, layout, 1, 1, "bottom_left");
    if (isBottomRightInnerCorner(p, layout))
      return selectInnerCornerTexture(p, layout, -1, 1, "bottom_right");
    if (isUpperRightInnerCorner(p, layout))
      return selectInnerCornerTexture(p, layout, -1, -1, "upper_right");
    if (isUpperLeftInnerCorner(p, layout))
      return selectInnerCornerTexture(p, layout, 1, -1, "upper_left");

    return null;
  }

  private static IPath selectInnerCornerTexture(
      Coordinate p, LevelElement[][] layout, int sx, int sy, String name) {
    Neighbors n = Neighbors.of(p, layout);
    String base = "wall/wall_inner_corner_" + name;

    if (sx == 1 && sy == -1)
      return new SimpleIPath(isInside(n.getDownRightE()) ? base : base + "_double");
    if (sx == -1 && sy == -1)
      return new SimpleIPath(isInside(n.getDownLeftE()) ? base : base + "_double");
    if (sx == 1 && sy == 1)
      return new SimpleIPath(isInside(n.getUpRightE()) ? base : base + "_double");
    if (sx == -1 && sy == 1)
      return new SimpleIPath(isInside(n.getUpLeftE()) ? base : base + "_double");

    return new SimpleIPath(base);
  }

  private static IPath findTexturePathTJunction(LevelPart lp) {
    Coordinate p = lp.position();
    LevelElement[][] layout = lp.layout();

    if (isEmptyCross(p, layout, Corner.UL)
        || isEmptyCross(p, layout, Corner.UR)
        || isEmptyCross(p, layout, Corner.BR)
        || isEmptyCross(p, layout, Corner.BL)) {
      return null;
    }

    Neighbors n = Neighbors.of(p, layout);

    boolean useTopInner =
        isNotFloor(n.getUpE())
            && isFloorOrDoor(n.getUpLeftE())
            && isFloorOrDoor(n.getUpRightE())
            && isNotFloor(n.getLeftE())
            && isNotFloor(n.getRightE())
            && isFloorOrDoor(n.getDownE());
    if (useTopInner) {
      return new SimpleIPath("wall/t_inner_top");
    }

    boolean useBottomInner =
        isNotFloor(n.getDownE())
            && isFloorOrDoor(n.getDownLeftE())
            && isFloorOrDoor(n.getDownRightE())
            && isNotFloor(n.getLeftE())
            && isNotFloor(n.getRightE())
            && isFloorOrDoor(n.getUpE());
    if (useBottomInner) {
      return new SimpleIPath("wall/t_inner_bottom");
    }

    boolean useLeftInner =
        isNotFloor(n.getUpE())
            && isNotFloor(n.getDownE())
            && isNotFloor(n.getLeftE())
            && isFloorOrDoor(n.getRightE())
            && isFloorOrDoor(n.getUpLeftE())
            && isFloorOrDoor(n.getDownLeftE());
    if (useLeftInner) {
      return new SimpleIPath("wall/t_inner_left");
    }

    boolean useRightInner =
        isNotFloor(n.getUpE())
            && isNotFloor(n.getDownE())
            && isFloorOrDoor(n.getLeftE())
            && isNotFloor(n.getRightE())
            && isFloorOrDoor(n.getUpRightE())
            && isFloorOrDoor(n.getDownRightE());
    if (useRightInner) {
      return new SimpleIPath("wall/t_inner_right");
    }

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
    Neighbors n = Neighbors.of(p, layout);

    boolean floorBelow = isFloorOrDoor(n.getDownE());
    boolean upNotFloor = isNotFloor(n.getUpE());
    boolean sidesNotFloor = isNotFloor(n.getLeftE()) && isNotFloor(n.getRightE());

    boolean upLeftFloor = isFloorOrDoor(n.getUpLeftE());
    boolean upRightFloor = isFloorOrDoor(n.getUpRightE());

    boolean bothDiagFloors =
        floorBelow && upNotFloor && sidesNotFloor && upLeftFloor && upRightFloor;

    boolean leftDiagonalCase =
        floorBelow && upNotFloor && sidesNotFloor && upRightFloor && !upLeftFloor;
    boolean rightDiagonalCase =
        floorBelow && upNotFloor && sidesNotFloor && upLeftFloor && !upRightFloor;

    boolean leftCornerDoubleCase =
        floorBelow && sidesNotFloor && isCornerDoubleAt(n.getUp(), layout, Corner.UR);
    boolean rightCornerDoubleCase =
        floorBelow && sidesNotFloor && isCornerDoubleAt(n.getUp(), layout, Corner.UL);

    boolean leftTriggersTop =
        isTTopEmptyAt(n.getLeft(), layout)
            || isCornerDoubleAt(n.getLeft(), layout, Corner.BL)
            || isTopEmptyBothAt(n.getLeft(), layout);
    boolean rightTriggersTop =
        isTTopEmptyAt(n.getRight(), layout)
            || isCornerDoubleAt(n.getRight(), layout, Corner.BR)
            || isTopEmptyBothAt(n.getRight(), layout);

    boolean leftTriggerCase = leftTriggersTop && rendersRightDoubleAt(n.getUp(), layout);
    boolean rightTriggerCase = rightTriggersTop && rendersLeftDoubleAt(n.getUp(), layout);

    boolean newStrictEmpty =
        floorBelow && upNotFloor && sidesNotFloor && !upLeftFloor && !upRightFloor;

    boolean adjEmptyTop = hasAdjacentInnerTopTJunction(p, layout);
    boolean openUpEmpty = isEmptyForTJunctionOpen(n.getUp(), layout);
    boolean upDoubleDueToVertical = xorEnds(n.getUp(), layout, Axis.VERTICAL);

    boolean anyEmptyTop = newStrictEmpty || adjEmptyTop || openUpEmpty || upDoubleDueToVertical;

    String result = "wall/t_inner_top";

    if (bothDiagFloors) {
      result = "wall/t_inner_top";
    } else if (leftDiagonalCase || leftCornerDoubleCase || leftTriggerCase) {
      result = "wall/t_inner_top_empty_left";
    } else if (rightDiagonalCase || rightCornerDoubleCase || rightTriggerCase) {
      result = "wall/t_inner_top_empty_right";
    } else if (anyEmptyTop) {
      result = "wall/t_inner_top_empty";
    }

    if ("wall/t_inner_top".equals(result) && disallowTInnerTop(p, layout)) {
      return "wall/t_inner_top_empty";
    }
    return result;
  }

  private static String selectBottomTJunctionTexture(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);

    boolean floorAbove = isFloorOrDoor(n.getUpE());
    boolean sidesNotFloor = isNotFloor(n.getLeftE()) && isNotFloor(n.getRightE());

    boolean leftDiagonalCase =
        floorAbove
            && isFloorOrDoor(n.getDownRightE())
            && sidesNotFloor
            && isNotFloor(n.getDownLeftE());
    boolean rightDiagonalCase =
        floorAbove
            && isFloorOrDoor(n.getDownLeftE())
            && sidesNotFloor
            && isNotFloor(n.getDownRightE());

    boolean leftCornerDoubleCase =
        floorAbove && sidesNotFloor && isCornerDoubleAt(n.getDown(), layout, Corner.BR);
    boolean rightCornerDoubleCase =
        floorAbove && sidesNotFloor && isCornerDoubleAt(n.getDown(), layout, Corner.BL);

    boolean adjEmptyBottom =
        hasAdjacentInnerBottomTJunction(p, layout)
            || (isFloorAbove(p, layout)
                && (isCornerDoubleAt(n.getLeft(), layout, Corner.UL)
                    || isCornerDoubleAt(n.getRight(), layout, Corner.UR)));

    boolean leftTriggers =
        isAnyTBottomEmptyAt(n.getLeft(), layout)
            || isCornerDoubleAt(n.getLeft(), layout, Corner.UL)
            || isTopEmptyBothAt(n.getLeft(), layout);
    boolean rightTriggers =
        isAnyTBottomEmptyAt(n.getRight(), layout)
            || isCornerDoubleAt(n.getRight(), layout, Corner.UR)
            || isTopEmptyBothAt(n.getRight(), layout);

    boolean belowLeftDouble = rendersLeftDoubleAt(n.getDown(), layout);
    boolean belowRightDouble = rendersRightDoubleAt(n.getDown(), layout);

    boolean leftTriggerCase = leftTriggers && belowRightDouble;
    boolean rightTriggerCase = rightTriggers && belowLeftDouble;

    boolean openDownEmpty = isEmptyForTJunctionOpen(n.getDown(), layout);

    if (leftDiagonalCase || leftCornerDoubleCase) return "wall/t_inner_bottom_empty_left";
    if (rightDiagonalCase || rightCornerDoubleCase) return "wall/t_inner_bottom_empty_right";
    if (adjEmptyBottom) return "wall/t_inner_bottom_empty";
    if (leftTriggerCase) return "wall/t_inner_bottom_empty_left";
    if (rightTriggerCase) return "wall/t_inner_bottom_empty_right";
    if (openDownEmpty) return "wall/t_inner_bottom_empty";

    String base = "wall/t_inner_bottom";
    if ("wall/t_inner_bottom".equals(base) && disallowTInnerBottom(p, layout)) {
      return "wall/t_inner_bottom_empty";
    }
    return base;
  }

  private static String selectLeftTJunctionTexture(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);

    boolean innerGroupLeft = isInnerGroup(n.getLeft(), layout, Axis.HORIZONTAL);
    boolean hasRowBelow =
        innerGroupLeft
            && isStem(
                new Coordinate(n.getLeft().x(), n.getLeft().y() - 1), layout, Axis.HORIZONTAL);
    boolean hasRowAbove =
        innerGroupLeft
            && isStem(
                new Coordinate(n.getLeft().x(), n.getLeft().y() + 1), layout, Axis.HORIZONTAL);
    boolean topByInnerGroup = hasRowBelow && !hasRowAbove;
    boolean bottomByInnerGroup = hasRowAbove && !hasRowBelow;

    boolean sidesNotFloor =
        isNotFloor(n.getLeftE()) && isNotFloor(n.getUpE()) && isNotFloor(n.getDownE());

    boolean topDiagonalCase =
        isFloorOrDoor(n.getRightE())
            && isFloorOrDoor(n.getUpLeftE())
            && sidesNotFloor
            && isNotFloor(n.getDownLeftE());
    boolean bottomDiagonalCase =
        isFloorOrDoor(n.getRightE())
            && isFloorOrDoor(n.getDownLeftE())
            && sidesNotFloor
            && isNotFloor(n.getUpLeftE());

    boolean topCornerDoubleCase =
        isFloorOrDoor(n.getRightE())
            && sidesNotFloor
            && isCornerDoubleAt(n.getLeft(), layout, Corner.UL);
    boolean bottomCornerDoubleCase =
        isFloorOrDoor(n.getRightE())
            && sidesNotFloor
            && isCornerDoubleAt(n.getLeft(), layout, Corner.BL);

    boolean belowRightDouble = rendersRightDoubleAt(n.getDown(), layout);
    boolean rightTopDoubleH = rendersTopDoubleAt(n.getRight(), layout);
    boolean aboveRightDouble = rendersRightDoubleAt(n.getUp(), layout);
    boolean rightBottomDoubleH = rendersBottomDoubleAt(n.getRight(), layout);

    boolean leftMatchesTop =
        isCornerDoubleAt(n.getLeft(), layout, Corner.BL)
            || isTTopEmptyAt(n.getLeft(), layout)
            || isTopEmptyBothAt(n.getLeft(), layout);
    boolean leftMatches =
        isCornerDoubleAt(n.getLeft(), layout, Corner.UL)
            || isTBottomEmptyRightAt(n.getLeft(), layout)
            || isTBottomEmptyAt(n.getLeft(), layout);

    boolean topTriggerCase = (belowRightDouble || rightTopDoubleH) && leftMatches;
    boolean bottomTriggerCase = (aboveRightDouble || rightBottomDoubleH) && leftMatchesTop;

    boolean adjEmptyLeft = hasAdjacentInnerLeftTJunction(p, layout);
    boolean openLeftEmpty = isEmptyForTJunctionOpen(n.getLeft(), layout);

    if (topDiagonalCase || topCornerDoubleCase || topByInnerGroup)
      return "wall/t_inner_left_empty_top";
    if (bottomDiagonalCase || bottomCornerDoubleCase || bottomByInnerGroup)
      return "wall/t_inner_left_empty_bottom";
    if (adjEmptyLeft) return "wall/right_double";
    if (topTriggerCase) return "wall/t_inner_left_empty_top";
    if (bottomTriggerCase) return "wall/t_inner_left_empty_bottom";
    if (openLeftEmpty) return "wall/right_double";

    if (disallowTInnerLeft(p, layout)) return "wall/right_double";
    return "wall/t_inner_left";
  }

  private static String selectRightTJunctionTexture(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);

    boolean innerGroupRight = isInnerGroup(n.getRight(), layout, Axis.HORIZONTAL);
    boolean hasRowBelow =
        innerGroupRight
            && isStem(
                new Coordinate(n.getRight().x(), n.getRight().y() - 1), layout, Axis.HORIZONTAL);
    boolean hasRowAbove =
        innerGroupRight
            && isStem(
                new Coordinate(n.getRight().x(), n.getRight().y() + 1), layout, Axis.HORIZONTAL);
    boolean topByInnerGroup = hasRowBelow && !hasRowAbove;
    boolean bottomByInnerGroup = hasRowAbove && !hasRowBelow;

    boolean sidesNotFloor =
        isNotFloor(n.getRightE()) && isNotFloor(n.getUpE()) && isNotFloor(n.getDownE());

    boolean topDiagonalCase =
        isFloorOrDoor(n.getLeftE())
            && isFloorOrDoor(n.getUpRightE())
            && sidesNotFloor
            && isNotFloor(n.getDownRightE());
    boolean bottomDiagonalCase =
        isFloorOrDoor(n.getLeftE())
            && isFloorOrDoor(n.getDownRightE())
            && sidesNotFloor
            && isNotFloor(n.getUpRightE());

    boolean topCornerDoubleCase =
        isFloorOrDoor(n.getLeftE())
            && sidesNotFloor
            && isCornerDoubleAt(n.getRight(), layout, Corner.UR);
    boolean bottomCornerDoubleCase =
        isFloorOrDoor(n.getLeftE())
            && sidesNotFloor
            && isCornerDoubleAt(n.getRight(), layout, Corner.BR);

    boolean belowLeftDouble = rendersLeftDoubleAt(n.getDown(), layout);
    boolean leftTopDoubleH = rendersTopDoubleAt(n.getLeft(), layout);
    boolean aboveLeftDouble = rendersLeftDoubleAt(n.getUp(), layout);
    boolean leftBottomDoubleH = rendersBottomDoubleAt(n.getLeft(), layout);

    boolean rightMatches =
        isCornerDoubleAt(n.getRight(), layout, Corner.UR)
            || isTBottomEmptyLeftAt(n.getRight(), layout)
            || isTBottomEmptyAt(n.getRight(), layout);
    boolean rightMatchesTop =
        isCornerDoubleAt(n.getRight(), layout, Corner.BR)
            || isTTopEmptyAt(n.getRight(), layout)
            || isTopEmptyBothAt(n.getRight(), layout);

    boolean topTriggerCase = (belowLeftDouble || leftTopDoubleH) && rightMatches;
    boolean bottomTriggerCase = (aboveLeftDouble || leftBottomDoubleH) && rightMatchesTop;

    boolean adjEmptyRight = hasAdjacentInnerRightTJunction(p, layout);
    boolean openRightEmpty = isEmptyForTJunctionOpen(n.getRight(), layout);

    if (topDiagonalCase || topCornerDoubleCase || topByInnerGroup)
      return "wall/t_inner_right_empty_top";
    if (bottomDiagonalCase || bottomCornerDoubleCase || bottomByInnerGroup)
      return "wall/t_inner_right_empty_bottom";
    if (adjEmptyRight) return "wall/left_double";
    if (topTriggerCase) return "wall/t_inner_right_empty_top";
    if (bottomTriggerCase) return "wall/t_inner_right_empty_bottom";
    if (openRightEmpty) return "wall/left_double";

    if (disallowTInnerRight(p, layout)) return "wall/left_double";
    return "wall/t_inner_right";
  }

  private static boolean isDiagonalFloorCross(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    boolean orthoNotFloor =
        isNotFloor(n.getUpE())
            && isNotFloor(n.getDownE())
            && isNotFloor(n.getLeftE())
            && isNotFloor(n.getRightE());
    boolean noPit = !hasPitOrthogonally(p, layout);
    boolean diagsInside =
        isInside(n.getUpLeftE())
            && isInside(n.getUpRightE())
            && isInside(n.getDownLeftE())
            && isInside(n.getDownRightE());
    return orthoNotFloor && noPit && diagsInside;
  }

  private static boolean isWallDoorExitOrTrueHole(LevelElement e) {
    return e == LevelElement.WALL
        || e == LevelElement.HOLE
        || e == LevelElement.DOOR
        || e == LevelElement.EXIT;
  }

  private static boolean orthoOnlyWallDoorExitOrTrueHole(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isWallDoorExitOrTrueHole(n.getUpE())
        && isWallDoorExitOrTrueHole(n.getDownE())
        && isWallDoorExitOrTrueHole(n.getLeftE())
        && isWallDoorExitOrTrueHole(n.getRightE());
  }

  private static int trueHolesAround(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    int h = 0;
    if (n.getUpE() == LevelElement.HOLE) h++;
    if (n.getDownE() == LevelElement.HOLE) h++;
    if (n.getLeftE() == LevelElement.HOLE) h++;
    if (n.getRightE() == LevelElement.HOLE) h++;
    return h;
  }

  private static boolean hasPitOrthogonally(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return n.getUpE() == LevelElement.PIT
        || n.getDownE() == LevelElement.PIT
        || n.getLeftE() == LevelElement.PIT
        || n.getRightE() == LevelElement.PIT;
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

  public static LevelElement get(LevelElement[][] layout, int x, int y) {
    return isInsideLayout(x, y, layout) ? layout[y][x] : null;
  }

  private static boolean isBarrier(LevelElement e) {
    return e == LevelElement.WALL || e == LevelElement.DOOR;
  }

  private static boolean isInside(LevelElement e) {
    return e != null && (e.value() || e == LevelElement.PIT || e == LevelElement.HOLE);
  }

  private static boolean hasBarrier(LevelElement[][] layout, int x, int y, Axis axis) {
    Neighbors n = Neighbors.of(new Coordinate(x, y), layout);
    return axis == Axis.VERTICAL
        ? isBarrier(n.getUpE()) && isBarrier(n.getDownE())
        : isBarrier(n.getLeftE()) && isBarrier(n.getRightE());
  }

  private static boolean isStem(Coordinate p, LevelElement[][] layout, Axis axis) {
    LevelElement self = get(layout, p.x(), p.y());
    if (self != LevelElement.WALL) return false;
    return axis == Axis.VERTICAL
        ? hasBarrier(layout, p.x(), p.y(), Axis.VERTICAL)
        : hasBarrier(layout, p.x(), p.y(), Axis.HORIZONTAL);
  }

  private static boolean isInnerGroup(Coordinate p, LevelElement[][] layout, Axis axis) {
    if (!isStem(p, layout, axis)) return false;
    if (axis == Axis.VERTICAL) {
      return endsWithInsideDir(p, layout, -1, 0, true) && endsWithInsideDir(p, layout, 1, 0, true);
    } else {
      return endsWithInsideDir(p, layout, 0, -1, false)
          && endsWithInsideDir(p, layout, 0, 1, false);
    }
  }

  private static boolean endsWithInsideDir(
      Coordinate p, LevelElement[][] layout, int stepX, int stepY, boolean vertical) {
    int x = p.x();
    int y = p.y();
    while (true) {
      x += stepX;
      y += stepY;
      if (!isInsideLayout(x, y, layout)) return false;
      boolean stemHere =
          get(layout, x, y) == LevelElement.WALL
              && (vertical
                  ? hasBarrier(layout, x, y, Axis.VERTICAL)
                  : hasBarrier(layout, x, y, Axis.HORIZONTAL));
      if (!stemHere) return isInside(get(layout, x, y));
    }
  }

  private static boolean xorEnds(Coordinate stem, LevelElement[][] layout, Axis axis) {
    return isStem(stem, layout, axis)
        && (endsWithInsideDir(
                stem,
                layout,
                axis == Axis.VERTICAL ? 1 : 0,
                axis == Axis.VERTICAL ? 0 : 1,
                axis == Axis.VERTICAL)
            ^ endsWithInsideDir(
                stem,
                layout,
                axis == Axis.VERTICAL ? -1 : 0,
                axis == Axis.VERTICAL ? 0 : -1,
                axis == Axis.VERTICAL));
  }

  private static boolean rendersEmptyAxis(Coordinate p, LevelElement[][] layout, Axis axis) {
    if (!isInnerGroup(p, layout, axis)) return false;
    Neighbors n = Neighbors.of(p, layout);
    if (axis == Axis.VERTICAL) {
      boolean leftStem = isStem(n.getLeft(), layout, Axis.VERTICAL);
      boolean rightStem = isStem(n.getRight(), layout, Axis.VERTICAL);
      return leftStem && rightStem;
    } else {
      boolean upStem = isStem(n.getUp(), layout, Axis.HORIZONTAL);
      boolean downStem = isStem(n.getDown(), layout, Axis.HORIZONTAL);
      return upStem && downStem;
    }
  }

  private static boolean rendersEmptyAt(Coordinate p, LevelElement[][] layout, Axis axis) {
    LevelElement here = get(layout, p.x(), p.y());
    if (here == LevelElement.PIT || here == LevelElement.SKIP || here == LevelElement.HOLE)
      return true;
    return rendersEmptyAxis(p, layout, axis);
  }

  private static boolean rendersDoubleAt(Coordinate p, LevelElement[][] layout, int ox, int oy) {
    if (ox == 0) {
      if (!isInnerGroup(p, layout, Axis.HORIZONTAL)) return false;
      boolean posStem = isStem(new Coordinate(p.x() + ox, p.y() + oy), layout, Axis.HORIZONTAL);
      boolean negStem = isStem(new Coordinate(p.x() - ox, p.y() - oy), layout, Axis.HORIZONTAL);
      return posStem && !negStem;
    } else {
      if (!isInnerGroup(p, layout, Axis.VERTICAL)) return false;
      boolean posStem = isStem(new Coordinate(p.x() + ox, p.y() + oy), layout, Axis.VERTICAL);
      boolean negStem = isStem(new Coordinate(p.x() - ox, p.y() - oy), layout, Axis.VERTICAL);
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

  private static boolean isEmptyCross(Coordinate p, LevelElement[][] layout, Corner corner) {
    Neighbors n = Neighbors.of(p, layout);

    Coordinate vStem = (corner == Corner.UL || corner == Corner.UR) ? n.getUp() : n.getDown();
    Coordinate hStem = (corner == Corner.UL || corner == Corner.BR) ? n.getRight() : n.getLeft();
    Coordinate diagEmpty =
        switch (corner) {
          case UL -> n.getDownLeft();
          case UR -> n.getDownRight();
          case BL -> n.getUpRight();
          case BR -> n.getUpLeft();
        };

    boolean defaultCase =
        isStem(vStem, layout, Axis.VERTICAL)
            && isStem(hStem, layout, Axis.HORIZONTAL)
            && rendersEmptyAt(diagEmpty, layout, Axis.VERTICAL);

    boolean doorOk =
        (corner == Corner.UL || corner == Corner.BR)
            ? rightIsDoor(p, layout)
            : leftIsDoor(p, layout);

    boolean notFloorUD = isNotFloor(n.getUpE()) && isNotFloor(n.getDownE());
    boolean notFloorHorizOpp =
        (corner == Corner.UL || corner == Corner.BR)
            ? isNotFloor(n.getLeftE())
            : isNotFloor(n.getRightE());
    LevelElement diagOppE =
        switch (corner) {
          case UL -> n.getDownLeftE();
          case UR -> n.getDownRightE();
          case BL -> n.getUpRightE();
          case BR -> n.getUpLeftE();
        };
    boolean notFloorDiagOpp = isNotFloor(diagOppE);

    boolean diagFloorsTriple =
        switch (corner) {
          case UL ->
              isFloorOrDoor(n.getUpLeftE())
                  && isFloorOrDoor(n.getUpRightE())
                  && isFloorOrDoor(n.getDownRightE());
          case UR ->
              isFloorOrDoor(n.getUpRightE())
                  && isFloorOrDoor(n.getUpLeftE())
                  && isFloorOrDoor(n.getDownLeftE());
          case BL ->
              isFloorOrDoor(n.getDownLeftE())
                  && isFloorOrDoor(n.getDownRightE())
                  && isFloorOrDoor(n.getUpLeftE());
          case BR ->
              isFloorOrDoor(n.getDownRightE())
                  && isFloorOrDoor(n.getDownLeftE())
                  && isFloorOrDoor(n.getUpRightE());
        };

    boolean altCase =
        doorOk && diagFloorsTriple && notFloorUD && notFloorHorizOpp && notFloorDiagOpp;

    boolean orthoWalls =
        leftIsWall(p, layout)
            && rightIsWall(p, layout)
            && aboveIsWall(p, layout)
            && belowIsWall(p, layout);

    boolean diagWallIsCorrect =
        switch (corner) {
          case UL -> n.getDownLeftE() == LevelElement.WALL;
          case UR -> n.getDownRightE() == LevelElement.WALL;
          case BL -> n.getUpRightE() == LevelElement.WALL;
          case BR -> n.getUpLeftE() == LevelElement.WALL;
        };

    boolean quadCase = orthoWalls && diagWallIsCorrect && diagFloorsTriple;

    return defaultCase || altCase || quadCase;
  }

  private static boolean isStemCrossCenter(Coordinate p, LevelElement[][] layout) {
    if (!isStem(p, layout, Axis.VERTICAL)) return false;
    Neighbors n = Neighbors.of(p, layout);
    boolean hasLR =
        isStem(n.getLeft(), layout, Axis.VERTICAL) && isStem(n.getRight(), layout, Axis.VERTICAL);
    boolean hasUD =
        isStem(n.getUp(), layout, Axis.HORIZONTAL) && isStem(n.getDown(), layout, Axis.HORIZONTAL);
    return hasLR && hasUD;
  }

  private static boolean isEmptyForTJunctionOpen(Coordinate p, LevelElement[][] layout) {
    LevelElement e = get(layout, p.x(), p.y());
    if (e == null || (!isInside(e) && !isBarrier(e))) return true;
    return rendersEmptyAt(p, layout, Axis.VERTICAL)
        || rendersEmptyAt(p, layout, Axis.HORIZONTAL)
        || isStemCrossCenter(p, layout);
  }

  private static boolean isInnerTJunction(Coordinate p, LevelElement[][] layout, int ix, int iy) {
    Neighbors n = Neighbors.of(p, layout);

    LevelElement upE = n.getUpE();
    LevelElement rightE = n.getRightE();
    LevelElement downE = n.getDownE();
    LevelElement leftE = n.getLeftE();

    LevelElement openE;
    Coordinate toward;
    if (ix == 1 && iy == 0) {
      openE = rightE;
      toward = n.getRight();
    } else if (ix == -1 && iy == 0) {
      openE = leftE;
      toward = n.getLeft();
    } else if (ix == 0 && iy == 1) {
      openE = upE;
      toward = n.getUp();
    } else if (ix == 0 && iy == -1) {
      openE = downE;
      toward = n.getDown();
    } else {
      return false;
    }

    if (ix == 1 && iy == 0) {
      if (!isFloorOrDoor(openE)) return false;
    }
    if (ix == -1 && iy == 0) {
      if (!isFloorOrDoor(openE)) return false;
    }
    if (ix == 0 && iy == 1) {
      if (!isFloorOrDoor(openE)) return false;
    }
    if (ix == 0 && iy == -1) {
      if (!isFloorOrDoor(openE)) return false;
    }

    if (!(isInside(openE)
        || (!isBarrier(openE) && !isInside(openE))
        || isEmptyForTJunctionOpen(toward, layout))) return false;

    int closed = 0;
    if (!(ix == 0 && iy == 1) && !isInsideNonDoor(upE)) closed++;
    if (!(ix == 1 && iy == 0) && !isInsideNonDoor(rightE)) closed++;
    if (!(ix == 0 && iy == -1) && !isInsideNonDoor(downE)) closed++;
    if (!(ix == -1 && iy == 0) && !isInsideNonDoor(leftE)) closed++;

    return closed == 3;
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
    Neighbors n = Neighbors.of(p, layout);
    boolean sides = isBarrier(n.getLeftE()) && isBarrier(n.getRightE());
    LevelElement stemE;
    LevelElement openE;
    Coordinate toward;
    if (openSign > 0) {
      stemE = n.getDownE();
      openE = n.getUpE();
      toward = n.getUp();
    } else if (openSign < 0) {
      stemE = n.getUpE();
      openE = n.getDownE();
      toward = n.getDown();
    } else {
      return false;
    }
    boolean stem = isBarrier(stemE);
    boolean open = !isBarrier(openE);
    Neighbors tn = Neighbors.of(toward, layout);
    boolean insideSides = isInside(tn.getLeftE()) && isInside(tn.getRightE());
    return sides && stem && open && insideSides;
  }

  private static boolean isTopTJunction(Coordinate p, LevelElement[][] layout) {
    return isOuterTJunctionOpenVert(p, layout, 1);
  }

  private static boolean isEmptyBothAt(Coordinate p, LevelElement[][] layout, int sign) {
    if (!hasBarrier(layout, p.x(), p.y(), Axis.HORIZONTAL)) return false;
    if (isInnerGroup(p, layout, Axis.VERTICAL)) return false;
    Neighbors n = Neighbors.of(p, layout);
    boolean wallInSignDir;
    Coordinate toward;
    Coordinate opposite;
    if (sign > 0) {
      wallInSignDir = aboveIsWall(p, layout);
      toward = n.getUp();
      opposite = n.getDown();
    } else if (sign < 0) {
      wallInSignDir = belowIsWall(p, layout);
      toward = n.getDown();
      opposite = n.getUp();
    } else {
      return false;
    }
    return wallInSignDir
        && isStem(toward, layout, Axis.VERTICAL)
        && rendersEmptyAt(opposite, layout, Axis.VERTICAL);
  }

  private static boolean isTopEmptyBothAt(Coordinate p, LevelElement[][] layout) {
    return isEmptyBothAt(p, layout, 1);
  }

  private static boolean isTEmptyAt(Coordinate p, LevelElement[][] layout, int sign) {
    if (!isInsideLayout(p.x(), p.y(), layout)) return false;
    boolean t;
    if (sign > 0) {
      t = isInnerTJunctionTop(p, layout);
    } else if (sign < 0) {
      t = isInnerTJunctionBottom(p, layout);
    } else {
      return false;
    }
    if (!t) return false;
    Neighbors n = Neighbors.of(p, layout);
    Coordinate c;
    if (sign > 0) {
      c = n.getUp();
    } else {
      c = n.getDown();
    }
    return rendersEmptyAt(c, layout, Axis.VERTICAL);
  }

  private static boolean isTTopEmptyAt(Coordinate p, LevelElement[][] layout) {
    return isTEmptyAt(p, layout, 1);
  }

  private static boolean isTBottomEmptyAt(Coordinate p, LevelElement[][] layout) {
    return isTEmptyAt(p, layout, -1);
  }

  private static boolean isTBottomEmptySideAt(Coordinate p, LevelElement[][] layout, int sideSign) {
    if (!isInsideLayout(p.x(), p.y(), layout)) return false;
    if (!isInnerTJunctionBottom(p, layout)) return false;
    Neighbors n = Neighbors.of(p, layout);
    Coordinate side;
    boolean cornerDouble;
    if (sideSign == 1) {
      side = n.getRight();
      cornerDouble = isCornerDoubleAt(side, layout, Corner.UR);
    } else if (sideSign == -1) {
      side = n.getLeft();
      cornerDouble = isCornerDoubleAt(side, layout, Corner.UL);
    } else {
      return false;
    }
    Coordinate down = n.getDown();
    boolean sideTriggers =
        isTBottomEmptyAt(side, layout) || cornerDouble || isTopEmptyBothAt(side, layout);
    if (sideSign == 1) {
      return sideTriggers && rendersLeftDoubleAt(down, layout);
    } else {
      return sideTriggers && rendersRightDoubleAt(down, layout);
    }
  }

  private static boolean isTBottomEmptyRightAt(Coordinate p, LevelElement[][] layout) {
    return isTBottomEmptySideAt(p, layout, 1);
  }

  private static boolean isTBottomEmptyLeftAt(Coordinate p, LevelElement[][] layout) {
    return isTBottomEmptySideAt(p, layout, -1);
  }

  private static boolean isFloorAbove(Coordinate p, LevelElement[][] layout) {
    LevelElement a = get(layout, p.x(), p.y() + 1);
    return isFloorOrDoor(a);
  }

  private static boolean isAnyTBottomEmptyAt(Coordinate c, LevelElement[][] layout) {
    return isTBottomEmptyAt(c, layout)
        || isTBottomEmptyLeftAt(c, layout)
        || isTBottomEmptyRightAt(c, layout);
  }

  private static boolean isCornerDoubleAt(Coordinate c, LevelElement[][] layout, Corner corner) {
    int hx;
    int sy;

    switch (corner) {
      case UL -> {
        hx = 1;
        sy = 1;
      }
      case UR -> {
        hx = -1;
        sy = 1;
      }
      case BL -> {
        hx = 1;
        sy = -1;
      }
      case BR -> {
        hx = -1;
        sy = -1;
      }
      default -> {
        return false;
      }
    }

    Coordinate vN = new Coordinate(c.x(), c.y() - sy);
    Coordinate hN = new Coordinate(c.x() + hx, c.y());

    boolean v = xorEnds(vN, layout, Axis.VERTICAL);
    boolean h = xorEnds(hN, layout, Axis.HORIZONTAL);
    boolean rendersDouble =
        rendersLeftDoubleAt(vN, layout)
            || rendersRightDoubleAt(vN, layout)
            || rendersTopDoubleAt(hN, layout)
            || rendersBottomDoubleAt(hN, layout);

    boolean isThatInnerCorner =
        switch (corner) {
          case UL -> isUpperLeftInnerCorner(c, layout);
          case UR -> isUpperRightInnerCorner(c, layout);
          case BL -> isBottomLeftInnerCorner(c, layout);
          case BR -> isBottomRightInnerCorner(c, layout);
        };

    return isThatInnerCorner && (v || h || rendersDouble);
  }

  private static boolean hasAdjacentInnerBottomTJunction(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isInnerTJunctionBottom(n.getLeft(), layout)
        || isInnerTJunctionBottom(n.getRight(), layout);
  }

  private static boolean hasAdjacentInnerTopTJunction(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isInnerTJunctionTop(n.getLeft(), layout) || isInnerTJunctionTop(n.getRight(), layout);
  }

  private static boolean hasAdjacentInnerLeftTJunction(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isInnerTJunctionLeft(n.getUp(), layout) || isInnerTJunctionLeft(n.getDown(), layout);
  }

  private static boolean hasAdjacentInnerRightTJunction(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isInnerTJunctionRight(n.getUp(), layout) || isInnerTJunctionRight(n.getDown(), layout);
  }

  private static boolean isInnerEmptyCorner(Coordinate p, LevelElement[][] layout, int sx, int sy) {
    if (!isInnerEmptyCornerBase(p, layout, sx, sy)) return false;

    Coordinate neighborX = new Coordinate(p.x() - sx, p.y());
    if (isInsideLayout(neighborX.x(), neighborX.y(), layout)
        && isInnerEmptyCornerBase(neighborX, layout, sx, sy)) return false;

    Coordinate neighborY = new Coordinate(p.x(), p.y() + sy);
    if (isInsideLayout(neighborY.x(), neighborY.y(), layout)
        && isInnerEmptyCornerBase(neighborY, layout, sx, sy)) return false;

    return true;
  }

  private static boolean isInnerEmptyCornerBase(
      Coordinate p, LevelElement[][] layout, int sx, int sy) {
    if (get(layout, p.x(), p.y()) != LevelElement.WALL) return false;

    Coordinate diag = new Coordinate(p.x() + sx, p.y() + sy);
    Coordinate diagA = new Coordinate(p.x() - sx, p.y() + sy);
    Coordinate diagB = new Coordinate(p.x() + sx, p.y() - sy);
    Coordinate diagC = new Coordinate(p.x() - sx, p.y() - sy);
    Coordinate orthoX = new Coordinate(p.x() + sx, p.y());
    Coordinate orthoY = new Coordinate(p.x(), p.y() + sy);

    LevelElement orthoXE = get(layout, orthoX.x(), orthoX.y());
    LevelElement orthoYE = get(layout, orthoY.x(), orthoY.y());

    if (orthoXE == LevelElement.DOOR || orthoYE == LevelElement.DOOR) return false;

    boolean diagIsFD = isFloorOrDoor(get(layout, diag.x(), diag.y()));
    boolean orthoXStrictWall = orthoXE == LevelElement.WALL;
    boolean orthoYStrictWall = orthoYE == LevelElement.WALL;
    boolean diagANotF = isNotFloor(get(layout, diagA.x(), diagA.y()));
    boolean diagBNotF = isNotFloor(get(layout, diagB.x(), diagB.y()));
    boolean diagCNotF = isNotFloor(get(layout, diagC.x(), diagC.y()));

    return diagIsFD && orthoXStrictWall && orthoYStrictWall && diagANotF && diagBNotF && diagCNotF;
  }

  private static boolean hasAdjacentFloorOrDoor(Coordinate p, LevelElement[][] layout) {
    return isFloorOrDoor(get(layout, p.x() + 1, p.y()))
        || isFloorOrDoor(get(layout, p.x() - 1, p.y()))
        || isFloorOrDoor(get(layout, p.x(), p.y() + 1))
        || isFloorOrDoor(get(layout, p.x(), p.y() - 1));
  }

  private static boolean isInnerEmptyCorner(Coordinate p, LevelElement[][] layout, Corner corner) {
    if (hasAdjacentFloorOrDoor(p, layout)) return false;

    int sx;
    int sy;

    switch (corner) {
      case UR -> {
        sx = 1;
        sy = 1;
      }
      case UL -> {
        sx = -1;
        sy = 1;
      }
      case BR -> {
        sx = 1;
        sy = -1;
      }
      case BL -> {
        sx = -1;
        sy = -1;
      }
      default -> {
        return false;
      }
    }

    return isInnerEmptyCorner(p, layout, sx, sy);
  }

  private static boolean isAtBorder(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return n.getUpE() == null
        || n.getDownE() == null
        || n.getLeftE() == null
        || n.getRightE() == null;
  }

  private static boolean hasAdjacentFloor(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isFloorOrDoor(n.getUpE())
        || isFloorOrDoor(n.getDownE())
        || isFloorOrDoor(n.getLeftE())
        || isFloorOrDoor(n.getRightE());
  }

  private static boolean topEmptyLeftRightCase(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isNotFloor(n.getUpE())
        && isFloorOrDoor(n.getUpLeftE())
        && isFloorOrDoor(n.getUpRightE())
        && isNotFloor(n.getDownE())
        && n.getLeftE() == LevelElement.WALL
        && n.getRightE() == LevelElement.WALL
        && isNotFloor(n.getDownLeftE())
        && isNotFloor(n.getDownRightE());
  }

  private static boolean isBottomEmptyLeftRightCase(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isNotFloor(n.getDownE())
        && isFloorOrDoor(n.getDownLeftE())
        && isFloorOrDoor(n.getDownRightE())
        && isNotFloor(n.getUpE())
        && n.getLeftE() == LevelElement.WALL
        && n.getRightE() == LevelElement.WALL
        && isNotFloor(n.getUpLeftE())
        && isNotFloor(n.getUpRightE());
  }

  private static boolean isLeftTEmptyTopBottomCase(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isFloorOrDoor(n.getUpLeftE())
        && isFloorOrDoor(n.getDownLeftE())
        && isNotFloor(n.getLeftE())
        && isNotFloor(n.getRightE())
        && isNotFloor(n.getUpRightE())
        && isNotFloor(n.getDownRightE())
        && n.getUpE() == LevelElement.WALL
        && n.getDownE() == LevelElement.WALL;
  }

  private static boolean isRightTEmptyTopBottomCase(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isFloorOrDoor(n.getUpRightE())
        && isFloorOrDoor(n.getDownRightE())
        && isNotFloor(n.getLeftE())
        && isNotFloor(n.getRightE())
        && isNotFloor(n.getUpLeftE())
        && isNotFloor(n.getDownLeftE())
        && n.getUpE() == LevelElement.WALL
        && n.getDownE() == LevelElement.WALL;
  }

  private static boolean rendersEmptyLikeWallAt(Coordinate c, LevelElement[][] layout) {
    LevelElement e = get(layout, c.x(), c.y());
    if (e == LevelElement.PIT || e == LevelElement.SKIP || e == LevelElement.HOLE) return true;
    if (e != LevelElement.WALL) return false;
    if (rendersEmptyAt(c, layout, Axis.VERTICAL)
        || rendersEmptyAt(c, layout, Axis.HORIZONTAL)
        || isStemCrossCenter(c, layout)) {
      return true;
    }
    return isAtBorder(c, layout) && !hasAdjacentFloor(c, layout);
  }

  private static boolean isTopOutsideAndBelowNotFloor(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    Coordinate inner = n.getDown();
    return n.getUpE() == null
        && isNotFloor(n.getDownE())
        && !rendersEmptyLikeWallAt(inner, layout)
        && n.getDownLeftE() == LevelElement.FLOOR
        && n.getDownRightE() == LevelElement.FLOOR;
  }

  private static boolean isBottomOutsideAndAboveNotFloor(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    Coordinate inner = n.getUp();
    return n.getDownE() == null
        && isNotFloor(n.getUpE())
        && !rendersEmptyLikeWallAt(inner, layout)
        && n.getUpLeftE() == LevelElement.FLOOR
        && n.getUpRightE() == LevelElement.FLOOR;
  }

  private static boolean isRightOutsideAndLeftNotFloor(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    Coordinate inner = n.getLeft();
    return n.getRightE() == null
        && isNotFloor(n.getLeftE())
        && !rendersEmptyLikeWallAt(inner, layout)
        && n.getUpLeftE() == LevelElement.FLOOR
        && n.getDownLeftE() == LevelElement.FLOOR;
  }

  private static boolean isLeftOutsideAndRightNotFloor(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    Coordinate inner = n.getRight();
    return n.getLeftE() == null
        && isNotFloor(n.getRightE())
        && !rendersEmptyLikeWallAt(inner, layout)
        && n.getUpRightE() == LevelElement.FLOOR
        && n.getDownRightE() == LevelElement.FLOOR;
  }

  private static boolean disallowTInnerRight(Coordinate p, LevelElement[][] layout) {
    return (get(layout, p.x() + 1, p.y()) == LevelElement.WALL
            && get(layout, p.x() + 1, p.y() - 1) == LevelElement.WALL
            && get(layout, p.x() + 2, p.y() - 1) == LevelElement.FLOOR)
        || (get(layout, p.x() + 1, p.y()) == LevelElement.WALL
            && get(layout, p.x() + 1, p.y() + 1) == LevelElement.WALL
            && get(layout, p.x() + 2, p.y() + 1) == LevelElement.FLOOR);
  }

  private static boolean disallowTInnerLeft(Coordinate p, LevelElement[][] layout) {
    return (get(layout, p.x() - 1, p.y()) == LevelElement.WALL
            && get(layout, p.x() - 1, p.y() - 1) == LevelElement.WALL
            && get(layout, p.x() - 2, p.y() - 1) == LevelElement.FLOOR)
        || (get(layout, p.x() - 1, p.y()) == LevelElement.WALL
            && get(layout, p.x() - 1, p.y() + 1) == LevelElement.WALL
            && get(layout, p.x() - 2, p.y() + 1) == LevelElement.FLOOR);
  }

  private static boolean disallowTInnerTop(Coordinate p, LevelElement[][] layout) {
    return (get(layout, p.x(), p.y() + 1) == LevelElement.WALL
            && get(layout, p.x() + 1, p.y() + 1) == LevelElement.WALL
            && get(layout, p.x() + 1, p.y() + 2) == LevelElement.FLOOR)
        || (get(layout, p.x(), p.y() + 1) == LevelElement.WALL
            && get(layout, p.x() - 1, p.y() + 1) == LevelElement.WALL
            && get(layout, p.x() - 1, p.y() + 2) == LevelElement.FLOOR);
  }

  private static boolean disallowTInnerBottom(Coordinate p, LevelElement[][] layout) {
    return (get(layout, p.x(), p.y() - 1) == LevelElement.WALL
            && get(layout, p.x() + 1, p.y() - 1) == LevelElement.WALL
            && get(layout, p.x() + 1, p.y() - 2) == LevelElement.FLOOR)
        || (get(layout, p.x(), p.y() - 1) == LevelElement.WALL
            && get(layout, p.x() - 1, p.y() - 1) == LevelElement.WALL
            && get(layout, p.x() - 1, p.y() - 2) == LevelElement.FLOOR);
  }

  private static boolean isFloorOrDoor(LevelElement e) {
    return e == LevelElement.FLOOR
        || e == LevelElement.DOOR
        || e == LevelElement.EXIT
        || e == LevelElement.HOLE;
  }

  private static boolean isNotFloor(LevelElement e) {
    return e != LevelElement.FLOOR;
  }

  private static boolean isInsideNonDoor(LevelElement e) {
    return e != LevelElement.DOOR && e != LevelElement.EXIT && isInside(e);
  }

  private static boolean isInnerTopWall(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return (leftIsWall(p, layout) || leftIsDoor(p, layout))
        && (rightIsWall(p, layout) || rightIsDoor(p, layout))
        && isInsideNonDoor(n.getUpE())
        && isInside(n.getDownE());
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
