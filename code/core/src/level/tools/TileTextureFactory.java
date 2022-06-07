package level.tools;

import level.elements.Tile;

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
    public static String findTexturePath(
            LevelElement element,
            DesignLabel design,
            LevelElement[][] layout,
            Coordinate position) {
        String path = design.name().toLowerCase() + "/";
        if (element == LevelElement.SKIP) {
            path += "floor/empty";
        } else if (element == LevelElement.FLOOR) {
            path += "floor/floor_1";
        } else if (element == LevelElement.EXIT) {
            path += "floor/floor_ladder";
        }

        // is field in a non-playable area?
        else if (isInSpace(position, layout)) {
            path += "floor/empty";
        }

        // walls
        else if (isRightWall(position, layout)) {
            path += "wall/right";
        } else if (isLeftWall(position, layout)) {
            path += "wall/left";
        } else if (isSideWall(position, layout)) {
            path += "wall/side";
        } else if (isTopWall(position, layout)) {
            path += "wall/top";
        } else if (isBottomWall(position, layout)) {
            path += "wall/bottom";
        } else if (isBottomAndTopWall(position, layout)) {
            path += "wall/top_bottom";
        }

        // corners
        else if (isBottomLeftCorner(position, layout)) {
            path += "wall/corner_bottom_left";
        } else if (isBottomRightCorner(position, layout)) {
            path += "wall/corner_bottom_right";
        } else if (isUpperRightCorner(position, layout)) {
            path += "wall/corner_upper_right";
        } else if (isUpperLeftCorner(position, layout)) {
            path += "wall/corner_upper_left";
        }

        // fehler zustand
        else {
            path += "floor/empty";
        }

        return "textures/dungeon/" + path + ".png";
    }

    /**
     * Checks which texture must be used for the passed tile based on the surrounding tiles.
     *
     * @param element Tile to check for
     * @param layout The level
     * @return Path to texture
     */
    public static String findTexturePath(Tile element, Tile[][] layout) {
        return findTexturePath(element, layout, element.getLevelElement());
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
                elementLayout[y][x] = layout[y][x].getLevelElement();
            }
        }
        elementLayout[element.getCoordinate().y][element.getCoordinate().x] = elementType;
        return findTexturePath(
                elementType, element.getDesignLabel(), elementLayout, element.getCoordinate());
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

    @SuppressWarnings("unused")
    private static boolean isFourWayCross(Coordinate p, LevelElement[][] layout) {
        return (aboveIsWall(p, layout)
                && belowIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout));
    }

    @SuppressWarnings("unused")
    private static boolean isThreeWayCrossUp(Coordinate p, LevelElement[][] layout) {
        return (!belowIsWall(p, layout)
                && aboveIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout));
    }

    @SuppressWarnings("unused")
    private static boolean isThreeWayCrossDown(Coordinate p, LevelElement[][] layout) {
        return (!aboveIsWall(p, layout)
                && belowIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout));
    }

    @SuppressWarnings("unused")
    private static boolean isThreeWayCrossLeft(Coordinate p, LevelElement[][] layout) {
        return (belowIsWall(p, layout)
                && aboveIsWall(p, layout)
                && leftIsWall(p, layout)
                && !rightIsWall(p, layout));
    }

    @SuppressWarnings("unused")
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
            return layout[p.y + 1][p.x] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean belowIsWall(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y - 1][p.x] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean leftIsWall(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y][p.x - 1] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean rightIsWall(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y][p.x + 1] == LevelElement.WALL;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean aboveIsFloor(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y + 1][p.x] == LevelElement.FLOOR
                    || layout[p.y + 1][p.x] == LevelElement.EXIT;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean belowIsFloor(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y - 1][p.x] == LevelElement.FLOOR
                    || layout[p.y + 1][p.x] == LevelElement.EXIT;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean leftIsFloor(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y][p.x - 1] == LevelElement.FLOOR
                    || layout[p.y + 1][p.x] == LevelElement.EXIT;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean rightIsFloor(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y][p.x + 1] == LevelElement.FLOOR
                    || layout[p.y + 1][p.x] == LevelElement.EXIT;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @SuppressWarnings("unused")
    private static boolean rightIsSkip(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y][p.x + 1] == LevelElement.SKIP;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean leftIsSkip(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y][p.x - 1] == LevelElement.SKIP;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean aboveIsSkip(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y + 1][p.x] == LevelElement.SKIP;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    private static boolean belowIsSkip(Coordinate p, LevelElement[][] layout) {
        try {
            return layout[p.y - 1][p.x] == LevelElement.SKIP;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
}
