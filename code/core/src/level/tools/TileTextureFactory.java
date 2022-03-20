package level.tools;

public class TileTextureFactory {
    /**
     * Checks which texture must be used for the passed field based on the surrounding fields.
     *
     * @param element Element to check for
     * @param design Design of the element
     * @param layout The level
     * @param position Position of the element.
     * @return Path to texture
     */
    public static String findTexture(
            LevelElement element,
            DesignLabel design,
            LevelElement[][] layout,
            Coordinate position) {
        String path = design.name().toLowerCase() + "/";
        if (element == LevelElement.SKIP) path += "floor/empty";
        else if (element == LevelElement.FLOOR) path += "floor/floor_1";
        else if (element == LevelElement.EXIT) path += "floor/floor_ladder";
        else if (element == LevelElement.DOOR) path += "floor/floor_1";

        // is field in a non-playable area?
        else if (isInSpace(position, layout)) path += "floor/empty";

        // walls
        else if (isRightWall(position, layout)) path += "wall/right";
        else if (isLeftWall(position, layout)) path += "wall/left";
        else if (isSideWall(position, layout)) path += "wall/side";
        else if (isTopWall(position, layout)) path += "wall/top";
        else if (isBottomWall(position, layout)) path += "wall/bottom";
        else if (isBottomAndTopWall(position, layout)) path += "wall/top_bottom";

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
        else if (isBottomLeftCorner(position, layout)) path += "wall/corner_bottom_left";
        else if (isBottomRightCorner(position, layout)) path += "wall/corner_bottom_right";
        else if (isUpperRightCorner(position, layout)) path += "wall/corner_upper_right";
        else if (isUpperLeftCorner(position, layout)) path += "wall/corner_upper_left";

        // fehler zustand
        else path += "floor/empty";

        return "textures/dungeon/" + path + ".png";
    }

    private static boolean isInSpace(Coordinate p, LevelElement[][] layout) {
        return (belowIsSkip(p, layout)
                        && aboveIsSkip(p, layout)
                        && leftIsSkip(p, layout)
                        && rightIsFloor(p, layout))
                || belowIsWall(p, layout)
                        && aboveIsWall(p, layout)
                        && leftIsWall(p, layout)
                        && rightIsWall(p, layout);
    }

    private static boolean isFourWayCross(Coordinate p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout)
                && belowIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout));
    }

    private static boolean isThreeWayCrossUp(Coordinate p, LevelElement[][] layout) {
        return (!belowIsWall(p, layout)
                && aboveIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout));
    }

    private static boolean isThreeWayCrossDown(Coordinate p, LevelElement[][] layout) {
        return (!aboveIsWall(p, layout)
                && belowIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout));
    }

    private static boolean isThreeWayCrossLeft(Coordinate p, LevelElement[][] layout) {
        return (belowIsWall(p, layout)
                && aboveIsWall(p, layout)
                && leftIsWall(p, layout)
                && !rightIsWall(p, layout));
    }

    private static boolean isThreeWayCrossRight(Coordinate p, LevelElement[][] layout) {
        return (belowIsWall(p, layout)
                && aboveIsWall(p, layout)
                && rightIsWall(p, layout)
                && !leftIsWall(p, layout));
    }

    private static boolean isBottomLeftCorner(Coordinate p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) && rightIsWall(p, layout));
    }

    private static boolean isBottomRightCorner(Coordinate p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) && leftIsWall(p, layout));
    }

    private static boolean isUpperRightCorner(Coordinate p, LevelElement[][] layout) {
        return (belowIsWall(p, layout) && leftIsWall(p, layout));
    }

    private static boolean isUpperLeftCorner(Coordinate p, LevelElement[][] layout) {
        return (belowIsWall(p, layout) && rightIsWall(p, layout));
    }

    private static boolean isRightWall(Coordinate p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) || belowIsWall(p, layout))
                && leftIsFloor(p, layout)
                && !rightIsFloor(p, layout);
    }

    private static boolean isLeftWall(Coordinate p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) || belowIsWall(p, layout))
                && !leftIsFloor(p, layout)
                && rightIsFloor(p, layout);
    }

    private static boolean isSideWall(Coordinate p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout) || belowIsWall(p, layout))
                && leftIsFloor(p, layout)
                && rightIsFloor(p, layout);
    }

    private static boolean isTopWall(Coordinate p, LevelElement[][] layout) {
        return belowIsFloor(p, layout)
                && !aboveIsFloor(p, layout)
                && (leftIsWall(p, layout) || rightIsWall(p, layout));
    }

    private static boolean isBottomWall(Coordinate p, LevelElement[][] layout) {
        return !belowIsFloor(p, layout)
                && aboveIsFloor(p, layout)
                && (leftIsWall(p, layout) || rightIsWall(p, layout));
    }

    private static boolean isBottomAndTopWall(Coordinate p, LevelElement[][] layout) {
        return belowIsFloor(p, layout)
                && aboveIsFloor(p, layout)
                && (leftIsWall(p, layout) || rightIsWall(p, layout));
    }

    private static boolean aboveIsWall(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y + 1][(int) p.x] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean belowIsWall(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y - 1][(int) p.x] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean leftIsWall(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x - 1] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean rightIsWall(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x + 1] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean aboveIsFloor(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y + 1][(int) p.x] == LevelElement.FLOOR
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.EXIT
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.DOOR;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean belowIsFloor(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y - 1][(int) p.x] == LevelElement.FLOOR
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.EXIT
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.DOOR;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean leftIsFloor(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x - 1] == LevelElement.FLOOR
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.EXIT
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.DOOR;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean rightIsFloor(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x + 1] == LevelElement.FLOOR
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.EXIT
                    || layout[(int) p.y + 1][(int) p.x] == LevelElement.DOOR;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean rightIsSkip(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x + 1] == LevelElement.SKIP;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean leftIsSkip(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y][(int) p.x - 1] == LevelElement.SKIP;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean aboveIsSkip(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y + 1][(int) p.x] == LevelElement.SKIP;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean belowIsSkip(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[(int) p.y - 1][(int) p.x] == LevelElement.SKIP;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
}
