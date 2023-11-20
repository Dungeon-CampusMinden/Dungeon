package core.level;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;

import core.Entity;
import core.level.elements.ILevel;
import core.level.elements.astar.TileConnection;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Constants;
import core.utils.Point;

import dsl.semanticanalysis.types.typebuilding.annotation.DSLType;

import java.util.ArrayList;
import java.util.List;

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
    private final float friction;

    /**
     * Creates a new Tile.
     *
     * @param texturePath Path to the texture of the tile.
     * @param globalPosition Position of the tile in the global system.
     * @param designLabel Design of the Tile
     * @param level The level this Tile belongs to
     * @param friction The friction of this Tile
     */
    public Tile(
            String texturePath,
            Coordinate globalPosition,
            DesignLabel designLabel,
            ILevel level,
            float friction) {
        this.texturePath = texturePath;
        this.globalPosition = globalPosition;
        this.designLabel = designLabel;
        this.level = level;
        this.friction = friction;
    }
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
        this(texturePath, globalPosition, designLabel, level, Constants.DEFAULT_FRICTION);
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
    public String texturePath() {
        return texturePath;
    }

    /**
     * Change texture of the tile.
     *
     * @param texture New texture of the tile.
     */
    public void texturePath(String texture) {
        this.texturePath = texture;
    }

    /**
     * @return The global coordinate of the tile.
     */
    public Coordinate coordinate() {
        return globalPosition;
    }

    /**
     * @return The global coordinate of the tile as point.
     */
    public Point position() {
        return coordinate().toPoint();
    }

    /**
     * @return the DesignLabel of this tile
     */
    public DesignLabel designLabel() {
        return designLabel;
    }

    /**
     * @return the LevelElement of this tile
     */
    public LevelElement levelElement() {
        return levelElement;
    }

    /**
     * Change the type of the tile.
     *
     * @param newLevelElement New type of the tile.
     */
    public void levelElement(LevelElement newLevelElement) {
        this.levelElement = newLevelElement;
    }

    /**
     * @return the Level this tile is in
     */
    public ILevel level() {
        return level;
    }

    /**
     * Sets the corresponding level for this tile.
     *
     * @param tileLevel The level this tile is in
     */
    public void level(TileLevel tileLevel) {
        level = tileLevel;
    }

    /**
     * Used by libGDX pathfinding
     *
     * @return the index of this tile
     */
    public int index() {
        return index;
    }

    /**
     * Used by libGDX pathfinding
     *
     * @param index value of the index
     */
    public void index(int index) {
        this.index = index;
    }

    /**
     * Get friction of this tile.
     *
     * @return the friction of this tile
     */
    public float friction() {
        return this.friction;
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
     * @return all connections to other tile
     */
    public Array<Connection<Tile>> connections() {
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
        if (globalPosition.x < goal.coordinate().x) {
            directions.add(Direction.E);
        } else if (globalPosition.x > goal.coordinate().x) {
            directions.add(Direction.W);
        }
        if (globalPosition.y < goal.coordinate().y) {
            directions.add(Direction.N);
        } else if (globalPosition.y > goal.coordinate().y) {
            directions.add(Direction.S);
        }
        return directions.toArray(new Direction[0]);
    }

    public abstract boolean isAccessible();

    // --------------------------- For LibGDX Pathfinding ---------------------------
    @DSLType(name = "tile_direction")
    public enum Direction {
        N,
        E,
        S,
        W,
    }

    // --------------------------- End LibGDX Pathfinding ---------------------------

}
