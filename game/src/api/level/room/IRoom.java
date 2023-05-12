package api.level.room;

import api.level.elements.tile.DoorTile;
import api.level.elements.tile.Tile;
import api.level.levelgraph.LevelNode;
import java.util.LinkedHashSet;

public interface IRoom {
    /**
     * @return all DoorTiles in this room
     */
    LinkedHashSet<DoorTile> getDoors();

    Tile[][] getLayout();

    LevelNode getLevelNode();
}
