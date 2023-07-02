package core.level.elements;

import core.level.Tile;
import core.level.elements.tile.*;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;

import java.util.List;

public interface ILevel extends ITileable {

    /** Mark a random tile as start */
    default void randomStart() {
        startTile(randomTile(LevelElement.FLOOR));
    }

    /**
     * Set the start tile.
     *
     * @param start The start tile.
     */
    void startTile(Tile start);

    /** Mark a random tile as end */
    default void randomEnd() {
        List<FloorTile> floorTiles = floorTiles();
        if (floorTiles.size() <= 1) {
            // not enough Tiles for startTile and ExitTile
            return;
        }
        int startTileIndex = floorTiles.indexOf(startTile());
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
    List<FloorTile> floorTiles();

    /**
     * Returns List of all wall tiles of the level.
     *
     * @return list of wall tiles
     */
    List<WallTile> wallTiles();

    /**
     * Returns List of all hole tiles of the level.
     *
     * @return list of hole tiles
     */
    List<HoleTile> holeTiles();

    /**
     * Returns List of all door tiles of the level.
     *
     * @return list of door tiles
     */
    List<DoorTile> doorTiles();

    /**
     * Returns List of all exit tiles of the level.
     *
     * @return list of exit tiles
     */
    List<ExitTile> exitTiles();

    /**
     * Returns List of all skip tiles of the level.
     *
     * @return list of skip tiles
     */
    List<SkipTile> skipTiles();

    void addConnectionsToNeighbours(Tile checkTile);

    /**
     * F=Floor, W=Wall, E=Exit, S=Skip/Blank
     *
     * @return The level layout in String format
     */
    default String printLevel() {
        StringBuilder output = new StringBuilder();
        for (int y = 0; y < layout().length; y++) {
            for (int x = 0; x < layout()[0].length; x++) {
                if (layout()[y][x].levelElement() == LevelElement.FLOOR) {
                    output.append("F");
                } else if (layout()[y][x].levelElement() == LevelElement.WALL) {
                    output.append("W");
                } else if (layout()[y][x].levelElement() == LevelElement.EXIT) {
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
        ILevel level = tile.level();
        if (level == null) {
            return;
        }
        level.removeTile(tile);
        Tile newTile =
                TileFactory.createTile(
                        TileTextureFactory.findTexturePath(tile, layout(), changeInto),
                        tile.coordinate(),
                        changeInto,
                        tile.designLabel());
        level.layout()[tile.coordinate().y][tile.coordinate().x] = newTile;
        level.addTile(newTile);
    }

    @Override
    default Tile randomTile(LevelElement elementType) {
        return switch (elementType) {
            case SKIP -> skipTiles().size() > 0
                    ? skipTiles().get(RANDOM.nextInt(skipTiles().size()))
                    : null;
            case FLOOR -> floorTiles().size() > 0
                    ? floorTiles().get(RANDOM.nextInt(floorTiles().size()))
                    : null;
            case WALL -> wallTiles().size() > 0
                    ? wallTiles().get(RANDOM.nextInt(wallTiles().size()))
                    : null;
            case HOLE -> holeTiles().size() > 0
                    ? holeTiles().get(RANDOM.nextInt(holeTiles().size()))
                    : null;
            case EXIT -> exitTiles().size() > 0
                    ? exitTiles().get(RANDOM.nextInt(exitTiles().size()))
                    : null;
            case DOOR -> doorTiles().size() > 0
                    ? doorTiles().get(RANDOM.nextInt(doorTiles().size()))
                    : null;
        };
    }

    /**
     * @return random floor tile
     */
    default Tile randomFloorTile() {
        return randomTile(LevelElement.FLOOR);
    }
}
