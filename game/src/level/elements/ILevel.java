package level.elements;

import java.util.List;
import level.elements.tile.*;
import level.tools.LevelElement;
import level.tools.TileTextureFactory;

public interface ILevel extends ITileable {

    /** Mark a random tile as start */
    default void setRandomStart() {
        setStartTile(getRandomTile(LevelElement.FLOOR));
    }

    /**
     * Set the start tile.
     *
     * @param start The start tile.
     */
    void setStartTile(Tile start);

    /** Mark a random tile as end */
    default void setRandomEnd() {
        Tile newEnd = getRandomTile(LevelElement.FLOOR);
        while (newEnd == getStartTile()) {
            newEnd = getRandomTile(LevelElement.FLOOR);
        }
        changeTileElementType(newEnd, LevelElement.EXIT);
    }

    /**
     * Set the end tile.
     *
     * @param end The end tile.
     */
    void setEndTile(Tile end);

    /**
     * Add floor tile to level.
     *
     * @param tile new floor tile
     */
    void addFloorTile(FloorTile tile);

    /**
     * Add wall tile to level.
     *
     * @param tile new wall tile
     */
    void addWallTile(WallTile tile);

    /**
     * Add hole tile to level.
     *
     * @param tile new hole tile
     */
    void addHoleTile(HoleTile tile);

    /**
     * Add door tile to level.
     *
     * @param tile new door tile
     */
    void addDoorTile(DoorTile tile);

    /**
     * Add exit tile to level.
     *
     * @param tile new exit tile
     */
    void addExitTile(ExitTile tile);

    /**
     * Add skip tile to level.
     *
     * @param tile new skip tile
     */
    void addSkipTile(SkipTile tile);

    /**
     * Add unspecific tile to level.
     *
     * @param tile tile to add
     */
    default void addTile(Tile tile) {
        switch (tile.getLevelElement()) {
            case SKIP -> {
                addSkipTile((SkipTile) tile);
            }
            case FLOOR -> {
                addFloorTile((FloorTile) tile);
            }
            case WALL -> {
                addWallTile((WallTile) tile);
            }
            case HOLE -> {
                addHoleTile((HoleTile) tile);
            }
            case EXIT -> {
                addExitTile((ExitTile) tile);
            }
            case DOOR -> {
                addDoorTile((DoorTile) tile);
            }
        }
    }

    /**
     * Removes tile from the level
     *
     * @param tile Tile to be removed
     */
    void removeTile(Tile tile);

    /**
     * Returns List of all floor tiles of the level.
     *
     * @return list of floor tiles
     */
    List<FloorTile> getFloorTiles();

    /**
     * Returns List of all wall tiles of the level.
     *
     * @return list of wall tiles
     */
    List<WallTile> getWallTiles();

    /**
     * Returns List of all hole tiles of the level.
     *
     * @return list of hole tiles
     */
    List<HoleTile> getHoleTiles();

    /**
     * Returns List of all door tiles of the level.
     *
     * @return list of door tiles
     */
    List<DoorTile> getDoorTiles();

    /**
     * Returns List of all exit tiles of the level.
     *
     * @return list of exit tiles
     */
    List<ExitTile> getExitTiles();

    /**
     * Returns List of all skip tiles of the level.
     *
     * @return list of skip tiles
     */
    List<SkipTile> getSkipTiles();

    void addConnectionsToNeighbours(Tile checkTile);

    /**
     * F=Floor, W=Wall, E=Exit, S=Skip/Blank
     *
     * @return The level layout in String format
     */
    default String printLevel() {
        StringBuilder output = new StringBuilder();
        for (int y = 0; y < getLayout().length; y++) {
            for (int x = 0; x < getLayout()[0].length; x++) {
                if (getLayout()[y][x].getLevelElement() == LevelElement.FLOOR) {
                    output.append("F");
                } else if (getLayout()[y][x].getLevelElement() == LevelElement.WALL) {
                    output.append("W");
                } else if (getLayout()[y][x].getLevelElement() == LevelElement.EXIT) {
                    output.append("E");
                } else {
                    output.append("S");
                }
            }
            output.append("\n");
        }
        return output.toString();
    }

    /**
     * Change the type of tile (including changing texture)
     *
     * @param tile The Tile you want to change
     * @param changeInto The LevelElement to change the Tile into.
     */
    default void changeTileElementType(Tile tile, LevelElement changeInto) {
        ILevel level = tile.getLevel();
        if (level == null) {
            return;
        }
        level.removeTile(tile);
        Tile newTile =
                TileFactory.createTile(
                        TileTextureFactory.findTexturePath(tile, getLayout(), changeInto),
                        tile.getCoordinate(),
                        changeInto,
                        tile.getDesignLabel(),
                        level);
        // newTile.setIndex(tile.getIndex());
        level.addTile(newTile);
        // level.addConnectionsToNeighbours(newTile);
        // for (Connection<Tile> neighbor : newTile.getConnections().items) {
        //    Tile n = neighbor.getToNode();
        //    n.addConnection(newTile);
        // }
        level.getLayout()[tile.getCoordinate().y][tile.getCoordinate().x] = newTile;
        if (changeInto == LevelElement.EXIT) {
            level.setEndTile(newTile);
        }
    }

    @Override
    default Tile getRandomTile(LevelElement elementType) {
        return switch (elementType) {
            case SKIP -> getSkipTiles().size() > 0
                    ? getSkipTiles().get(RANDOM.nextInt(getSkipTiles().size()))
                    : null;
            case FLOOR -> getFloorTiles().size() > 0
                    ? getFloorTiles().get(RANDOM.nextInt(getFloorTiles().size()))
                    : null;
            case WALL -> getWallTiles().size() > 0
                    ? getWallTiles().get(RANDOM.nextInt(getWallTiles().size()))
                    : null;
            case HOLE -> getHoleTiles().size() > 0
                    ? getHoleTiles().get(RANDOM.nextInt(getHoleTiles().size()))
                    : null;
            case EXIT -> getExitTiles().size() > 0
                    ? getExitTiles().get(RANDOM.nextInt(getExitTiles().size()))
                    : null;
            case DOOR -> getDoorTiles().size() > 0
                    ? getDoorTiles().get(RANDOM.nextInt(getDoorTiles().size()))
                    : null;
        };
    }
}
