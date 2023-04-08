package level.elements.tile;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;
import ecs.entities.Entity;
import java.util.ArrayList;
import java.util.List;
import level.elements.ILevel;
import level.elements.TileLevel;
import level.elements.astar.TileConnection;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import tools.Point;

/**
 * A Tile is a field of the level.
 *
 * @author Andre Matutat
 */
public abstract class Tile {
    protected final Coordinate globalPosition;
    protected DesignLabel designLabel;
    protected String texturePath;

    protected ILevel level;
    protected LevelElement levelElement;
    protected transient Array<Connection<Tile>> connections = new Array<>();
    protected int index;

    /**
     * Creates a new Tile.
     *
     * @param texturePath Path to the texture of the tile.
     * @param globalPosition Position of the tile in the global system.
     * @param designLabel Design of the Tile
     * @param level The level this Tile belongs to
     */
    public Tile(
            String texturePath, Coordinate globalPosition, DesignLabel designLabel, ILevel level) {
        this.texturePath = texturePath;
        this.globalPosition = globalPosition;
        this.designLabel = designLabel;
        this.level = level;
    }

    /**
     * What happens, if someone moves on this Tile?
     *
     * @param element Who entered this Tile?
     */
    public abstract void onEntering(Entity element);

    /**
     * @return path to the texture of this tile
     */
    public String getTexturePath() {
        return texturePath;
    }

    /**
     * Change texture of the tile.
     *
     * @param texture New texture of the tile.
     */
    public void setTexturePath(String texture) {
        this.texturePath = texture;
    }

    /**
     * @return The global coordinate of the tile.
     */
    public Coordinate getCoordinate() {
        return globalPosition;
    }

    /**
     * @return The global coordinate of the tile as point.
     */
    public Point getCoordinateAsPoint() {
        return getCoordinate().toPoint();
    }

    /**
     * @return the DesignLabel of this tile
     */
    public DesignLabel getDesignLabel() {
        return designLabel;
    }

    /**
     * @return the LevelElement of this tile
     */
    public LevelElement getLevelElement() {
        return levelElement;
    }

    /**
     * Change the type of the tile.
     *
     * @param newLevelElement New type of the tile.
     */
    public void setLevelElement(LevelElement newLevelElement) {
        this.levelElement = newLevelElement;
    }

    /**
     * @return the Level this tile is in
     */
    public ILevel getLevel() {
        return level;
    }

    /**
     * Sets the corresponding level for this tile.
     *
     * @param tileLevel The level this tile is in
     */
    public void setLevel(TileLevel tileLevel) {
        level = tileLevel;
    }

    /**
     * Used by libGDX pathfinding
     *
     * @return the index of this tile
     */
    public int getIndex() {
        return index;
    }

    /**
     * Used by libGDX pathfinding
     *
     * @param index value of the index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * connects to tile together. this mean you can go from one tile to another. Connections are
     * needed to calculate a path through the dungeon.
     *
     * @param to Tile to connect with.
     */
    public void addConnection(Tile to) {
        if (connections == null) {
            connections = new Array<>();
        }
        connections.add(new TileConnection(this, to));
    }

    /**
     * Used by libGDX pathfinding
     *
     * @return all connections to other tiles
     */
    public Array<Connection<Tile>> getConnections() {
        return connections;
    }

    /**
     * Returns the direction to a given tile.
     *
     * @param goal To which tile is the direction.
     * @return Can either be north, east, south, west or a combination of two.
     */
    public Direction[] directionTo(Tile goal) {
        List<Direction> directions = new ArrayList<>();
        if (globalPosition.x < goal.getCoordinate().x) {
            directions.add(Direction.E);
        } else if (globalPosition.x > goal.getCoordinate().x) {
            directions.add(Direction.W);
        }
        if (globalPosition.y < goal.getCoordinate().y) {
            directions.add(Direction.N);
        } else if (globalPosition.y > goal.getCoordinate().y) {
            directions.add(Direction.S);
        }
        return directions.toArray(new Direction[0]);
    }

    public abstract boolean isAccessible();

    // --------------------------- For LibGDX Pathfinding ---------------------------
    public enum Direction {
        N,
        E,
        S,
        W,
    }

    // --------------------------- End LibGDX Pathfinding ---------------------------

}
