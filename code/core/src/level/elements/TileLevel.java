package level.elements;

import level.elements.astar.TileHeuristic;
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
        setRandomEnd();
        setRandomStart();
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

    /**
     * Converts the given LevelElement[][] in a corresponding Tile[][]
     *
     * @param layout The LevelElement[][]
     * @param designLabel The selected Design for the Tiles
     * @return The converted Tile[][]
     */
    protected static Tile[][] convertLevelElementToTile(
            LevelElement[][] layout, DesignLabel designLabel) {
        Tile[][] tileLayout = new Tile[layout.length][layout[0].length];
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[0].length; x++) {
                Coordinate coordinate = new Coordinate(x, y);
                String texturePath =
                        TileTextureFactory.findTexturePath(
                                layout[y][x], designLabel, layout, coordinate);
                tileLayout[y][x] = new Tile(texturePath, coordinate, layout[y][x], designLabel);
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

    /** Connect each tile with it neighbour tiles. */
    protected void makeConnections() {
        for (int x = 0; x < layout[0].length; x++) {
            for (Tile[] tiles : layout) {
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
    protected void addConnectionsToNeighbours(Tile checkTile) {
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
        changeTileElementType(startTile, LevelElement.FLOOR);
    }

    @Override
    public Tile getEndTile() {
        return endTile;
    }

    @Override
    public void setEndTile(Tile end) {
        if (endTile != null) {
            changeTileElementType(endTile, LevelElement.FLOOR);
        }
        endTile = end;
        changeTileElementType(end, LevelElement.EXIT);
    }
}
