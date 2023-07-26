package core.level.utils;

import core.level.Tile;
import core.utils.position.Point;

public class TileTextureFactory {
    /**
     * Helper record class for {@link TileTextureFactory}.
     *
     * @param element Element to check for
     * @param design Design of the element
     * @param layout The level
     * @param position Position of the element.
     */
    public record LevelPart(
            LevelElement element, DesignLabel design, LevelElement[][] layout, Point position) {}

    /**
     * Checks which texture must be used for the passed field based on the surrounding fields.
     *
     * @param levelPart a part of a level
     * @return Path to texture
     */
    public static String findTexturePath(LevelPart levelPart) {
        String prefixPath = "dungeon/" + levelPart.design().name().toLowerCase() + "/";

        String path = findTexturePathFloor(levelPart);
        if (path != null) {
            return prefixPath + path + ".png";
        }

        path = findTexturePathDoor(levelPart);
        if (path != null) {
            return prefixPath + path + ".png";
        }

        path = findTexturePathInnerCorner(levelPart);
        if (path != null) {
            return prefixPath + path + ".png";
        }

        path = findTexturePathOuterCorner(levelPart);
        if (path != null) {
            return prefixPath + path + ".png";
        }

        path = findTexturePathWall(levelPart);
        if (path != null) {
            return prefixPath + path + ".png";
        }

        // Error state
        return prefixPath + "floor/empty.png";
    }

