package level.tools;

import tools.Point;

import java.util.Locale;

public class TileTextureFactory {

    public static String findTexture(
            LevelElement e, DesignLabel l, LevelElement[][] layout, Point p) {
        String path = l.name().toLowerCase(Locale.ROOT) + "/";

        if (e == LevelElement.FLOOR) path += "floor/floor_1";
        else if (e == LevelElement.EXIT) path += "floor/floor_ladder";

        // walls
        else if (isRightWall(p, layout)) path += "wall/right";
        else if (isLeftWall(p, layout)) path += "wall/left";
        else if (isSideWall(p, layout)) path += "wall/side";
        else if (isTopWall(p, layout)) path += "wall/top";
        else if (isBottomWall(p, layout)) path += "wall/bottom";
        else if (isBottomAndTopWall(p, layout)) path += "wall/top_bottom";

        /*     //crossroads
                else if (isFourWayCross(p, layout))
                    path += "wall/fourway_cross";
                else if (isThreeWayCrossDown(p, layout))
                    path += "wall/top";
                else if (isThreeWayCrossUp(p, layout))
                    path += "wall/bottom";
                else if (isThreeWayCrossLeft(p, layout))
                    path += "wall/threeway_cross_left";
                else if (isThreeWayCrossRight(p, layout))
                    path += "wall/threeway_cross_right";
        */
        // corners
        else if (isBottomLeftCorner(p, layout)) path += "wall/corner_bottom_left";
        else if (isBottomRightCorner(p, layout)) path += "wall/corner_bottom_right";
        else if (isUpperRightCorner(p, layout)) path += "wall/corner_upper_right";
        else if (isUpperLeftCorner(p, layout)) path += "wall/corner_upper_left";

        // fehler zustand
        else path += "floor/floor_1";

        return "textures/dungeon/" + path + ".png";
    }

    private static boolean isFourWayCross(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout)
                && belowIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout));
    }

    private static boolean isThreeWayCrossUp(Point p, LevelElement[][] layout) {
        return (!belowIsWall(p, layout)
                && aboveIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout));
    }

    private static boolean isThreeWayCrossDown(Point p, LevelElement[][] layout) {
        return (!aboveIsWall(p, layout)
                && belowIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout));
    }

    private static boolean isThreeWayCrossLeft(Point p, LevelElement[][] layout) {
        return (belowIsWall(p, layout)
                && aboveIsWall(p, layout)
                && leftIsWall(p, layout)
                && !rightIsWall(p, layout));
    }

    private static boolean isThreeWayCrossRight(Point p, LevelElement[][] layout) {
        return (belowIsWall(p, layout)
                && aboveIsWall(p, layout)
                && rightIsWall(p, layout)
                && !leftIsWall(p, layout));
    }

    private static boolean isBottomLeftCorner(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) && rightIsWall(p, layout));
    }

    private static boolean isBottomRightCorner(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) && leftIsWall(p, layout));
    }

    private static boolean isUpperRightCorner(Point p, LevelElement[][] layout) {
        return (belowIsWall(p, layout) && leftIsWall(p, layout));
    }

    private static boolean isUpperLeftCorner(Point p, LevelElement[][] layout) {
        return (belowIsWall(p, layout) && rightIsWall(p, layout));
    }

    private static boolean isRightWall(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) || belowIsWall(p, layout))
                && leftIsFloor(p, layout)
                && !rightIsFloor(p, layout);
    }

    private static boolean isLeftWall(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) || belowIsWall(p, layout))
                && !leftIsFloor(p, layout)
                && rightIsFloor(p, layout);
    }

    private static boolean isSideWall(Point p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) || belowIsWall(p, layout))
                && leftIsFloor(p, layout)
                && rightIsFloor(p, layout);
    }

    private static boolean isTopWall(Point p, LevelElement[][] layout) {
        return belowIsFloor(p, layout)
                && !aboveIsFloor(p, layout)
                && (leftIsWall(p, layout) || rightIsWall(p, layout));
    }

    private static boolean isBottomWall(Point p, LevelElement[][] layout) {
        return !belowIsFloor(p, layout)
                && aboveIsFloor(p, layout)
                && (leftIsWall(p, layout) || rightIsWall(p, layout));
    }

    private static boolean isBottomAndTopWall(Point p, LevelElement[][] layout) {
        return belowIsFloor(p, layout)
                && aboveIsFloor(p, layout)
                && (leftIsWall(p, layout) || rightIsWall(p, layout));
    }

    private static boolean aboveIsWall(Point p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y + 1][(int) p.x] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean belowIsWall(Point p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y - 1][(int) p.x] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean leftIsWall(Point p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x - 1] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean rightIsWall(Point p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x + 1] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean aboveIsFloor(Point p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y + 1][(int) p.x] == LevelElement.FLOOR
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.EXIT;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean belowIsFloor(Point p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y - 1][(int) p.x] == LevelElement.FLOOR
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.EXIT;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean leftIsFloor(Point p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x - 1] == LevelElement.FLOOR
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.EXIT;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean rightIsFloor(Point p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x + 1] == LevelElement.FLOOR
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.EXIT;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
}
