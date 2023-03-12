package level.elements;

import com.badlogic.gdx.ai.pfa.Connection;
import java.util.ArrayList;
import java.util.List;
import level.elements.astar.TileHeuristic;
import level.elements.tile.*;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.TileTextureFactory;

/**
 * A level is a 2D-Array of Tiles.
 *
 * @author Andre Matutat
 */
public class TileLevel implements ILevel {
    protected final TileHeuristic tileHeuristic = new TileHeuristic();
    protected Tile startTile;
    protected Tile endTile;
    protected int nodeCount = 0;
    protected Tile[][] layout;

    protected ArrayList<FloorTile> floorTiles = new ArrayList<>();
    protected ArrayList<WallTile> wallTiles = new ArrayList<>();
    protected ArrayList<HoleTile> holeTiles = new ArrayList<>();
    protected ArrayList<DoorTile> doorTiles = new ArrayList<>();
    protected ArrayList<ExitTile> exitTiles = new ArrayList<>();
    protected ArrayList<SkipTile> skipTiles = new ArrayList<>();

    private static final Coordinate CONNECTION_OFFSETS[] = {
        new Coordinate(0, 1), new Coordinate(0, -1), new Coordinate(1, 0), new Coordinate(-1, 0),
    };
    /**
     * Create a new level
     *
     * @param layout The layout of the level.
     */
    public TileLevel(Tile[][] layout) {
        this.layout = layout;
        makeConnections();
        // setRandomEnd();
        setRandomStart();
    }

    /**
     * Create a new Level
     *
     * @param layout The layout of the Level
     * @param designLabel The design the level should have
     */
    public TileLevel(LevelElement[][] layout, DesignLabel designLabel) {
        this.layout = convertLevelElementToTile(layout, designLabel);
        makeConnections();
        // setRandomEnd();
        setRandomStart();
    }

    /**
     * Converts the given LevelElement[][] in a corresponding Tile[][]
     *
     * @param layout The LevelElement[][]
     * @param designLabel The selected Design for the Tiles
     * @return The converted Tile[][]
     */
    protected Tile[][] convertLevelElementToTile(LevelElement[][] layout, DesignLabel designLabel) {
        Tile[][] tileLayout = new Tile[layout.length][layout[0].length];
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[0].length; x++) {
                Coordinate coordinate = new Coordinate(x, y);
                String texturePath =
                        TileTextureFactory.findTexturePath(
                                new TileTextureFactory.LevelPart(
                                        layout[y][x], designLabel, layout, coordinate));
                tileLayout[y][x] =
                        TileFactory.createTile(
                                texturePath, coordinate, layout[y][x], designLabel, this);
            }
        }
        return tileLayout;
    }

    @Override
    public int getNodeCount() {
        return nodeCount;
    }

    @Override
    public TileHeuristic getTileHeuristic() {
        return tileHeuristic;
    }

    /** Connect each tile with its neighbour tiles. */
    protected void makeConnections() {
        for (int x = 0; x < layout[0].length; x++) {
            for (Tile[] tiles : layout) {
                if (tiles[x].getLevel() == null) {
                    tiles[x].setLevel(this);
                }
                if (endTile == null && tiles[x].getLevelElement() == LevelElement.EXIT) {
                    setEndTile(tiles[x]);
                }
                if (tiles[x].isAccessible()) {
                    tiles[x].setIndex(nodeCount++);
                    addConnectionsToNeighbours(tiles[x]);
                }
            }
        }
    }

    /**
     * Check each tile around the tile, if it is accessible add it to the connectionList.
     *
     * @param checkTile Tile to check for.
     */
    public void addConnectionsToNeighbours(Tile checkTile) {
        for (Coordinate v : CONNECTION_OFFSETS) {
            Coordinate c =
                    new Coordinate(
                            checkTile.getCoordinate().x + v.x, checkTile.getCoordinate().y + v.y);
            Tile t = getTileAt(c);
            if (t != null && t.isAccessible()) {
                checkTile.addConnection(t);
            }
        }
    }

    @Override
    public void addFloorTile(FloorTile tile) {
        floorTiles.add(tile);
    }

    @Override
    public void addWallTile(WallTile tile) {
        wallTiles.add(tile);
    }

    @Override
    public void addHoleTile(HoleTile tile) {
        holeTiles.add(tile);
    }

    @Override
    public void addDoorTile(DoorTile tile) {
        doorTiles.add(tile);
    }

    @Override
    public void addExitTile(ExitTile tile) {
        if (getEndTile() != null) {
            changeTileElementType(getEndTile(), LevelElement.FLOOR);
        }
        exitTiles.add(tile);
        setEndTile(tile);
    }

    @Override
    public void addSkipTile(SkipTile tile) {
        skipTiles.add(tile);
    }

    @Override
    public List<FloorTile> getFloorTiles() {
        return floorTiles;
    }
    ;

    @Override
    public List<WallTile> getWallTiles() {
        return wallTiles;
    }

    @Override
    public List<HoleTile> getHoleTiles() {
        return holeTiles;
    }

    @Override
    public List<DoorTile> getDoorTiles() {
        return doorTiles;
    }

    @Override
    public List<ExitTile> getExitTiles() {
        return exitTiles;
    }

    @Override
    public List<SkipTile> getSkipTiles() {
        return skipTiles;
    }

    @Override
    public void removeTile(Tile tile) {
        switch (tile.getLevelElement()) {
            case SKIP -> {
                skipTiles.remove(tile);
            }
            case FLOOR -> {
                floorTiles.remove(tile);
            }
            case WALL -> {
                wallTiles.remove(tile);
            }
            case HOLE -> {
                holeTiles.remove(tile);
            }
            case EXIT -> {
                exitTiles.remove(tile);
                setEndTile(null);
            }
            case DOOR -> {
                doorTiles.remove(tile);
            }
        }
        // remove all connections to the removed Tile
        tile.getConnections()
                .forEach(
                        x ->
                                x.getToNode()
                                        .getConnections()
                                        .forEach(
                                                y -> {
                                                    if (y.getToNode() == tile)
                                                        x.getToNode()
                                                                .getConnections()
                                                                .removeValue(y, true);
                                                }));
        // TODO better fix
        if (tile.isAccessible()) nodeCount--;
    }

    @Override
    public void addTile(Tile tile) {
        switch (tile.getLevelElement()) {
            case SKIP -> addSkipTile((SkipTile) tile);
            case FLOOR -> addFloorTile((FloorTile) tile);
            case WALL -> addWallTile((WallTile) tile);
            case HOLE -> addHoleTile((HoleTile) tile);
            case EXIT -> addExitTile((ExitTile) tile);
            case DOOR -> addDoorTile((DoorTile) tile);
        }
        if (tile.isAccessible()) {
            this.addConnectionsToNeighbours(tile);
            for (Connection<Tile> neighbor : tile.getConnections().items) {
                Tile n = neighbor.getToNode();
                n.addConnection(tile);
            }
            // TODO Better fix
            nodeCount++;
        }
    }

    @Override
    public Tile[][] getLayout() {
        return layout;
    }

    @Override
    public Tile getStartTile() {
        return startTile;
    }

    @Override
    public void setStartTile(Tile start) {
        startTile = start;
    }

    @Override
    public Tile getEndTile() {
        return endTile;
    }

    @Override
    public void setEndTile(Tile end) {
        endTile = end;
    }
}
