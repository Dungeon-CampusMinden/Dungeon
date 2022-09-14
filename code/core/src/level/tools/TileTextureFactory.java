package level.tools;

import level.elements.Tile;

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
            LevelElement element,
            DesignLabel design,
            LevelElement[][] layout,
            Coordinate position) {}

    /**
     * Checks which texture must be used for the passed field based on the surrounding fields.
     *
     * @param levelPart a part of a level
     * @return Path to texture
     */
    public static String findTexturePath(LevelPart levelPart) {
        String prefixPath = "textures/dungeon/" + levelPart.design().name().toLowerCase() + "/";

        String path = findTexturePathFloor(levelPart);
        if (path != null) {
            return prefixPath + path + ".png";
        }

        path = findTexturePathWall(levelPart);
        if (path != null) {
            return prefixPath + path + ".png";
        }

        path = findTexturePathCorner(levelPart);
        if (path != null) {
            return prefixPath + path + ".png";
        }

        // fehler zustand
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
                new LevelPart(
                        elementType,
                        element.getDesignLabel(),
                        elementLayout,
                        element.getCoordinate()));
    }

    private static String findTexturePathFloor(LevelPart levelPart) {
        if (levelPart.element() == LevelElement.SKIP) {
            return "floor/empty";
        } else if (levelPart.element() == LevelElement.FLOOR) {
            return "floor/floor_1";
        } else if (levelPart.element() == LevelElement.EXIT) {
            return "floor/floor_ladder";
        }
        // is field in a non-playable area?
        else if (isInSpace(levelPart.position(), levelPart.layout())) {
            return "floor/empty";
        }
        return null;
    }

    private static String findTexturePathWall(LevelPart levelPart) {
        if (isRightWall(levelPart.position(), levelPart.layout())) {
            return "wall/right";
        } else if (isLeftWall(levelPart.position(), levelPart.layout())) {
            return "wall/left";
        } else if (isSideWall(levelPart.position(), levelPart.layout())) {
            return "wall/side";
        } else if (isTopWall(levelPart.position(), levelPart.layout())) {
            return "wall/top";
        } else if (isBottomWall(levelPart.position(), levelPart.layout())) {
            return "wall/bottom";
        } else if (isBottomAndTopWall(levelPart.position(), levelPart.layout())) {
            return "wall/top_bottom";
        }
        return null;
    }

    private static String findTexturePathCorner(LevelPart levelPart) {
        if (isBottomLeftCorner(levelPart.position(), levelPart.layout())) {
            return "wall/corner_bottom_left";
        } else if (isBottomRightCorner(levelPart.position(), levelPart.layout())) {
            return "wall/corner_bottom_right";
        } else if (isUpperRightCorner(levelPart.position(), levelPart.layout())) {
            return "wall/corner_upper_right";
        } else if (isUpperLeftCorner(levelPart.position(), levelPart.layout())) {
            return "wall/corner_upper_left";
        }
        return null;
    }

    private static boolean isInSpace(Coordinate p, LevelElement[][] layout) {
        return isInSpaceSkip(p, layout) || isInSpaceWall(p, layout);
    }

    private static boolean isInSpaceSkip(Coordinate p, LevelElement[][] layout) {
        return belowIsSkip(p, layout)
                && aboveIsSkip(p, layout)
                && leftIsSkip(p, layout)
                && rightIsFloor(p, layout);
    }

    private static boolean isInSpaceWall(Coordinate p, LevelElement[][] layout) {
        return belowIsWall(p, layout)
                && aboveIsWall(p, layout)
                && leftIsWall(p, layout)
                && rightIsWall(p, layout);
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
