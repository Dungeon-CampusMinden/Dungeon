package core.level;

import core.level.elements.ILevel;
import core.level.elements.astar.TileConnection;
import core.level.elements.astar.TileHeuristic;
import core.level.elements.tile.*;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.position.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private static final Point CONNECTION_OFFSETS[] = {
        new Point(0, 1), new Point(0, -1), new Point(1, 0), new Point(-1, 0),
    };
    /**
     * Create a new level
     *
     * @param layout The layout of the level.
     */
    public TileLevel(Tile[][] layout) {
        this.layout = layout;
        putTilesInLists();
        if (startTile == null) randomStart();
        if (exitTiles.size() == 0) randomEnd();
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
                Point coordinate = new Point(x, y);
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
    public TileHeuristic tileHeuristic() {
        return tileHeuristic;
    }

    /**
     * Check each tile around the tile, if it is accessible add it to the connectionList.
     *
     * @param checkTile Tile to check for.
     */
    public void addConnectionsToNeighbours(Tile checkTile) {
        for (Point v : CONNECTION_OFFSETS) {
            Point c =
                    new Point(
                            checkTile.position().point().x + v.x,
                            checkTile.position().point().y + v.y);
            Tile t = tileAt(c);
            if (t != null
                    && t.isAccessible()
                    && !checkTile.connections().contains(new TileConnection(checkTile, t), false)) {
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
        if (endTile() != null) {
            changeTileElementType(endTile(), LevelElement.FLOOR);
        }
        exitTiles.add(tile);
    }

    @Override
    public void addSkipTile(SkipTile tile) {
        skipTiles.add(tile);
    }

    @Override
    public List<FloorTile> floorTiles() {
        return floorTiles;
    }

    @Override
    public List<WallTile> wallTiles() {
        return wallTiles;
    }

    @Override
    public List<HoleTile> holeTiles() {
        return holeTiles;
    }

    @Override
    public List<DoorTile> doorTiles() {
        return doorTiles;
    }

    @Override
    public List<ExitTile> exitTiles() {
        return exitTiles;
    }

    @Override
    public List<SkipTile> skipTiles() {
        return skipTiles;
    }

    @Override
    public void removeTile(Tile tile) {
        switch (tile.levelElement()) {
            case SKIP -> skipTiles.remove(tile);
            case FLOOR -> floorTiles.remove(tile);
            case WALL -> wallTiles.remove(tile);
            case HOLE -> holeTiles.remove(tile);
            case DOOR -> doorTiles.remove(tile);
            case EXIT -> exitTiles.remove(tile);
        }

        tile.connections()
                .forEach(
                        x ->
                                x.getToNode()
                                        .connections()
                                        .removeValue(
                                                new TileConnection(x.getToNode(), tile), false));
        if (tile.isAccessible()) removeIndex(tile.index());
    }

    private void removeIndex(int index) {
        Arrays.stream(layout)
                .flatMap(x -> Arrays.stream(x).filter(y -> y.index() > index))
                .forEach(x -> x.index(x.index() - 1));
        nodeCount--;
    }

    @Override
    public void addTile(Tile tile) {
        switch (tile.levelElement()) {
            case SKIP -> addSkipTile((SkipTile) tile);
            case FLOOR -> addFloorTile((FloorTile) tile);
            case WALL -> addWallTile((WallTile) tile);
            case HOLE -> addHoleTile((HoleTile) tile);
            case EXIT -> addExitTile((ExitTile) tile);
            case DOOR -> addDoorTile((DoorTile) tile);
        }
        if (tile.isAccessible()) {
            this.addConnectionsToNeighbours(tile);
            tile.connections()
                    .forEach(
                            x -> {
                                if (!x.getToNode()
                                        .connections()
                                        .contains(new TileConnection(x.getToNode(), tile), false))
                                    x.getToNode().addConnection(tile);
                            });
            tile.index(nodeCount++);
        }
        tile.level(this);
    }

    @Override
    public Tile[][] layout() {
        return layout;
    }

    @Override
    public Tile startTile() {
        return startTile;
    }

    @Override
    public void startTile(Tile start) {
        startTile = start;
    }

    @Override
    public Tile endTile() {
        return exitTiles.size() > 0 ? exitTiles.get(0) : null;
    }
}
