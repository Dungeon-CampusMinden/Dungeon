package level.elements;

import java.util.ArrayList;
import java.util.List;
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

        // upperTile
        Coordinate upper =
                new Coordinate(checkTile.getCoordinate().x, checkTile.getCoordinate().y + 1);
        Tile upperTile = getTileAt(upper);
        // lowerTile
        Coordinate lower =
                new Coordinate(checkTile.getCoordinate().x, checkTile.getCoordinate().y - 1);
        Tile lowerTile = getTileAt(lower);
        // leftTile
        Coordinate left =
                new Coordinate(checkTile.getCoordinate().x - 1, checkTile.getCoordinate().y);
        Tile leftTile = getTileAt(left);
        // rightTile
        Coordinate right =
                new Coordinate(checkTile.getCoordinate().x + 1, checkTile.getCoordinate().y);
        Tile rightTile = getTileAt(right);

        List<Tile> neighbourTiles = new ArrayList<>();
        neighbourTiles.add(upperTile);
        neighbourTiles.add(lowerTile);
        neighbourTiles.add(leftTile);
        neighbourTiles.add(rightTile);

        for (Tile n : neighbourTiles) {
            if (n != null) {
                checkTile.addConnection(n);
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
