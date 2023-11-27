package core.level;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;

import core.level.elements.ILevel;
import core.level.elements.astar.TileConnection;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.path.IPath;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLType;

import java.util.ArrayList;
import java.util.List;

/**
 * A Tile is a field of the level.
 *
 * <p>A Tile has a {@link Coordinate} in the Level, which defines the x and y index in the Level's
 * Tile-Matrix.
 *
 * <p>A Tile can be accessible or non-accessible; this is represented by a boolean value.
 *
 * <p>After you add a Tile to a Level, you must call {@link #level(TileLevel)}.
 *
 * <p>The concrete type of the Tile is defined by the inheriting class.
 */
public abstract class Tile {
    private static final float DEFAULT_FRICTION = 0.8f;
    protected final Coordinate globalPosition;
    private final float friction;
    protected DesignLabel designLabel;
    protected IPath texturePath;
    protected ILevel level;
    protected LevelElement levelElement;
    protected transient Array<Connection<Tile>> connections = new Array<>();
    protected int index;

    /**
     * Create a new Tile.
     *
     * @param texturePath Path to the texture of the tile.
     * @param globalPosition Position of the tile in the global system.
     * @param designLabel Design of the Tile.
     * @param friction The friction of this Tile.
     */
    public Tile(
            final IPath texturePath,
            final Coordinate globalPosition,
            final DesignLabel designLabel,
            float friction) {
        this.texturePath = texturePath;
        this.globalPosition = globalPosition;
        this.designLabel = designLabel;
        this.friction = friction;
    }

    /**
     * Create a new Tile.
     *
     * @param texturePath Path to the texture of the tile.
     * @param globalPosition Position of the tile in the global system.
     * @param designLabel Design of the Tile.
     */
    public Tile(
            final IPath texturePath,
            final Coordinate globalPosition,
            final DesignLabel designLabel) {
        this(texturePath, globalPosition, designLabel, DEFAULT_FRICTION);
    }

    /**
     * Get the texture of this tile.
     *
     * @return Path to the texture of this tile.
     */
    public IPath texturePath() {
        return texturePath;
    }

    /**
     * Change the texture of the tile.
     *
     * @param texture New texture of the tile.
     */
    public void texturePath(final IPath texture) {
        this.texturePath = texture;
    }

    /**
     * Get the coordinate of this tile.
     *
     * @return The coordinate of the tile.
     */
    public Coordinate coordinate() {
        return new Coordinate(globalPosition);
    }

    /**
     * Get the coordinate of this tile as a point.
     *
     * @return The coordinate of the tile as a point.
     */
    public Point position() {
        return coordinate().toPoint();
    }

    /**
     * Get the design label of this tile.
     *
     * @return The DesignLabel of this tile.
     */
    public DesignLabel designLabel() {
        return designLabel;
    }

    /**
     * Defines the element type of this tile.
     *
     * @return The LevelElement of this tile.
     */
    public LevelElement levelElement() {
        return levelElement;
    }

    /**
     * Change the type of the tile.
     *
     * @param newLevelElement New type of the tile.
     */
    public void levelElement(final LevelElement newLevelElement) {
        this.levelElement = newLevelElement;
    }

    /**
     * Get the level where this tile is in.
     *
     * @return The level this tile is in.
     */
    public ILevel level() {
        return level;
    }

    /**
     * Sets the corresponding level for this tile.
     *
     * @param tileLevel The level this tile is in.
     */
    public void level(final TileLevel tileLevel) {
        level = tileLevel;
    }

    /**
     * Used by LibGDX pathfinding.
     *
     * @return The index of this tile.
     */
    public int index() {
        return index;
    }

    /**
     * Used by LibGDX pathfinding.
     *
     * @param index Value of the index.
     */
    public void index(int index) {
        this.index = index;
    }

    /**
     * Get the friction of this tile.
     *
     * @return The friction of this tile.
     */
    public float friction() {
        return this.friction;
    }

    /**
     * Connects two tiles together. This means you can go from one tile to another. Connections are
     * needed to calculate a path through the dungeon.
     *
     * @param to Tile to connect with.
     */
    public void addConnection(final Tile to) {
        if (connections == null) {
            connections = new Array<>();
        }
        connections.add(new TileConnection(this, to));
    }

    /**
     * Used by LibGDX pathfinding.
     *
     * @return All connections to other tiles.
     */
    public Array<Connection<Tile>> connections() {
        return connections;
    }

    /**
     * Returns the direction to a given tile.
     *
     * @param goal To which tile is the direction.
     * @return Can either be north, east, south, west, or a combination of two.
     */
    public Direction[] directionTo(final Tile goal) {
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

    /**
     * Check if this tile is accessible (like a floor) or not (like a wall).
     *
     * @return true if this tile is accessible, false if not.
     */
    public boolean isAccessible() {
        return levelElement.value();
    }

    @DSLType(name = "tile_direction")
    public enum Direction {
        N,
        E,
        S,
        W,
    }
}
