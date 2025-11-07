package core.level.utils;

import core.level.Tile;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * Resolves texture paths for dungeon tiles based on local neighborhood patterns. Provides
 * prioritized resolvers (floor, door, wall, T-junction, inner corner), plus helper predicates for
 * wall grouping, stems, corners, and emptiness. All returned paths are relative to a design root
 * (e.g., {@code dungeon/<design>/}).
 */
public class TileTextureFactory {

  /**
   * Logical axis used by wall grouping and stem detection. VERTICAL checks up/down neighbors;
   * HORIZONTAL checks left/right neighbors.
   */
  private enum Axis {
    /** Checks vertical alignment by comparing the up and down neighbors. */
    VERTICAL,

    /** Checks horizontal alignment by comparing the left and right neighbors. */
    HORIZONTAL
  }

  /**
   * Represents the four corner quadrants relative to a cell. Used for inner-corner and diagonal
   * texture checks:
   *
   * <ul>
   *   <li>{@code UL} – upper-left
   *   <li>{@code UR} – upper-right
   *   <li>{@code BL} – bottom-left
   *   <li>{@code BR} – bottom-right
   * </ul>
   */
  private enum Corner {
    UR,
    UL,
    BR,
    BL
  }

  /**
   * Cardinal directions with integer step deltas used for neighborhood traversal and
   * orientation-sensitive wall logic.
   *
   * <p>Each direction defines ({@code dx}, {@code dy}) offsets indicating how a coordinate changes
   * when moving one step in that direction.
   */
  private enum Dir {

    /** Moves one step upward (positive Y). */
    UP(0, 1),

    /** Moves one step downward (negative Y). */
    DOWN(0, -1),

    /** Moves one step to the left (negative X). */
    LEFT(-1, 0),

    /** Moves one step to the right (positive X). */
    RIGHT(1, 0);

    /** The x-axis delta applied when stepping in this direction. */
    private final int dx;

    /** The y-axis delta applied when stepping in this direction. */
    private final int dy;

    /**
     * Creates a direction with the given coordinate deltas.
     *
     * @param dx the x-axis delta
     * @param dy the y-axis delta
     */
    Dir(int dx, int dy) {
      this.dx = dx;
      this.dy = dy;
    }

    /**
     * Returns {@code true} if this direction is vertical ({@link #UP} or {@link #DOWN}).
     *
     * @return whether the direction is vertical
     */
    public boolean isVertical() {
      return this == UP || this == DOWN;
    }

    /**
     * Returns {@code true} if this direction is horizontal ({@link #LEFT} or {@link #RIGHT}).
     *
     * @return whether the direction is horizontal
     */
    public boolean isHorizontal() {
      return this == LEFT || this == RIGHT;
    }
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

  /**
   * Resolves the texture path for the given level part by consulting specialized resolvers in
   * priority order: floor → door → wall → T-junction → inner corner. The returned path is prefixed
   * with the active design directory {@code dungeon/<design>/} and suffixed with {@code .png}. If
   * no resolver matches, this method falls back to {@code floor/empty.png}.
   *
   * @param levelPart the level part (element, design, layout, position) to resolve
   * @return a path pointing to the chosen texture within the design root, never {@code null}
   */
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

    path = findTexturePathSingleAdjacentWall(levelPart);
    if (path != null) return new SimpleIPath(prefixPath + path.pathString() + ".png");
    return new SimpleIPath(prefixPath + "floor/empty.png");
  }

  /**
   * Applies a fallback for isolated wall tiles. If the resolved path represents a visible wall but
   * no adjacent cell would also render as a visible wall, this method replaces the texture with the
   * design's `floor/empty.png`. Cross-shaped walls and empty-cross corner walls are exempt from
   * this fallback.
   *
   * @param lp the current level part
   * @param resolved the texture path previously resolved for {@code lp}
   * @return the original {@code resolved} if the wall is not isolated (or is a cross variant);
   *     otherwise a path to {@code floor/empty.png}; returns {@code null} if {@code resolved} is
   *     {@code null}
   */
  private static IPath applyIsolatedWallFallback(LevelPart lp, IPath resolved) {
    if (resolved == null) return null;
    String s = resolved.pathString();
    if (s != null
        && (s.endsWith("/wall/cross.png") || s.matches(".*/wall/corner_.*_empty_cross\\.png$")))
      return resolved;
    if (!isVisibleWallPath(resolved)) return resolved;

    LevelElement[][] layout = lp.layout();
    Coordinate p = lp.position();

    for (Dir d : Dir.values()) {
      int nx = p.x() + d.dx, ny = p.y() + d.dy;
      if (!isInsideLayout(nx, ny, layout)) continue;
      LevelPart n = new LevelPart(get(layout, nx, ny), lp.design(), layout, new Coordinate(nx, ny));
      if (isVisibleWallPath(resolvePrimaryPath(n))) return resolved;
    }
    String base = "dungeon/" + lp.design().name().toLowerCase() + "/";
    return new SimpleIPath(base + "floor/empty.png");
  }

