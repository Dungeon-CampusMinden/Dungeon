package level.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import level.elements.astar.TileConnection;
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
        putTilesInLists();
        if (startTile == null) setRandomStart();
        if (exitTiles.size() == 0) setRandomEnd();
    }

    /**
     * Create a new Level
     *
     * @param layout The layout of the Level
     * @param designLabel The design the level should have
     */
    public TileLevel(LevelElement[][] layout, DesignLabel designLabel) {
        this(convertLevelElementToTile(layout, designLabel));
    }

    private void putTilesInLists() {
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[0].length; x++) {
                addTile(layout[y][x]);
            }
        }
    }

    /**
     * Converts the given LevelElement[][] in a corresponding Tile[][]
     *
     * @param layout The LevelElement[][]
     * @param designLabel The selected Design for the Tiles
     * @return The converted Tile[][]
     */
    private static Tile[][] convertLevelElementToTile(
            LevelElement[][] layout, DesignLabel designLabel) {
        Tile[][] tileLayout = new Tile[layout.length][layout[0].length];
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[0].length; x++) {
                Coordinate coordinate = new Coordinate(x, y);
                String texturePath =
                        TileTextureFactory.findTexturePath(
                                new TileTextureFactory.LevelPart(
                                        layout[y][x], designLabel, layout, coordinate));
                tileLayout[y][x] =
                        TileFactory.createTile(texturePath, coordinate, layout[y][x], designLabel);
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
            if (t != null
                    && t.isAccessible()
                    && !checkTile
                            .getConnections()
                            .contains(new TileConnection(checkTile, t), false)) {
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
    }

    @Override
    public void addSkipTile(SkipTile tile) {
        skipTiles.add(tile);
    }

    @Override
    public List<FloorTile> getFloorTiles() {
        return floorTiles;
    }

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
            case SKIP -> skipTiles.remove(tile);
            case FLOOR -> floorTiles.remove(tile);
            case WALL -> wallTiles.remove(tile);
            case HOLE -> holeTiles.remove(tile);
            case DOOR -> doorTiles.remove(tile);
            case EXIT -> exitTiles.remove(tile);
        }

        tile.getConnections()
                .forEach(
                        x ->
                                x.getToNode()
                                        .getConnections()
                                        .removeValue(
                                                new TileConnection(x.getToNode(), tile), false));
        if (tile.isAccessible()) removeIndex(tile.getIndex());
    }

    private void removeIndex(int index) {
        Arrays.stream(layout)
                .flatMap(x -> Arrays.stream(x).filter(y -> y.getIndex() > index))
                .forEach(x -> x.setIndex(x.getIndex() - 1));
        nodeCount--;
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
            tile.getConnections()
                    .forEach(
                            x -> {
                                if (!x.getToNode()
                                        .getConnections()
                                        .contains(new TileConnection(x.getToNode(), tile), false))
                                    x.getToNode().addConnection(tile);
                            });
            tile.setIndex(nodeCount++);
        }
        tile.setLevel(this);
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
        return exitTiles.size() > 0 ? exitTiles.get(0) : null;
    }
}
