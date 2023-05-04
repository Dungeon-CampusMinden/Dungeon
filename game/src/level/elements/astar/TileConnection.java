package level.elements.astar;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.math.Vector2;
import level.elements.tile.Tile;

/**
 * Represents a connection between two tile
 *
 * @author Marti Stuwe
 */
public class TileConnection implements Connection<Tile> {

    private final Tile from;
    private final Tile to;
    private final float cost;

    /**
     * Create a directed connection between two Tiles
     *
     * @param from Start-Tile
     * @param to End-Tile
     */
    public TileConnection(Tile from, Tile to) {
        this.from = from;
        this.to = to;
        this.cost =
                Vector2.dst(
                        from.getCoordinate().x,
                        from.getCoordinate().y,
                        to.getCoordinate().x,
                        to.getCoordinate().y);
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public Tile getFromNode() {
        return from;
    }

    @Override
    public Tile getToNode() {
        return to;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == TileConnection.class
                && ((TileConnection) obj).from == from
                && ((TileConnection) obj).to == to;
    }
}