    /**
     * Checks which texture must be used for the passed tile based on the surrounding tiles.
     *
     * @param element Tile to check for
     * @param layout The level
     * @return Path to texture
     */
    public static String findTexturePath(Tile element, Tile[][] layout) {
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
    public static String findTexturePath(Tile element, Tile[][] layout, LevelElement elementType) {
        LevelElement[][] elementLayout = new LevelElement[layout.length][layout[0].length];
        for (int x = 0; x < layout[0].length; x++) {
            for (int y = 0; y < layout.length; y++) {
                elementLayout[y][x] = layout[y][x].levelElement();
            }
        }
        elementLayout[element.position().point().y_i()][element.position().point().x_i()] =
                elementType;
        return findTexturePath(
                new LevelPart(
                        elementType,
                        element.designLabel(),
                        elementLayout,
                        element.position().point()));
    }

    private static String findTexturePathFloor(LevelPart levelPart) {
        if (levelPart.element() == LevelElement.SKIP) {
            return "floor/empty";
        } else if (levelPart.element() == LevelElement.FLOOR) {
            return "floor/floor_1";
        } else if (levelPart.element() == LevelElement.EXIT) {
            return "floor/floor_ladder";
        } else if (levelPart.element() == LevelElement.HOLE) {
            if (aboveIsHole(levelPart.position, levelPart.layout)) {
                return "floor/floor_hole1";
            } else {
                return "floor/floor_hole";
            }
        }
        return null;
    }

    private static String findTexturePathDoor(LevelPart levelPart) {
        if (levelPart.element() == LevelElement.DOOR) {
            if (belowIsAccessible(levelPart.position, levelPart.layout)) {
                return "door/top";
            } else if (leftIsAccessible(levelPart.position, levelPart.layout)) {
                return "door/right";
            } else if (rightIsAccessible(levelPart.position, levelPart.layout)) {
                return "door/left";
            } else if (aboveIsAccessible(levelPart.position, levelPart.layout)) {
                return "door/bottom";
            }
        }
        return null;
    }

    private static String findTexturePathWall(LevelPart levelPart) {
        if (isRightWall(levelPart.position(), levelPart.layout())) {
            return "wall/wall_right";
        } else if (isLeftWall(levelPart.position(), levelPart.layout())) {
            return "wall/wall_left";
        } else if (isTopWall(levelPart.position(), levelPart.layout())) {
            return "wall/wall_top";
        } else if (isBottomWall(levelPart.position(), levelPart.layout())) {
            return "wall/wall_bottom";
        }
        return null;
    }

    private static String findTexturePathInnerCorner(LevelPart levelPart) {
        if (isCrossUpperLeftBottomRight(levelPart.position(), levelPart.layout())) {
            return "wall/wall_cross_upper_left_bottom_right";
        } else if (isCrossUpperRightBottomLeft(levelPart.position(), levelPart.layout())) {
            return "wall/wall_cross_upper_right_bottom_left";
        } else if (isBottomLeftInnerCorner(levelPart.position(), levelPart.layout())) {
            return "wall/wall_inner_corner_bottom_left";
        } else if (isBottomRightInnerCorner(levelPart.position(), levelPart.layout())) {
            return "wall/wall_inner_corner_bottom_right";
        } else if (isUpperRightInnerCorner(levelPart.position(), levelPart.layout())) {
            return "wall/wall_inner_corner_upper_right";
        } else if (isUpperLeftInnerCorner(levelPart.position(), levelPart.layout())) {
            return "wall/wall_inner_corner_upper_left";
        }
        return null;
    }

    private static String findTexturePathOuterCorner(LevelPart levelPart) {
        if (isBottomLeftOuterCorner(levelPart.position(), levelPart.layout())) {
            return "wall/wall_outer_corner_bottom_left";
        } else if (isBottomRightOuterCorner(levelPart.position(), levelPart.layout())) {
            return "wall/wall_outer_corner_bottom_right";
        } else if (isUpperRightOuterCorner(levelPart.position(), levelPart.layout())) {
            return "wall/wall_outer_corner_upper_right";
        } else if (isUpperLeftOuterCorner(levelPart.position(), levelPart.layout())) {
            return "wall/wall_outer_corner_upper_left";
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
    private static boolean isInSpaceWall(Point p, LevelElement[][] layout) {
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
    private static boolean isCrossUpperLeftBottomRight(Point p, LevelElement[][] layout) {
        return (isInSpaceWall(p, layout)
                && upperLeftIsAccessible(p, layout)
                && bottomRightIsAccessible(p, layout));
    }

    /**
     * Checks if tile with coordinate p should be a crossUpperRightBottomLeft wall. Tile has to be
     * surrounded by walls and have accessible tiles in the upper right and bottom left.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isCrossUpperRightBottomLeft(Point p, LevelElement[][] layout) {
        return (isInSpaceWall(p, layout)
                && upperRightIsAccessible(p, layout)
                && bottomLeftIsAccessible(p, layout));
    }

    /**
     * Checks if tile with coordinate p should be a bottomLeftOuterCorner wall. Tile has to have
     * walls above and to the right and an accessible tile to the upper right.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isBottomLeftOuterCorner(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout)
                && rightIsWall(p, layout)
                && upperRightIsAccessible(p, layout));
    }

    /**
     * Checks if tile with coordinate p should be a bottomRightOuterCorner wall. Tile has to have
     * walls above and to the left and an accessible tile to the upper left.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isBottomRightOuterCorner(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout)
                && leftIsWall(p, layout)
                && upperLeftIsAccessible(p, layout));
    }

    /**
     * Checks if tile with coordinate p should be a upperRightOuterCorner wall. Tile has to have
     * walls below and to the left and an accessible tile to the bottom left.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isUpperRightOuterCorner(Point p, LevelElement[][] layout) {
        return (belowIsWall(p, layout)
                && leftIsWall(p, layout)
                && bottomLeftIsAccessible(p, layout));
    }

    /**
     * Checks if tile with coordinate p should be a upperLeftOuterCorner wall. Tile has to have
     * walls below and to the right and an accessible tile to the bottom right.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isUpperLeftOuterCorner(Point p, LevelElement[][] layout) {
        return (belowIsWall(p, layout)
                && rightIsWall(p, layout)
                && bottomRightIsAccessible(p, layout));
    }

    /**
     * Checks if tile with coordinate p should be a bottomLeftInnerCorner wall. Tile has to have
     * walls above and to the right and inside tiles (accessible or hole) either to the left and
     * bottom right, below and to the upper left or below and to the left.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isBottomLeftInnerCorner(Point p, LevelElement[][] layout) {
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
    private static boolean isBottomRightInnerCorner(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout)
                && leftIsWall(p, layout)
                && (rightIsInside(p, layout) && bottomLeftIsInside(p, layout)
                        || belowIsInside(p, layout) && upperRightIsInside(p, layout)
                        || belowIsInside(p, layout) && rightIsInside(p, layout)));
    }

    /**
     * Checks if tile with coordinate p should be a upperRightInnerCorner wall. Tile has to have
     * walls below and to the left and inside tiles (accessible or hole) either to the right and
     * upper left, above and to the bottom right or above and to the right.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isUpperRightInnerCorner(Point p, LevelElement[][] layout) {
        return (belowIsWall(p, layout)
                && leftIsWall(p, layout)
                && (rightIsInside(p, layout) && upperLeftIsInside(p, layout)
                        || aboveIsInside(p, layout) && bottomRightIsInside(p, layout)
                        || aboveIsInside(p, layout) && rightIsInside(p, layout)));
    }

    /**
     * Checks if tile with coordinate p should be a upperLeftInnerCorner wall. Tile has to have
     * walls below and to the right and inside tiles (accessible or hole) either to the left and
     * upper right, above and to the bottom left or above and to the left.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isUpperLeftInnerCorner(Point p, LevelElement[][] layout) {
        return (belowIsWall(p, layout)
                && rightIsWall(p, layout)
                && (leftIsInside(p, layout) && upperRightIsInside(p, layout)
                        || aboveIsInside(p, layout) && bottomLeftIsInside(p, layout)
                        || aboveIsInside(p, layout) && leftIsInside(p, layout)));
    }

    /**
     * Checks if tile with coordinate p should be a right wall. Tile has to have walls above and
     * below and an inside tile (accessible or hole) to the left.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isRightWall(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) || aboveIsDoor(p, layout))
                && (belowIsWall(p, layout) || belowIsDoor(p, layout))
                && leftIsInside(p, layout);
    }

    /**
     * Checks if tile with coordinate p should be a left wall. Tile has to have walls above and
     * below and an inside tile (accessible or hole) to the right.
     *
     * @param p coordinate to check
     * @param layout The level
     * @return true if all conditions are met
     */
    private static boolean isLeftWall(Point p, LevelElement[][] layout) {
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
    private static boolean isTopWall(Point p, LevelElement[][] layout) {
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
    private static boolean isBottomWall(Point p, LevelElement[][] layout) {
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
    private static boolean aboveIsWall(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() + 1][p.x_i()] == LevelElement.WALL;

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
    private static boolean belowIsWall(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() - 1][p.x_i()] == LevelElement.WALL;

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
    private static boolean leftIsWall(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i()][p.x_i() - 1] == LevelElement.WALL;

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
    private static boolean rightIsWall(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i()][p.x_i() + 1] == LevelElement.WALL;

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
    private static boolean aboveIsDoor(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() + 1][p.x_i()] == LevelElement.DOOR;

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
    private static boolean belowIsDoor(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() - 1][p.x_i()] == LevelElement.DOOR;

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
    private static boolean leftIsDoor(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i()][p.x_i() - 1] == LevelElement.DOOR;

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
    private static boolean rightIsDoor(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i()][p.x_i() + 1] == LevelElement.DOOR;

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
    private static boolean aboveIsAccessible(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() + 1][p.x_i()].value();

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
    private static boolean leftIsAccessible(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i()][p.x_i() - 1].value();

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
    private static boolean rightIsAccessible(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i()][p.x_i() + 1].value();

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
    private static boolean belowIsAccessible(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() - 1][p.x_i()].value();

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
    private static boolean upperRightIsAccessible(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() + 1][p.x_i() + 1].value();

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
    private static boolean bottomRightIsAccessible(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() - 1][p.x_i() + 1].value();

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
    private static boolean bottomLeftIsAccessible(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() - 1][p.x_i() - 1].value();

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
    private static boolean upperLeftIsAccessible(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() + 1][p.x_i() - 1].value();

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
    private static boolean aboveIsHole(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() + 1][p.x_i()] == LevelElement.HOLE;

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
    private static boolean leftIsHole(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i()][p.x_i() - 1] == LevelElement.HOLE;

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
    private static boolean rightIsHole(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i()][p.x_i() + 1] == LevelElement.HOLE;

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
    private static boolean belowIsHole(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() - 1][p.x_i()] == LevelElement.HOLE;

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
    private static boolean upperRightIsHole(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() + 1][p.x_i() + 1] == LevelElement.HOLE;

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
    private static boolean bottomRightIsHole(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() - 1][p.x_i() + 1] == LevelElement.HOLE;

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
    private static boolean bottomLeftIsHole(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() - 1][p.x_i() - 1] == LevelElement.HOLE;

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
    private static boolean upperLeftIsHole(Point p, LevelElement[][] layout) {
        try {
            return layout[p.y_i() + 1][p.x_i() - 1] == LevelElement.HOLE;

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
    private static boolean aboveIsInside(Point p, LevelElement[][] layout) {
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
    private static boolean leftIsInside(Point p, LevelElement[][] layout) {
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
    private static boolean rightIsInside(Point p, LevelElement[][] layout) {
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
    private static boolean belowIsInside(Point p, LevelElement[][] layout) {
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
    private static boolean upperRightIsInside(Point p, LevelElement[][] layout) {
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
    private static boolean bottomRightIsInside(Point p, LevelElement[][] layout) {
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
    private static boolean bottomLeftIsInside(Point p, LevelElement[][] layout) {
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
    private static boolean upperLeftIsInside(Point p, LevelElement[][] layout) {
        try {
            return (upperLeftIsAccessible(p, layout) || upperLeftIsHole(p, layout));

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
}
