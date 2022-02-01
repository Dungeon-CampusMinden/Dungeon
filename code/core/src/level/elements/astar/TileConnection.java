package level.elements.astar;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.math.Vector2;
import level.elements.room.Tile;

/**
 * Represents a connection between two tile
 *
 * @author Marti Stuwe
 */
public class TileConnection implements Connection<Tile> {

    private final Tile from;
    private final Tile to;
    private final float cost;

    public TileConnection(Tile from, Tile to) {
        this.from = from;
        this.to = to;
        this.cost =
                Vector2.dst(
                        from.getGlobalPosition().x,
                        from.getGlobalPosition().y,
                        to.getGlobalPosition().x,
                        to.getGlobalPosition().y);
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
}