  /**
   * Determines whether the given path points to a wall texture that would be rendered (i.e., it is
   * under a {@code /wall/} directory and is not the explicit {@code wall/empty.png}).
   *
   * @param fullPath a fully constructed path (including design prefix)
   * @return {@code true} if the path denotes a non-empty wall texture; {@code false} otherwise
   */
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
    String prefix = "dungeon/" + designLabel.name().toLowerCase() + "/";
    String ep =
        switch (levelElement) {
          case SKIP -> "wall/empty";
          case FLOOR -> "floor/floor_1";
          case EXIT -> "floor/floor_ladder";
          case HOLE -> "floor/floor_hole";
          case PIT -> "floor/floor_damaged";
          case DOOR -> "door/top";
          case WALL -> "wall/wall_right";
          default -> "floor/empty";
        };
    return new SimpleIPath(prefix + ep + ".png");
  }

  /**
   * Selects a floor-related texture for the given level part when its element is floor-related
   * (floor-like or {@code EXIT}). Handles special variants for holes (chooses {@code floor_hole1}
   * if there is a hole above), and maps common floor elements to their canonical textures.
   *
   * @param levelPart the level part to evaluate
   * @return a floor texture path (without design prefix) or {@code null} if the element is not
   *     floor-related
   */
  private static IPath findTexturePathFloor(LevelPart levelPart) {
    LevelElement e = levelPart.element();
    if (e == LevelElement.HOLE) {
      boolean holeAbove =
          neighborMatches(
              levelPart.position(), levelPart.layout(), Dir.UP, x -> x == LevelElement.HOLE);
      return new SimpleIPath(holeAbove ? "floor/floor_hole1" : "floor/floor_hole");
    }
    return switch (e) {
      case SKIP -> new SimpleIPath("wall/empty");
      case FLOOR -> new SimpleIPath("floor/floor_1");
      case EXIT -> new SimpleIPath("floor/floor_ladder");
      case PIT -> new SimpleIPath("floor/floor_damaged");
      default -> null;
    };
  }

  /**
   * Infers a door texture orientation based on adjacent accessible tiles (walkable or floor-like).
   * If no side matches, defaults to {@code door/top}.
   *
   * @param levelPart the level part expected to be a door
   * @return a door texture path (without design prefix), or {@code null} if the element is not a
   *     door
   */
  private static IPath findTexturePathDoor(LevelPart levelPart) {
    if (levelPart.element() != LevelElement.DOOR) return null;

    if (isInsideDir(levelPart.position, levelPart.layout, Dir.DOWN)) {
      return new SimpleIPath("door/top");
    } else if (isInsideDir(levelPart.position, levelPart.layout, Dir.LEFT)) {
      return new SimpleIPath("door/right");
    } else if (isInsideDir(levelPart.position, levelPart.layout, Dir.RIGHT)) {
      return new SimpleIPath("door/left");
    } else if (isInsideDir(levelPart.position, levelPart.layout, Dir.UP)) {
      return new SimpleIPath("door/bottom");
    }

    return new SimpleIPath("door/top");
  }

  /**
   * Resolves the wall texture for the given level part by evaluating a series of structural
   * patterns (crosses, inner-empty corners, border cases, T-variants, inner groups, stems) against
   * surrounding tiles. Returns the first matching wall variant; if none match, returns {@code
   * null}.
   *
   * @param levelPart the level part to evaluate (must be a WALL element)
   * @return a relative wall texture path (without design prefix), or {@code null} if no wall rule
   *     applies
   */
  private static IPath findTexturePathWall(LevelPart levelPart) {
    if (levelPart.element() != LevelElement.WALL) return null;

    Coordinate p = levelPart.position();
    LevelElement[][] layout = levelPart.layout();

    if (isDiagonalFloorCross(p, layout)) return new SimpleIPath("wall/cross");

    int floorCount = likeFloorAround(p, layout);
    if (floorCount >= 1
        && orthoOnlyWallDoorExitOrLikeFloor(p, layout)
        && !hasFloorLikeOrthogonally(p, layout)) return new SimpleIPath("wall/cross");

    if (outsideAndOppositeNotFloor(p, layout, Dir.UP))
      return new SimpleIPath("wall/t_inner_bottom_empty_left_right");
    if (outsideAndOppositeNotFloor(p, layout, Dir.DOWN))
      return new SimpleIPath("wall/t_inner_top_empty_left_right");
    if (outsideAndOppositeNotFloor(p, layout, Dir.RIGHT))
      return new SimpleIPath("wall/t_inner_left_empty_top_bottom");
    if (outsideAndOppositeNotFloor(p, layout, Dir.LEFT))
      return new SimpleIPath("wall/t_inner_right_empty_top_bottom");

    if (isInnerEmptyCorner(p, layout, Corner.BR))
      return new SimpleIPath("wall/corner_bottom_right_inner_empty");
    if (isInnerEmptyCorner(p, layout, Corner.BL))
      return new SimpleIPath("wall/corner_bottom_left_inner_empty");
    if (isInnerEmptyCorner(p, layout, Corner.UL))
      return new SimpleIPath("wall/corner_upper_left_inner_empty");
    if (isInnerEmptyCorner(p, layout, Corner.UR))
      return new SimpleIPath("wall/corner_upper_right_inner_empty");

    if (isAtBorder(p, layout) && !hasAdjacentFloorOrDoor(p, layout))
      return new SimpleIPath("wall/empty");

    if (hasNoEmptyWallsAround(p, layout)
        && emptyLeftRightCase(p, layout, Dir.UP)
        && (isDiagonalFloorLike(p, layout, Corner.BL) || isDiagonalFloorLike(p, layout, Corner.BR)))
      return new SimpleIPath("wall/corner_upper_left_empty_cross");

    if (hasNoEmptyWallsAround(p, layout)
        && emptyLeftRightCase(p, layout, Dir.DOWN)
        && (isDiagonalFloorLike(p, layout, Corner.UL) || isDiagonalFloorLike(p, layout, Corner.UR)))
      return new SimpleIPath("wall/corner_upper_right_empty_cross");

    if (hasNoEmptyWallsAround(p, layout)
        && tEmptyTopBottomCase(p, layout, Dir.LEFT)
        && (isDiagonalFloorLike(p, layout, Corner.UR) || isDiagonalFloorLike(p, layout, Corner.BR)))
      return new SimpleIPath("wall/corner_bottom_left_empty_cross");

    if (hasNoEmptyWallsAround(p, layout)
        && tEmptyTopBottomCase(p, layout, Dir.RIGHT)
        && (isDiagonalFloorLike(p, layout, Corner.UL) || isDiagonalFloorLike(p, layout, Corner.BL)))
      return new SimpleIPath("wall/corner_bottom_right_empty_cross");

    if (emptyLeftRightCase(p, layout, Dir.UP))
      return new SimpleIPath("wall/t_inner_top_empty_left_right");

    if (emptyLeftRightCase(p, layout, Dir.DOWN))
      return new SimpleIPath("wall/t_inner_bottom_empty_left_right");

    if (tEmptyTopBottomCase(p, layout, Dir.LEFT))
      return new SimpleIPath("wall/t_inner_left_empty_top_bottom");

    if (tEmptyTopBottomCase(p, layout, Dir.RIGHT))
      return new SimpleIPath("wall/t_inner_right_empty_top_bottom");

    IPath inner = selectInnerWallTexture(p, layout, Axis.VERTICAL);
    if (inner != null) return inner;

    IPath innerH = selectInnerWallTexture(p, layout, Axis.HORIZONTAL);
    if (innerH != null) return innerH;

    IPath crossEmpty = selectCrossEmptyOfStems(p, layout);
    if (crossEmpty != null) return crossEmpty;

    if (isInnerTopWall(p, layout)) return new SimpleIPath("wall/wall_inner_top");

    return null;
  }

  /**
   * Selects a wall-end texture for the given level part when exactly one orthogonal neighbor is a
   * {@code WALL} and the other three orthogonal neighbors are not {@code WALL}. The chosen variant
   * corresponds to the side on which the single wall is found:
   *
   * <p>The returned path is relative (without design prefix and file suffix). If the
   * single-adjacent wall condition is not satisfied, this method returns {@code null}.
   *
   * @param lp the level part to evaluate
   * @return a relative wall-end texture path, or {@code null} if no single-adjacent-wall case
   *     applies
   */
  private static IPath findTexturePathSingleAdjacentWall(LevelPart lp) {
    Coordinate p = lp.position();
    LevelElement[][] layout = lp.layout();

    boolean up = isWallDir(p, layout, Dir.UP);
    boolean down = isWallDir(p, layout, Dir.DOWN);
    boolean left = isWallDir(p, layout, Dir.LEFT);
    boolean right = isWallDir(p, layout, Dir.RIGHT);

    if (up && !down && !left && !right) {
      return new SimpleIPath("wall/wall_end_bottom");
    } else if (down && !up && !left && !right) {
      return new SimpleIPath("wall/wall_end_top");
    } else if (right && !up && !down && !left) {
      return new SimpleIPath("wall/wall_end_left");
    } else if (left && !up && !down && !right) {
      return new SimpleIPath("wall/wall_end_right");
    } else {
      return null;
    }
  }

  /**
   * Chooses an empty wall texture when a vertical stem cell is the center of a stem cross (i.e., it
   * has vertical and horizontal stem neighbors on both sides).
   *
   * @param p the coordinate to test (expected to be a WALL)
   * @param layout the level grid
   * @return {@code "wall/empty"} if the cell forms a full cross of stems; otherwise {@code null}
   */
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

  /**
   * Resolves inner-group wall textures for continuous stem segments along the given axis. For
   * vertical groups, prefers double/empty/side variants based on adjacent vertical stems and
   * early-exits on conflicting inner-T conditions. For horizontal groups, maps to
   * empty/top-empty/bottom-empty depending on adjacent horizontal stems.
   *
   * @param p the wall coordinate at which to resolve
   * @param layout the level grid
   * @param axis the grouping axis (VERTICAL or HORIZONTAL)
   * @return a relative wall texture path for the inner-group case, or {@code null} if not
   *     applicable
   */
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

  /**
   * Resolves inner-corner wall textures by inspecting the four corner configurations around the
   * current cell. Prefers double-corner variants when both adjacent sides are inside/floor and the
   * opposite sides are not; otherwise maps to empty-cross or standard inner-corner variants as
   * applicable.
   *
   * @param levelPart the level part to evaluate
   * @return a relative inner-corner wall texture path, or {@code null} if no rule applies
   */
  private static IPath findTexturePathInnerCorner(LevelPart levelPart) {
    Coordinate p = levelPart.position();
    LevelElement[][] layout = levelPart.layout();
    Neighbors n = Neighbors.of(p, layout);

    boolean blDouble =
        isFloorOrDoor(n.getLeftE())
            && isFloorOrDoor(n.getDownE())
            && !isInside(n.getRightE())
            && !isInside(n.getUpE())
            && !isInside(n.getUpRightE());

    boolean brDouble =
        isFloorOrDoor(n.getRightE())
            && isFloorOrDoor(n.getDownE())
            && !isInside(n.getLeftE())
            && !isInside(n.getUpE())
            && !isInside(n.getUpLeftE());

    boolean urDouble =
        isFloorOrDoor(n.getUpE())
            && isFloorOrDoor(n.getRightE())
            && !isInside(n.getDownE())
            && !isInside(n.getLeftE())
            && !isInside(n.getDownLeftE());

    boolean ulDouble =
        isFloorOrDoor(n.getUpE())
            && isFloorOrDoor(n.getLeftE())
            && !isInside(n.getDownE())
            && !isInside(n.getRightE())
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

    if (isInnerCorner(p, layout, Corner.BL))
      return selectInnerCornerTexture(p, layout, 1, 1, "bottom_left");
    if (isInnerCorner(p, layout, Corner.BR))
      return selectInnerCornerTexture(p, layout, -1, 1, "bottom_right");
    if (isInnerCorner(p, layout, Corner.UR))
      return selectInnerCornerTexture(p, layout, -1, -1, "upper_right");
    if (isInnerCorner(p, layout, Corner.UL))
      return selectInnerCornerTexture(p, layout, 1, -1, "upper_left");

    return null;
  }

  /**
   * Picks a specific inner-corner texture (single vs. double) for the named corner, based on
   * whether the diagonal outward from the corner is inside or not.
   *
   * @param p the wall coordinate at the corner center
   * @param layout the level grid
   * @param sx corner x-sign (+1 right, -1 left)
   * @param sy corner y-sign (+1 down, -1 up)
   * @param name corner name suffix used in the texture path
   * @return a relative texture path for the chosen inner-corner variant
   */
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

  /**
   * Resolves T-junction wall textures. First handles simple inner T cases for each orientation,
   * then defers to {@code selectTJunctionTexture} for nuanced variants, and finally checks for the
   * open outer T case. Returns the first match.
   *
   * @param lp the level part to evaluate
   * @return a relative T-junction texture path, or {@code null} if no rule matches
   */
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

    if (isInnerTJunction(p, layout, Dir.UP))
      return new SimpleIPath(selectTJunctionTexture(lp, Dir.UP));
    if (isInnerTJunction(p, layout, Dir.DOWN))
      return new SimpleIPath(selectTJunctionTexture(lp, Dir.DOWN));
    if (isInnerTJunction(p, layout, Dir.LEFT))
      return new SimpleIPath(selectTJunctionTexture(lp, Dir.LEFT));
    if (isInnerTJunction(p, layout, Dir.RIGHT))
      return new SimpleIPath(selectTJunctionTexture(lp, Dir.RIGHT));

    if (isOuterTJunctionOpen(p, layout, Dir.UP)) {
      return new SimpleIPath("wall/t_cross_top");
    }
    return null;
  }

  /**
   * Determines the precise T-junction variant for the given facing direction by analyzing
   * forward/back, side, and diagonal neighbors, as well as adjacent inner-T, double-stem, and
   * open-space conditions.
   *
   * @param lp the level part at the T-junction center (element, design, layout, position)
   * @param facing the open direction of the T (UP, DOWN, LEFT, RIGHT)
   * @return a relative texture path string for the selected T-junction variant
   */
  private static String selectTJunctionTexture(LevelPart lp, Dir facing) {
    Coordinate p = lp.position();
    LevelElement[][] layout = lp.layout();
    Neighbors n = Neighbors.of(p, layout);

    if (facing == Dir.UP || facing == Dir.DOWN) {
      boolean up = facing == Dir.UP;

      LevelElement fwdE = up ? n.getDownE() : n.getUpE();
      LevelElement backE = up ? n.getUpE() : n.getDownE();
      LevelElement diagL = up ? n.getUpLeftE() : n.getDownLeftE();
      LevelElement diagR = up ? n.getUpRightE() : n.getDownRightE();

      boolean floorForward = isFloorOrDoor(fwdE);
      boolean forwardNotFloor = isNotFloor(backE);
      boolean sidesNotFloor = isNotFloor(n.getLeftE()) && isNotFloor(n.getRightE());
      boolean diagLeftFloor = isFloorOrDoor(diagL);
      boolean diagRightFloor = isFloorOrDoor(diagR);

      boolean bothDiagFloors =
          floorForward && forwardNotFloor && sidesNotFloor && diagLeftFloor && diagRightFloor;
      boolean leftDiagonalCase =
          floorForward && forwardNotFloor && sidesNotFloor && diagRightFloor && !diagLeftFloor;
      boolean rightDiagonalCase =
          floorForward && forwardNotFloor && sidesNotFloor && diagLeftFloor && !diagRightFloor;

      Coordinate fwdCoord = up ? n.getUp() : n.getDown();
      boolean fwdEmptyByTexture = rendersWallEmptyAt(lp, fwdCoord);
      Corner leftCorner = up ? Corner.UR : Corner.BR;
      Corner rightCorner = up ? Corner.UL : Corner.BL;

      boolean leftCornerDoubleCase =
          floorForward && sidesNotFloor && isCornerDoubleAt(fwdCoord, layout, leftCorner);
      boolean rightCornerDoubleCase =
          floorForward && sidesNotFloor && isCornerDoubleAt(fwdCoord, layout, rightCorner);

      boolean leftTriggersTop =
          isTEmptyAt(n.getLeft(), layout, Dir.UP)
              || isCornerDoubleAt(n.getLeft(), layout, Corner.BL)
              || isEmptyBothAt(n.getLeft(), layout, Dir.UP);
      boolean rightTriggersTop =
          isTEmptyAt(n.getRight(), layout, Dir.UP)
              || isCornerDoubleAt(n.getRight(), layout, Corner.BR)
              || isEmptyBothAt(n.getRight(), layout, Dir.UP);

      boolean leftTriggersBottom =
          isAnyTBottomEmptyAt(n.getLeft(), layout)
              || isCornerDoubleAt(n.getLeft(), layout, Corner.UL)
              || isEmptyBothAt(n.getLeft(), layout, Dir.UP);
      boolean rightTriggersBottom =
          isAnyTBottomEmptyAt(n.getRight(), layout)
              || isCornerDoubleAt(n.getRight(), layout, Corner.UR)
              || isEmptyBothAt(n.getRight(), layout, Dir.UP);

      boolean leftTriggerCase =
          up
              ? leftTriggersTop && rendersDoubleAt(n.getUp(), layout, Dir.RIGHT)
              : leftTriggersBottom && rendersDoubleAt(n.getDown(), layout, Dir.LEFT);

      boolean rightTriggerCase =
          up
              ? rightTriggersTop && rendersDoubleAt(n.getUp(), layout, Dir.LEFT)
              : rightTriggersBottom && rendersDoubleAt(n.getDown(), layout, Dir.RIGHT);

      Coordinate diagFwdLeft = up ? n.getUpLeft() : n.getDownLeft();
      Coordinate diagFwdRight = up ? n.getUpRight() : n.getDownRight();
      boolean diagFwdLeftEmpty = isEmptyForTJunctionOpen(diagFwdLeft, layout);
      boolean diagFwdRightEmpty = isEmptyForTJunctionOpen(diagFwdRight, layout);
      boolean newStrictEmpty =
          floorForward && forwardNotFloor && sidesNotFloor && diagFwdLeftEmpty && diagFwdRightEmpty;

      boolean adjEmpty =
          up
              ? hasAdjacentInnerTJunction(p, layout, Dir.UP)
              : (hasAdjacentInnerTJunction(p, layout, Dir.DOWN)
                  || (isFloorAbove(p, layout)
                      && (isCornerDoubleAt(n.getLeft(), layout, Corner.UL)
                          || isCornerDoubleAt(n.getRight(), layout, Corner.UR))));

      boolean openEmpty =
          up
              ? isEmptyForTJunctionOpen(n.getUp(), layout)
              : isEmptyForTJunctionOpen(n.getDown(), layout);

      boolean upDoubleDueToVertical = up && xorEnds(n.getUp(), layout, Axis.VERTICAL);

      boolean anyEmptyTop =
          up
              && (newStrictEmpty
                  || adjEmpty
                  || openEmpty
                  || upDoubleDueToVertical
                  || fwdEmptyByTexture);

      boolean anyEmptyBottom = !up && (adjEmpty || openEmpty || fwdEmptyByTexture);

      String base = up ? "top" : "bottom";
      String keep = "wall/t_inner_" + base;
      String empty = "wall/t_inner_" + base + "_empty";
      String emptyLeft = "wall/t_inner_" + base + "_empty_left";
      String emptyRight = "wall/t_inner_" + base + "_empty_right";

      Dir forwardDir = up ? Dir.UP : Dir.DOWN;

      if (bothDiagFloors) return disallowTInner(p, layout, forwardDir) ? empty : keep;
      if (leftDiagonalCase || leftCornerDoubleCase || leftTriggerCase) return emptyLeft;
      if (rightDiagonalCase || rightCornerDoubleCase || rightTriggerCase) return emptyRight;
      if (anyEmptyTop || anyEmptyBottom) return empty;
      if (disallowTInner(p, layout, forwardDir)) return empty;
      return keep;
    } else {
      boolean right = facing == Dir.RIGHT;

      Coordinate side = right ? n.getRight() : n.getLeft();
      LevelElement sideE = right ? n.getRightE() : n.getLeftE();
      LevelElement oppE = right ? n.getLeftE() : n.getRightE();
      LevelElement upRightE = right ? n.getUpRightE() : n.getUpLeftE();
      LevelElement downRightE = right ? n.getDownRightE() : n.getDownLeftE();

      boolean innerGroupSide = isInnerGroup(side, layout, Axis.HORIZONTAL);
      boolean hasRowBelow =
          innerGroupSide && isStem(new Coordinate(side.x(), side.y() - 1), layout, Axis.HORIZONTAL);
      boolean hasRowAbove =
          innerGroupSide && isStem(new Coordinate(side.x(), side.y() + 1), layout, Axis.HORIZONTAL);
      boolean topByInnerGroup = hasRowBelow && !hasRowAbove;
      boolean bottomByInnerGroup = hasRowAbove && !hasRowBelow;

      boolean sidesNotFloor =
          isNotFloor(sideE) && isNotFloor(n.getUpE()) && isNotFloor(n.getDownE());

      boolean topDiagonalCase =
          isFloorOrDoor(oppE) && isFloorOrDoor(upRightE) && sidesNotFloor && isNotFloor(downRightE);
      boolean bottomDiagonalCase =
          isFloorOrDoor(oppE) && isFloorOrDoor(downRightE) && sidesNotFloor && isNotFloor(upRightE);

      Corner topCorner = right ? Corner.UR : Corner.UL;
      Corner bottomCorner = right ? Corner.BR : Corner.BL;

      boolean topCornerDoubleCase =
          isFloorOrDoor(oppE) && sidesNotFloor && isCornerDoubleAt(side, layout, topCorner);
      boolean bottomCornerDoubleCase =
          isFloorOrDoor(oppE) && sidesNotFloor && isCornerDoubleAt(side, layout, bottomCorner);

      boolean belowOppDouble = rendersDoubleAt(n.getDown(), layout, right ? Dir.LEFT : Dir.RIGHT);
      boolean sideTopDoubleH = rendersDoubleAt(side, layout, Dir.UP);
      boolean aboveOppDouble = rendersDoubleAt(n.getUp(), layout, right ? Dir.LEFT : Dir.RIGHT);
      boolean sideBottomDoubleH = rendersDoubleAt(side, layout, Dir.DOWN);

      boolean sideMatches =
          right
              ? isCornerDoubleAt(side, layout, Corner.UR)
                  || isTBottomEmptySideAt(side, layout, Dir.LEFT)
                  || isTEmptyAt(side, layout, Dir.DOWN)
              : isCornerDoubleAt(side, layout, Corner.UL)
                  || isTBottomEmptySideAt(side, layout, Dir.RIGHT)
                  || isTEmptyAt(side, layout, Dir.DOWN);

      boolean sideMatchesTop =
          right
              ? isCornerDoubleAt(side, layout, Corner.BR)
                  || isTEmptyAt(side, layout, Dir.UP)
                  || isEmptyBothAt(side, layout, Dir.UP)
              : isCornerDoubleAt(side, layout, Corner.BL)
                  || isTEmptyAt(side, layout, Dir.UP)
                  || isEmptyBothAt(side, layout, Dir.UP);

      boolean topTriggerCase = (belowOppDouble || sideTopDoubleH) && sideMatches;
      boolean bottomTriggerCase = (aboveOppDouble || sideBottomDoubleH) && sideMatchesTop;

      boolean adjEmpty =
          right
              ? hasAdjacentInnerTJunction(p, layout, Dir.RIGHT)
              : hasAdjacentInnerTJunction(p, layout, Dir.LEFT);
      boolean openEmpty = isEmptyForTJunctionOpen(side, layout);

      if (topDiagonalCase || topCornerDoubleCase || topByInnerGroup || topTriggerCase)
        return right ? "wall/t_inner_right_empty_top" : "wall/t_inner_left_empty_top";

      if (bottomDiagonalCase || bottomCornerDoubleCase || bottomByInnerGroup || bottomTriggerCase)
        return right ? "wall/t_inner_right_empty_bottom" : "wall/t_inner_left_empty_bottom";

      boolean disallow =
          right ? disallowTInner(p, layout, Dir.RIGHT) : disallowTInner(p, layout, Dir.LEFT);

      if (adjEmpty || openEmpty || disallow)
        return right ? "wall/left_double" : "wall/right_double";
      return right ? "wall/t_inner_right" : "wall/t_inner_left";
    }
  }

  /**
   * Checks whether the cell forms a diagonal “inside” cross: all four orthogonal neighbors are not
   * floor, there is no orthogonal PIT, and all four diagonal neighbors are inside
   * (walkable/hole/pit).
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @return {@code true} if the diagonal cross condition holds; otherwise {@code false}
   */
  private static boolean isDiagonalFloorCross(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    boolean orthoNotFloor =
        isNotFloor(n.getUpE())
            && isNotFloor(n.getDownE())
            && isNotFloor(n.getLeftE())
            && isNotFloor(n.getRightE());
    boolean noPit = !hasFloorLikeOrthogonally(p, layout);
    boolean diagsInside =
        isInside(n.getUpLeftE())
            && isInside(n.getUpRightE())
            && isInside(n.getDownLeftE())
            && isInside(n.getDownRightE());
    return orthoNotFloor && noPit && diagsInside;
  }

  /**
   * Verifies that all four orthogonal neighbors of {@code p} are either {@code WALL} or
   * floor-related (i.e. floor, door, exit, or hole).
   *
   * @param p the coordinate to inspect
   * @param layout the level grid
   * @return {@code true} if all four orthogonal neighbors are {@code WALL} or floor-related;
   *     otherwise {@code false}
   */
  private static boolean orthoOnlyWallDoorExitOrLikeFloor(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return ((n.getUpE() == LevelElement.WALL) || isFloorOrDoor(n.getUpE()))
        && ((n.getDownE() == LevelElement.WALL) || isFloorOrDoor(n.getDownE()))
        && ((n.getLeftE() == LevelElement.WALL) || isFloorOrDoor(n.getLeftE()))
        && ((n.getRightE() == LevelElement.WALL) || isFloorOrDoor(n.getRightE()));
  }

  /**
   * Counts orthogonal neighbors of {@code p} that are floor-like elements ({@code FLOOR}, {@code
   * PIT}, or {@code HOLE}).
   *
   * @param p the coordinate to inspect
   * @param layout the level grid
   * @return the number of floor-like tiles among up, down, left, and right (0–4)
   */
  private static int likeFloorAround(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    int h = 0;
    if (isFloorLike(n.getUpE())) h++;
    if (isFloorLike(n.getDownE())) h++;
    if (isFloorLike(n.getLeftE())) h++;
    if (isFloorLike(n.getRightE())) h++;
    return h;
  }

  /**
   * Checks whether any orthogonal neighbor of {@code p} is floor-like ({@code FLOOR}, {@code PIT},
   * or {@code HOLE}).
   *
   * @param p the coordinate to inspect
   * @param layout the level grid
   * @return {@code true} if at least one of up, down, left, or right is floor-like; otherwise
   *     {@code false}
   */
  private static boolean hasFloorLikeOrthogonally(Coordinate p, LevelElement[][] layout) {
    return likeFloorAround(p, layout) > 0;
  }

  /**
   * Determines whether the given wall cell forms an inner corner for the specified corner
   * orientation. Requires a wall/door barrier on both adjacent axes and sufficient interior
   * (inside/diagonal-inside) space in the corner quadrant.
   *
   * @param p the wall coordinate to test
   * @param layout the level grid
   * @param corner the corner orientation (UL, UR, BL, BR)
   * @return {@code true} if the cell constitutes an inner corner of the given orientation
   */
  private static boolean isInnerCorner(Coordinate p, LevelElement[][] layout, Corner corner) {
    boolean vertBarrier =
        switch (corner) {
          case BL, BR -> isWallDir(p, layout, Dir.UP) || isDoorDir(p, layout, Dir.UP);
          case UL, UR -> isWallDir(p, layout, Dir.DOWN) || isDoorDir(p, layout, Dir.DOWN);
        };

    boolean horizBarrier =
        switch (corner) {
          case BL, UL -> isWallDir(p, layout, Dir.RIGHT) || isDoorDir(p, layout, Dir.RIGHT);
          case BR, UR -> isWallDir(p, layout, Dir.LEFT) || isDoorDir(p, layout, Dir.LEFT);
        };

    if (!vertBarrier || !horizBarrier) return false;

    return switch (corner) {
      case BL ->
          (isInsideDir(p, layout, Dir.LEFT) && isDiagonalInside(p, layout, Corner.BR))
              || (isInsideDir(p, layout, Dir.DOWN) && isDiagonalInside(p, layout, Corner.UL))
              || (isInsideDir(p, layout, Dir.DOWN) && isInsideDir(p, layout, Dir.LEFT));
      case BR ->
          (isInsideDir(p, layout, Dir.RIGHT) && isDiagonalInside(p, layout, Corner.BL))
              || (isInsideDir(p, layout, Dir.DOWN) && isDiagonalInside(p, layout, Corner.UR))
              || (isInsideDir(p, layout, Dir.DOWN) && isInsideDir(p, layout, Dir.RIGHT));
      case UR ->
          (isInsideDir(p, layout, Dir.RIGHT) && isDiagonalInside(p, layout, Corner.UL))
              || (isInsideDir(p, layout, Dir.UP) && isDiagonalInside(p, layout, Corner.BR))
              || (isInsideDir(p, layout, Dir.UP) && isInsideDir(p, layout, Dir.RIGHT));
      case UL ->
          (isInsideDir(p, layout, Dir.LEFT) && isDiagonalInside(p, layout, Corner.UR))
              || (isInsideDir(p, layout, Dir.UP) && isDiagonalInside(p, layout, Corner.BL))
              || (isInsideDir(p, layout, Dir.UP) && isInsideDir(p, layout, Dir.LEFT));
    };
  }

  /**
   * Checks whether the given coordinates lie within the bounds of the layout array.
   *
   * @param x the x-index (column)
   * @param y the y-index (row)
   * @param layout the level grid
   * @return {@code true} if (x, y) is within bounds; otherwise {@code false}
   */
  private static boolean isInsideLayout(int x, int y, LevelElement[][] layout) {
    int h = layout.length;
    int w = layout[0].length;
    return y >= 0 && y < h && x >= 0 && x < w;
  }

  /**
   * Safely retrieves the element at (x, y) or {@code null} if the coordinates are out of bounds.
   *
   * @param layout the level grid
   * @param x the x-index (column)
   * @param y the y-index (row)
   * @return the element at (x, y), or {@code null} if outside the layout
   */
  public static LevelElement get(LevelElement[][] layout, int x, int y) {
    return isInsideLayout(x, y, layout) ? layout[y][x] : null;
  }

  /**
   * Indicates whether the element is a blocking barrier for wall logic.
   *
   * @param e the element to test
   * @return {@code true} if {@code e} is WALL or DOOR; otherwise {@code false}
   */
  private static boolean isBarrier(LevelElement e) {
    return e == LevelElement.WALL || e == LevelElement.DOOR;
  }

  /**
   * Determines if the element counts as interior/accessible space for wall rules. Treats walkable
   * tiles and cavities as inside.
   *
   * @param e the element to test (may be {@code null})
   * @return {@code true} if {@code e} is walkable or a PIT/HOLE; otherwise {@code false}
   */
  private static boolean isInside(LevelElement e) {
    return e != null && (e.value() || isFloorLike(e));
  }

  /**
   * Tests whether the cell at (x, y) is bordered by barriers on both sides along the given axis.
   * For VERTICAL, checks up/down; for HORIZONTAL, checks left/right.
   *
   * @param layout the level grid
   * @param x the x-index (column)
   * @param y the y-index (row)
   * @param axis the axis to check (VERTICAL or HORIZONTAL)
   * @return {@code true} if both opposite neighbors along the axis are barriers; otherwise {@code
   *     false}
   */
  private static boolean hasBarrier(LevelElement[][] layout, int x, int y, Axis axis) {
    Neighbors n = Neighbors.of(new Coordinate(x, y), layout);
    return axis == Axis.VERTICAL
        ? isBarrier(n.getUpE()) && isBarrier(n.getDownE())
        : isBarrier(n.getLeftE()) && isBarrier(n.getRightE());
  }

  /**
   * Checks whether the wall at {@code p} is a stem along the given axis, i.e., the tile itself is a
   * WALL and has barriers on both opposite sides along that axis.
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @param axis the axis to evaluate (VERTICAL or HORIZONTAL)
   * @return {@code true} if {@code p} is a wall stem on {@code axis}; otherwise {@code false}
   */
  private static boolean isStem(Coordinate p, LevelElement[][] layout, Axis axis) {
    LevelElement self = get(layout, p.x(), p.y());
    if (self != LevelElement.WALL) return false;
    return axis == Axis.VERTICAL
        ? hasBarrier(layout, p.x(), p.y(), Axis.VERTICAL)
        : hasBarrier(layout, p.x(), p.y(), Axis.HORIZONTAL);
  }

  /**
   * Determines whether the wall stem at {@code p} belongs to an inner group: a continuous stem
   * segment whose ends (both directions along the axis) terminate into inside space.
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @param axis the grouping axis (VERTICAL or HORIZONTAL)
   * @return {@code true} if {@code p} is part of such an inner group; otherwise {@code false}
   */
  private static boolean isInnerGroup(Coordinate p, LevelElement[][] layout, Axis axis) {
    if (!isStem(p, layout, axis)) return false;
    int sx = (axis == Axis.VERTICAL) ? 1 : 0;
    int sy = (axis == Axis.VERTICAL) ? 0 : 1;
    return endsWithInsideDir(p, layout, -sx, -sy, axis)
        && endsWithInsideDir(p, layout, sx, sy, axis);
  }

  /**
   * Walks from {@code p} in the given step direction, continuing while cells are wall stems on
   * {@code axis}, and returns whether the first non-stem encountered is inside.
   *
   * @param p start coordinate
   * @param layout the level grid
   * @param stepX step on x per iteration
   * @param stepY step on y per iteration
   * @param axis the axis used to classify stems
   * @return {@code true} if the segment ends at an inside cell; {@code false} if out of bounds or
   *     not inside
   */
  private static boolean endsWithInsideDir(
      Coordinate p, LevelElement[][] layout, int stepX, int stepY, Axis axis) {
    int x = p.x();
    int y = p.y();
    while (true) {
      x += stepX;
      y += stepY;
      if (!isInsideLayout(x, y, layout)) return false;
      boolean stemHere =
          get(layout, x, y) == LevelElement.WALL
              && (axis == Axis.VERTICAL
                  ? hasBarrier(layout, x, y, Axis.VERTICAL)
                  : hasBarrier(layout, x, y, Axis.HORIZONTAL));
      if (!stemHere) return isInside(get(layout, x, y));
    }
  }

  /**
   * Tests whether a stem’s two axial ends differ in “inside” termination (exclusive-or): exactly
   * one end terminates into inside space.
   *
   * @param stem the stem coordinate to test
   * @param layout the level grid
   * @param axis the axis along which to check the ends
   * @return {@code true} if exactly one end is inside; otherwise {@code false}
   */
  private static boolean xorEnds(Coordinate stem, LevelElement[][] layout, Axis axis) {
    int sx = (axis == Axis.VERTICAL) ? 1 : 0;
    int sy = (axis == Axis.VERTICAL) ? 0 : 1;
    return isStem(stem, layout, axis)
        && (endsWithInsideDir(stem, layout, sx, sy, axis)
            ^ endsWithInsideDir(stem, layout, -sx, -sy, axis));
  }

  /**
   * Indicates whether an inner-group stem at {@code p} renders as empty for the given axis. For
   * vertical groups, both left and right neighbors must be vertical stems; for horizontal groups,
   * both up and down neighbors must be horizontal stems.
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @param axis the axis context for emptiness
   * @return {@code true} if the inner-group configuration renders empty; otherwise {@code false}
   */
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

  /**
   * Returns whether the cell at {@code p} should render as empty either because the tile type is
   * intrinsically empty ({@code SKIP}) or due to axis-based inner-group rules.
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @param axis axis context for inner-group emptiness
   * @return {@code true} if the tile renders as empty; otherwise {@code false}
   */
  private static boolean rendersSkipAt(Coordinate p, LevelElement[][] layout, Axis axis) {
    LevelElement here = get(layout, p.x(), p.y());
    if (here == LevelElement.SKIP) return true;
    return rendersEmptyAxis(p, layout, axis);
  }

  /**
   * Indicates whether an inner-group stem at {@code p} should render the "double" variant when
   * looking toward {@code dir}. A double render occurs if the forward neighbor is a stem on the
   * computed axis and the opposite neighbor is not.
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @param dir the direction to evaluate (sets the axis implicitly)
   * @return {@code true} if the double variant should be used; otherwise {@code false}
   */
  private static boolean rendersDoubleAt(Coordinate p, LevelElement[][] layout, Dir dir) {
    Axis axis = (dir.dx == 0) ? Axis.HORIZONTAL : Axis.VERTICAL;
    if (!isInnerGroup(p, layout, axis)) return false;
    Coordinate pos = new Coordinate(p.x() + dir.dx, p.y() + dir.dy);
    Coordinate neg = new Coordinate(p.x() - dir.dx, p.y() - dir.dy);
    boolean posStem = isStem(pos, layout, axis);
    boolean negStem = isStem(neg, layout, axis);
    return posStem && !negStem;
  }

  /**
   * Detects an "empty cross" configuration for the given corner around {@code p}. Combines stem
   * checks on the orthogonal arms with emptiness at the opposite diagonal, and accepts
   * alternative/quad cases involving doors, walls, and floor triples.
   *
   * @param p the pivot coordinate
   * @param layout the level grid
   * @param corner which corner (UL, UR, BL, BR) to evaluate
   * @return {@code true} if the empty-cross condition holds; otherwise {@code false}
   */
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
            && rendersSkipAt(diagEmpty, layout, Axis.VERTICAL);

    boolean doorOk =
        (corner == Corner.UL || corner == Corner.BR)
            ? isDoorDir(p, layout, Dir.RIGHT)
            : isDoorDir(p, layout, Dir.LEFT);

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
        isWallDir(p, layout, Dir.LEFT)
            && isWallDir(p, layout, Dir.RIGHT)
            && isWallDir(p, layout, Dir.UP)
            && isWallDir(p, layout, Dir.DOWN);

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

  /**
   * Determines whether {@code p} is the center of a cross made entirely of stems: a vertical stem
   * at {@code p} with vertical stems to the left/right and horizontal stems up/down.
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @return {@code true} if {@code p} is a full stem cross center; otherwise {@code false}
   */
  private static boolean isStemCrossCenter(Coordinate p, LevelElement[][] layout) {
    if (!isStem(p, layout, Axis.VERTICAL)) return false;
    Neighbors n = Neighbors.of(p, layout);
    boolean hasLR =
        isStem(n.getLeft(), layout, Axis.VERTICAL) && isStem(n.getRight(), layout, Axis.VERTICAL);
    boolean hasUD =
        isStem(n.getUp(), layout, Axis.HORIZONTAL) && isStem(n.getDown(), layout, Axis.HORIZONTAL);
    return hasLR && hasUD;
  }

  /**
   * Indicates whether {@code p} should be treated as empty space for open T-junction logic. Counts
   * null/outside, non-inside/non-barrier cells as empty, and also treats axis-empty cells (via
   * {@link #rendersSkipAt(Coordinate, LevelElement[][], Axis)}) and stem-cross centers as empty.
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @return {@code true} if {@code p} is considered empty for open T-junction checks; otherwise
   *     {@code false}
   */
  private static boolean isEmptyForTJunctionOpen(Coordinate p, LevelElement[][] layout) {
    LevelElement e = get(layout, p.x(), p.y());
    if (e == null || (!isInside(e) && !isBarrier(e))) return true;
    return rendersSkipAt(p, layout, Axis.VERTICAL)
        || rendersSkipAt(p, layout, Axis.HORIZONTAL)
        || isStemCrossCenter(p, layout);
  }

  /**
   * Determines whether the cell at {@code p} forms an inner T-junction whose opening faces {@code
   * openDir}. Requires a floor/door in the opening direction (or an equivalent open condition) and
   * the other three sides to be closed (non-inside non-door).
   *
   * @param p the candidate T-junction cell
   * @param layout the level grid
   * @param openDir the intended opening direction (UP, DOWN, LEFT, RIGHT)
   * @return {@code true} if {@code p} is an inner T-junction opening toward {@code openDir}
   */
  private static boolean isInnerTJunction(Coordinate p, LevelElement[][] layout, Dir openDir) {
    Neighbors n = Neighbors.of(p, layout);

    LevelElement upE = n.getUpE();
    LevelElement rightE = n.getRightE();
    LevelElement downE = n.getDownE();
    LevelElement leftE = n.getLeftE();

    LevelElement openE;
    Coordinate toward;

    switch (openDir) {
      case LEFT -> {
        openE = rightE;
        toward = n.getRight();
      }
      case RIGHT -> {
        openE = leftE;
        toward = n.getLeft();
      }
      case UP -> {
        openE = downE;
        toward = n.getDown();
      }
      case DOWN -> {
        openE = upE;
        toward = n.getUp();
      }
      default -> {
        return false;
      }
    }

    if (!isFloorOrDoor(openE)) return false;

    if (!(isInside(openE)
        || (!isBarrier(openE) && !isInside(openE))
        || isEmptyForTJunctionOpen(toward, layout))) return false;

    int closed = 0;
    if (openDir != Dir.DOWN && !isInsideNonDoor(upE)) closed++;
    if (openDir != Dir.LEFT && !isInsideNonDoor(rightE)) closed++;
    if (openDir != Dir.UP && !isInsideNonDoor(downE)) closed++;
    if (openDir != Dir.RIGHT && !isInsideNonDoor(leftE)) closed++;

    return closed == 3;
  }

  /**
   * Detects an open outer T-junction facing up or down. Requires barriers on both horizontal sides,
   * a barrier on the stem opposite to the opening, a non-barrier at the opening, and inside space
   * flanking the next tile in the opening direction.
   *
   * @param p the candidate junction cell
   * @param layout the level grid
   * @param openDir the vertical opening direction (UP or DOWN)
   * @return {@code true} if an open outer T-junction is present; otherwise {@code false}
   */
  private static boolean isOuterTJunctionOpen(Coordinate p, LevelElement[][] layout, Dir openDir) {
    if (openDir != Dir.UP && openDir != Dir.DOWN) return false;

    Neighbors n = Neighbors.of(p, layout);
    boolean sides = isBarrier(n.getLeftE()) && isBarrier(n.getRightE());

    LevelElement stemE = (openDir == Dir.UP) ? n.getDownE() : n.getUpE();
    LevelElement openE = (openDir == Dir.UP) ? n.getUpE() : n.getDownE();
    Coordinate toward = (openDir == Dir.UP) ? n.getUp() : n.getDown();

    boolean stem = isBarrier(stemE);
    boolean open = !isBarrier(openE);
    Neighbors tn = Neighbors.of(toward, layout);
    boolean insideSides = isInside(tn.getLeftE()) && isInside(tn.getRightE());
    return sides && stem && open && insideSides;
  }

  /**
   * For vertical directions, checks a horizontal wall segment at {@code p} and returns whether the
   * segment is "empty on the opposite side": there is a wall toward {@code dir}, a vertical stem
   * beyond it, and the opposite side renders empty vertically.
   *
   * @param p the position on the horizontal segment
   * @param layout the level grid
   * @param dir vertical check direction (UP or DOWN)
   * @return {@code true} if the opposite side qualifies as empty under these rules
   */
  private static boolean isEmptyBothAt(Coordinate p, LevelElement[][] layout, Dir dir) {
    if (!hasBarrier(layout, p.x(), p.y(), Axis.HORIZONTAL)) return false;
    if (isInnerGroup(p, layout, Axis.VERTICAL)) return false;

    Neighbors n = Neighbors.of(p, layout);
    boolean wallInSignDir;
    Coordinate toward;
    Coordinate opposite;

    if (dir == Dir.UP) {
      wallInSignDir = isWallDir(p, layout, Dir.UP);
      toward = n.getUp();
      opposite = n.getDown();
    } else if (dir == Dir.DOWN) {
      wallInSignDir = isWallDir(p, layout, Dir.DOWN);
      toward = n.getDown();
      opposite = n.getUp();
    } else {
      return false;
    }

    return wallInSignDir
        && isStem(toward, layout, Axis.VERTICAL)
        && rendersSkipAt(opposite, layout, Axis.VERTICAL);
  }

  /**
   * Checks whether {@code p} is an inner T-junction opening toward {@code dir} and that the
   * immediate tile in that direction renders empty on the vertical axis.
   *
   * @param p the candidate T-junction cell
   * @param layout the level grid
   * @param dir opening direction to test (UP or DOWN)
   * @return {@code true} if the T-junction exists and the forward cell renders empty
   */
  private static boolean isTEmptyAt(Coordinate p, LevelElement[][] layout, Dir dir) {
    if (!isInsideLayout(p.x(), p.y(), layout)) return false;
    if (dir != Dir.UP && dir != Dir.DOWN) return false;

    boolean t =
        (dir == Dir.UP)
            ? isInnerTJunction(p, layout, Dir.UP)
            : isInnerTJunction(p, layout, Dir.DOWN);
    if (!t) return false;

    Neighbors n = Neighbors.of(p, layout);
    Coordinate c = (dir == Dir.UP) ? n.getUp() : n.getDown();
    return rendersSkipAt(c, layout, Axis.VERTICAL);
  }

  /**
   * For a downward-opening inner T at {@code p}, evaluates whether emptiness is triggered from the
   * given horizontal side by nearby T-emptiness, corner-double, or both-empty conditions, and
   * whether the downward neighbor renders a matching double variant.
   *
   * @param p the T-junction cell (must be an inner T opening DOWN)
   * @param layout the level grid
   * @param side the side to evaluate (LEFT or RIGHT)
   * @return {@code true} if the side triggers an empty-bottom condition
   */
  private static boolean isTBottomEmptySideAt(Coordinate p, LevelElement[][] layout, Dir side) {
    if (!isInsideLayout(p.x(), p.y(), layout)) return false;
    if (!isInnerTJunction(p, layout, Dir.DOWN)) return false;
    if (side != Dir.LEFT && side != Dir.RIGHT) return false;

    Neighbors n = Neighbors.of(p, layout);
    Coordinate sideCoord = side == Dir.RIGHT ? n.getRight() : n.getLeft();
    boolean cornerDouble =
        side == Dir.RIGHT
            ? isCornerDoubleAt(sideCoord, layout, Corner.UR)
            : isCornerDoubleAt(sideCoord, layout, Corner.UL);

    Coordinate down = n.getDown();
    boolean sideTriggers =
        isTEmptyAt(sideCoord, layout, Dir.DOWN)
            || cornerDouble
            || isEmptyBothAt(sideCoord, layout, Dir.UP);

    return side == Dir.RIGHT
        ? sideTriggers && rendersDoubleAt(down, layout, Dir.LEFT)
        : sideTriggers && rendersDoubleAt(down, layout, Dir.RIGHT);
  }

  /**
   * Returns whether the cell directly above {@code p} (positive Y) is floor-like
   * (floor/door/exit/hole).
   *
   * @param p the reference coordinate
   * @param layout the level grid
   * @return {@code true} if the cell above is treated as floor/door; otherwise {@code false}
   */
  private static boolean isFloorAbove(Coordinate p, LevelElement[][] layout) {
    LevelElement a = get(layout, p.x(), p.y() + 1);
    return isFloorOrDoor(a);
  }

  /**
   * Aggregates bottom-emptiness checks for a downward-opening T at {@code c}: true if the bottom is
   * empty directly or via either horizontal side’s empty-bottom side rule.
   *
   * @param c the T-junction cell
   * @param layout the level grid
   * @return {@code true} if any bottom-empty condition applies; otherwise {@code false}
   */
  private static boolean isAnyTBottomEmptyAt(Coordinate c, LevelElement[][] layout) {
    return isTEmptyAt(c, layout, Dir.DOWN)
        || isTBottomEmptySideAt(c, layout, Dir.LEFT)
        || isTBottomEmptySideAt(c, layout, Dir.RIGHT);
  }

  /**
   * Determines whether the specified inner corner at {@code c} should render as a "double" corner.
   * True when the location is an inner corner of the given type and at least one adjacent axial
   * stem end XORs to inside or a matching double-stem condition is detected around it.
   *
   * @param c the corner cell to evaluate
   * @param layout the level grid
   * @param corner the corner orientation (UL, UR, BL, BR)
   * @return {@code true} if the corner qualifies for a double variant; otherwise {@code false}
   */
  private static boolean isCornerDoubleAt(Coordinate c, LevelElement[][] layout, Corner corner) {
    Neighbors n = Neighbors.of(c, layout);

    Coordinate vN =
        switch (corner) {
          case UL, UR -> n.getUp();
          case BL, BR -> n.getDown();
        };

    Coordinate hN =
        switch (corner) {
          case UL, BL -> n.getRight();
          case UR, BR -> n.getLeft();
        };

    boolean v = xorEnds(vN, layout, Axis.VERTICAL);
    boolean h = xorEnds(hN, layout, Axis.HORIZONTAL);
    boolean rendersDouble =
        rendersDoubleAt(vN, layout, Dir.LEFT)
            || rendersDoubleAt(vN, layout, Dir.RIGHT)
            || rendersDoubleAt(hN, layout, Dir.UP)
            || rendersDoubleAt(hN, layout, Dir.DOWN);

    boolean isThatInnerCorner = isInnerCorner(c, layout, corner);

    return isThatInnerCorner && (v || h || rendersDouble);
  }

  /**
   * Checks if there is an inner T-junction adjacent to {@code p} that opens in the same {@code
   * openDir}. Looks at the two neighbors perpendicular to the opening.
   *
   * @param p the reference coordinate
   * @param layout the level grid
   * @param openDir opening direction to match (UP, DOWN, LEFT, RIGHT)
   * @return {@code true} if a matching adjacent inner T-junction exists; otherwise {@code false}
   */
  private static boolean hasAdjacentInnerTJunction(
      Coordinate p, LevelElement[][] layout, Dir openDir) {
    Neighbors n = Neighbors.of(p, layout);
    return switch (openDir) {
      case UP ->
          isInnerTJunction(n.getLeft(), layout, Dir.UP)
              || isInnerTJunction(n.getRight(), layout, Dir.UP);
      case DOWN ->
          isInnerTJunction(n.getLeft(), layout, Dir.DOWN)
              || isInnerTJunction(n.getRight(), layout, Dir.DOWN);
      case LEFT ->
          isInnerTJunction(n.getUp(), layout, Dir.LEFT)
              || isInnerTJunction(n.getDown(), layout, Dir.LEFT);
      case RIGHT ->
          isInnerTJunction(n.getUp(), layout, Dir.RIGHT)
              || isInnerTJunction(n.getDown(), layout, Dir.RIGHT);
    };
  }

  /**
   * Returns whether any orthogonal neighbor of {@code p} is considered floor-like
   * (floor/door/exit/hole).
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @return {@code true} if at least one orthogonal neighbor is floor-like; otherwise {@code false}
   */
  private static boolean hasAdjacentFloorOrDoor(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return isFloorOrDoor(n.getUpE())
        || isFloorOrDoor(n.getDownE())
        || isFloorOrDoor(n.getLeftE())
        || isFloorOrDoor(n.getRightE());
  }

  /**
   * Base predicate for detecting an inner-empty-corner configuration at {@code p} in the signed
   * corner direction ({@code sx}, {@code sy}). Requires a WALL at {@code p}, strict WALLs on both
   * orthogonal sides, a floor-like diagonal in the corner, no doors on those orthogonal sides, and
   * non-floor at the three opposite diagonals.
   *
   * @param p the corner candidate
   * @param layout the level grid
   * @param sx corner x-sign (+1 right, -1 left)
   * @param sy corner y-sign (+1 down, -1 up)
   * @return {@code true} if the base inner-empty-corner conditions hold; otherwise {@code false}
   */
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

  /**
   * Determines whether {@code p} forms an inner empty corner of the given orientation. Rejects
   * cases with adjacent floor/door, validates the base pattern for the corner, and ensures neither
   * orthogonal neighbor also forms the same base corner.
   *
   * @param p the corner candidate
   * @param layout the level grid
   * @param corner the corner orientation (UL, UR, BL, BR)
   * @return {@code true} if {@code p} is an inner empty corner of the given type; otherwise {@code
   *     false}
   */
  private static boolean isInnerEmptyCorner(Coordinate p, LevelElement[][] layout, Corner corner) {
    if (hasAdjacentFloorOrDoor(p, layout)) return false;

    int sx, sy;
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

    if (!isInnerEmptyCornerBase(p, layout, sx, sy)) return false;

    Neighbors n = Neighbors.of(p, layout);
    Coordinate neighborX =
        (corner == Corner.UR || corner == Corner.BR) ? n.getLeft() : n.getRight();
    Coordinate neighborY = (corner == Corner.UR || corner == Corner.UL) ? n.getUp() : n.getDown();

    if (isInnerEmptyCornerBase(neighborX, layout, sx, sy)) return false;
    if (isInnerEmptyCornerBase(neighborY, layout, sx, sy)) return false;

    return true;
  }

  /**
   * Checks whether any orthogonal neighbor of {@code p} is out of bounds, i.e., the cell lies at
   * the border of the layout.
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @return {@code true} if {@code p} touches the layout boundary; otherwise {@code false}
   */
  private static boolean isAtBorder(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return n.getUpE() == null
        || n.getDownE() == null
        || n.getLeftE() == null
        || n.getRightE() == null;
  }

  /**
   * Determines if the tile at {@code c} should render as empty in wall context. Returns {@code
   * true} immediately for {@code SKIP}. For {@code WALL} tiles, returns {@code true} for
   * inner-group empty cases (either axis), stem-cross centers, or border walls without adjacent
   * floor. Other elements return {@code false}.
   *
   * @param c the coordinate to test
   * @param layout the level grid
   * @return {@code true} if the tile renders empty like a wall; otherwise {@code false}
   */
  private static boolean rendersSkipLikeWallAt(Coordinate c, LevelElement[][] layout) {
    LevelElement e = get(layout, c.x(), c.y());
    if (e == LevelElement.SKIP) return true;
    if (e != LevelElement.WALL) return false;
    if (rendersSkipAt(c, layout, Axis.VERTICAL)
        || rendersSkipAt(c, layout, Axis.HORIZONTAL)
        || isStemCrossCenter(c, layout)) {
      return true;
    }
    return isAtBorder(c, layout) && !hasAdjacentFloorOrDoor(c, layout);
  }

  /**
   * Returns whether the neighbor at {@code c} resolves to {@code /wall/empty.png} using the same
   * design and layout as {@code lp}.
   *
   * @param lp the context level part (design, layout)
   * @param c the neighbor coordinate
   * @return {@code true} if the resolved path ends with {@code /wall/empty.png}; otherwise {@code
   *     false}
   */
  private static boolean rendersWallEmptyAt(LevelPart lp, Coordinate c) {
    LevelElement e = get(lp.layout(), c.x(), c.y());
    if (e == null) return false;
    LevelPart neighbor = new LevelPart(e, lp.design(), lp.layout(), c);
    IPath path = resolvePrimaryPath(neighbor);
    String s = path != null ? path.pathString() : null;
    return s != null && s.endsWith("/wall/empty.png");
  }

  /**
   * Detects a pattern where the cells above/below form a non-floor barrier pair while the upper (or
   * lower) diagonals are floor-like, and both left/right neighbors are WALL.
   *
   * @param p the pivot coordinate
   * @param layout the level grid
   * @param vertical choose {@code UP} for the upper-case check or {@code DOWN} for the lower-case
   * @return {@code true} if the empty-left-right case is satisfied
   */
  private static boolean emptyLeftRightCase(Coordinate p, LevelElement[][] layout, Dir vertical) {
    Neighbors n = Neighbors.of(p, layout);
    boolean top = vertical == Dir.UP;
    LevelElement eA = top ? n.getUpE() : n.getDownE();
    LevelElement eB = top ? n.getDownE() : n.getUpE();
    LevelElement eAL = top ? n.getUpLeftE() : n.getDownLeftE();
    LevelElement eAR = top ? n.getUpRightE() : n.getDownRightE();
    LevelElement oppAL = top ? n.getDownLeftE() : n.getUpLeftE();
    LevelElement oppAR = top ? n.getDownRightE() : n.getUpRightE();
    return isNotFloor(eA)
        && isFloorOrDoor(eAL)
        && isFloorOrDoor(eAR)
        && isNotFloor(eB)
        && n.getLeftE() == LevelElement.WALL
        && n.getRightE() == LevelElement.WALL
        && isNotFloor(oppAL)
        && isNotFloor(oppAR);
  }

  /**
   * Detects a T-shaped pattern where the top and bottom are WALLs, the two side-diagonals on the
   * given {@code side} are floor-like, and the opposite side cells are not floor.
   *
   * @param p the pivot coordinate
   * @param layout the level grid
   * @param side the side to evaluate (LEFT or RIGHT)
   * @return {@code true} if the T top/bottom empty-side case holds; otherwise {@code false}
   */
  private static boolean tEmptyTopBottomCase(Coordinate p, LevelElement[][] layout, Dir side) {
    Neighbors n = Neighbors.of(p, layout);
    boolean right = side == Dir.RIGHT;
    LevelElement upSide = right ? n.getUpRightE() : n.getUpLeftE();
    LevelElement downSide = right ? n.getDownRightE() : n.getDownLeftE();
    LevelElement leftE = n.getLeftE();
    LevelElement rightE = n.getRightE();
    LevelElement upE = n.getUpE();
    LevelElement downE = n.getDownE();
    return isFloorOrDoor(upSide)
        && isFloorOrDoor(downSide)
        && isNotFloor(right ? rightE : leftE)
        && isNotFloor(right ? leftE : rightE)
        && isNotFloor(right ? n.getUpLeftE() : n.getUpRightE())
        && isNotFloor(right ? n.getDownLeftE() : n.getDownRightE())
        && upE == LevelElement.WALL
        && downE == LevelElement.WALL;
  }

  /**
   * Checks an outward-facing border condition: the outward neighbor is outside the layout, the
   * opposite neighbor is not floor, the inner neighbor is not empty-like, and the two diagonals on
   * the opposite side are floors.
   *
   * @param p the coordinate to test
   * @param layout the level grid
   * @param outward the outward direction to evaluate
   * @return {@code true} if the border-and-opposite-not-floor condition holds
   */
  private static boolean outsideAndOppositeNotFloor(
      Coordinate p, LevelElement[][] layout, Dir outward) {
    Neighbors n = Neighbors.of(p, layout);
    Coordinate inner =
        switch (outward) {
          case UP -> n.getDown();
          case DOWN -> n.getUp();
          case LEFT -> n.getRight();
          case RIGHT -> n.getLeft();
        };
    boolean outside =
        switch (outward) {
          case UP -> n.getUpE() == null;
          case DOWN -> n.getDownE() == null;
          case LEFT -> n.getLeftE() == null;
          case RIGHT -> n.getRightE() == null;
        };
    boolean oppositeNotFloor =
        switch (outward) {
          case UP -> isNotFloor(n.getDownE());
          case DOWN -> isNotFloor(n.getUpE());
          case LEFT -> isNotFloor(n.getRightE());
          case RIGHT -> isNotFloor(n.getLeftE());
        };
    boolean innerNotEmpty = !rendersSkipLikeWallAt(inner, layout);
    boolean floorsOk =
        switch (outward) {
          case UP -> isFloorOrDoor(n.getDownLeftE()) && isFloorOrDoor(n.getDownRightE());
          case DOWN -> isFloorOrDoor(n.getUpLeftE()) && isFloorOrDoor(n.getUpRightE());
          case LEFT -> isFloorOrDoor(n.getUpRightE()) && isFloorOrDoor(n.getDownRightE());
          case RIGHT -> isFloorOrDoor(n.getUpLeftE()) && isFloorOrDoor(n.getDownLeftE());
        };
    return outside && oppositeNotFloor && innerNotEmpty && floorsOk;
  }

  /**
   * Signals that an inner T facing {@code dir} should be suppressed when the forward cell is a WALL
   * and one of the perpendicular arms forms a WALL followed by a FLOOR two tiles away.
   *
   * @param p the candidate T center
   * @param layout the level grid
   * @param dir the facing direction of the T
   * @return {@code true} if the inner T should be disallowed; otherwise {@code false}
   */
  private static boolean disallowTInner(Coordinate p, LevelElement[][] layout, Dir dir) {
    int dx = dir.dx, dy = dir.dy;
    if (get(layout, p.x() + dx, p.y() + dy) != LevelElement.WALL) return false;

    Dir perp1 = (dir == Dir.LEFT || dir == Dir.RIGHT) ? Dir.UP : Dir.LEFT;
    Dir perp2 = (dir == Dir.LEFT || dir == Dir.RIGHT) ? Dir.DOWN : Dir.RIGHT;

    boolean arm1 =
        get(layout, p.x() + dx + perp1.dx, p.y() + dy + perp1.dy) == LevelElement.WALL
            && isFloorLike(get(layout, p.x() + 2 * dx + perp1.dx, p.y() + 2 * dy + perp1.dy));

    boolean arm2 =
        get(layout, p.x() + dx + perp2.dx, p.y() + dy + perp2.dy) == LevelElement.WALL
            && isFloorLike(get(layout, p.x() + 2 * dx + perp2.dx, p.y() + 2 * dy + perp2.dy));

    return arm1 || arm2;
  }

  /**
   * Returns whether the element is treated as floor-like for connectivity rules.
   *
   * @param e the element to test
   * @return {@code true} if {@code e} is FLOOR, PIT, HOLE, DOOR, or EXIT; otherwise {@code false}
   */
  private static boolean isFloorOrDoor(LevelElement e) {
    return isFloorLike(e) || e == LevelElement.DOOR || e == LevelElement.EXIT;
  }

  /**
   * Convenience predicate indicating the element is not floor-like.
   *
   * @param e the element to test
   * @return {@code true} if {@code e} is not floor-like; otherwise {@code false}
   */
  private static boolean isNotFloor(LevelElement e) {
    return !isFloorLike(e);
  }

  /**
   * Indicates interior space that is not a door/exit, for wall-closure checks.
   *
   * @param e the element to test
   * @return {@code true} if {@code e} is inside but neither DOOR nor EXIT; otherwise {@code false}
   */
  private static boolean isInsideNonDoor(LevelElement e) {
    return e != LevelElement.DOOR && e != LevelElement.EXIT && isInside(e);
  }

  /**
   * Detects an inner top wall: barriers (wall/door) to left and right, inside-non-door above, and
   * inside below.
   *
   * @param p the wall coordinate to evaluate
   * @param layout the level grid
   * @return {@code true} if the inner-top-wall pattern is present; otherwise {@code false}
   */
  private static boolean isInnerTopWall(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    return (isWallDir(p, layout, Dir.LEFT) || isDoorDir(p, layout, Dir.LEFT))
        && (isWallDir(p, layout, Dir.RIGHT) || isDoorDir(p, layout, Dir.RIGHT))
        && isInsideNonDoor(n.getUpE())
        && isInside(n.getDownE());
  }

  /**
   * Treats the diagonal as inside if it is accessible or a hole.
   *
   * @param p the reference coordinate
   * @param layout the level grid
   * @param corner which diagonal (UL, UR, BL, BR)
   * @return {@code true} if the diagonal is accessible or a hole; otherwise {@code false}
   */
  private static boolean isDiagonalInside(Coordinate p, LevelElement[][] layout, Corner corner) {
    Neighbors n = Neighbors.of(p, layout);
    LevelElement e =
        switch (corner) {
          case UR -> n.getUpRightE();
          case BR -> n.getDownRightE();
          case BL -> n.getDownLeftE();
          case UL -> n.getUpLeftE();
        };
    return isInside(e);
  }

  /**
   * Retrieves the element adjacent to {@code p} in direction {@code d}, or {@code null} if out of
   * bounds.
   *
   * @param layout the level grid
   * @param p the origin coordinate
   * @param d the direction offset
   * @return the neighboring element, or {@code null} if outside the layout
   */
  private static LevelElement at(LevelElement[][] layout, Coordinate p, Dir d) {
    int x = p.x() + d.dx, y = p.y() + d.dy;
    return isInsideLayout(x, y, layout) ? layout[y][x] : null;
  }

  /**
   * Tests the neighbor at {@code p + d} against the given predicate, guarding bounds.
   *
   * @param p the origin coordinate
   * @param layout the level grid
   * @param d the neighbor direction
   * @param pred element predicate to evaluate
   * @return {@code true} if the neighbor exists and satisfies {@code pred}; otherwise {@code false}
   */
  private static boolean neighborMatches(
      Coordinate p,
      LevelElement[][] layout,
      Dir d,
      java.util.function.Predicate<LevelElement> pred) {
    LevelElement e = at(layout, p, d);
    return e != null && pred.test(e);
  }

  /**
   * Returns whether the neighbor at {@code p + d} is a WALL.
   *
   * @param p the origin coordinate
   * @param layout the level grid
   * @param d the neighbor direction
   * @return {@code true} if the neighbor is WALL; otherwise {@code false}
   */
  private static boolean isWallDir(Coordinate p, LevelElement[][] layout, Dir d) {
    return neighborMatches(p, layout, d, e -> e == LevelElement.WALL);
  }

  /**
   * Returns whether the neighbor at {@code p + d} is a DOOR.
   *
   * @param p the origin coordinate
   * @param layout the level grid
   * @param d the neighbor direction
   * @return {@code true} if the neighbor is DOOR; otherwise {@code false}
   */
  private static boolean isDoorDir(Coordinate p, LevelElement[][] layout, Dir d) {
    return neighborMatches(p, layout, d, e -> e == LevelElement.DOOR);
  }

  /**
   * Returns whether the neighbor at {@code p + d} counts as inside (walkable or hole/pit).
   *
   * @param p the origin coordinate
   * @param layout the level grid
   * @param d the neighbor direction
   * @return {@code true} if the neighbor is inside; otherwise {@code false}
   */
  private static boolean isInsideDir(Coordinate p, LevelElement[][] layout, Dir d) {
    return neighborMatches(p, layout, d, e -> e.value() || isFloorLike(e));
  }

  /**
   * Verifies that all four orthogonal neighbors are WALL and none of them render as empty-like
   * walls under current rules.
   *
   * @param p the center coordinate
   * @param layout the level grid
   * @return {@code true} if all orthogonal neighbors are solid, non-empty walls; otherwise {@code
   *     false}
   */
  private static boolean hasNoEmptyWallsAround(Coordinate p, LevelElement[][] layout) {
    Neighbors n = Neighbors.of(p, layout);
    if (n.getUpE() != LevelElement.WALL) return false;
    if (n.getDownE() != LevelElement.WALL) return false;
    if (n.getLeftE() != LevelElement.WALL) return false;
    if (n.getRightE() != LevelElement.WALL) return false;

    if (rendersSkipLikeWallAt(n.getUp(), layout)) return false;
    if (rendersSkipLikeWallAt(n.getDown(), layout)) return false;
    if (rendersSkipLikeWallAt(n.getLeft(), layout)) return false;
    if (rendersSkipLikeWallAt(n.getRight(), layout)) return false;

    return true;
  }

  /**
   * Returns whether the element is considered floor-like for connectivity/neighborhood rules.
   *
   * @param e the element to test (may be {@code null})
   * @return {@code true} if {@code e} is {@code FLOOR}, {@code HOLE}, or {@code PIT}; otherwise
   *     {@code false}
   */
  private static boolean isFloorLike(LevelElement e) {
    return e == LevelElement.FLOOR || e == LevelElement.HOLE || e == LevelElement.PIT;
  }

  /**
   * Indicates whether the diagonal neighbor of {@code p} in the specified {@code corner} is
   * floor-like.
   *
   * @param p the reference coordinate
   * @param layout the level grid
   * @param corner which diagonal (UL, UR, BL, BR) to inspect
   * @return {@code true} if the diagonal element is {@code FLOOR}, {@code HOLE}, or {@code PIT};
   *     otherwise {@code false}
   */
  private static boolean isDiagonalFloorLike(Coordinate p, LevelElement[][] layout, Corner corner) {
    Neighbors n = Neighbors.of(p, layout);
    LevelElement e =
        switch (corner) {
          case UR -> n.getUpRightE();
          case BR -> n.getDownRightE();
          case BL -> n.getDownLeftE();
          case UL -> n.getUpLeftE();
        };
    return isFloorLike(e);
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
