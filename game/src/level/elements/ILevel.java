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
        List<FloorTile> floorTiles = getFloorTiles();
        if (floorTiles.size() <= 1) {
            // not enough Tiles for startTile and ExitTile
            return;
        }
        int startTileIndex = floorTiles.indexOf(getStartTile());
        int index = RANDOM.nextInt(floorTiles.size() - 1);
        changeTileElementType(
                floorTiles.get(index < startTileIndex ? index : index + 1), LevelElement.EXIT);
    }

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
    void addTile(Tile tile);

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
                        tile.getDesignLabel());
        level.getLayout()[tile.getCoordinate().y][tile.getCoordinate().x] = newTile;
        level.addTile(newTile);
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

    /**
     * @return random floor tile
     */
    default Tile getRandomFloorTile() {
        return getRandomTile(LevelElement.FLOOR);
    }
}
