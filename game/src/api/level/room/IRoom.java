package api.level.room;

import java.util.LinkedHashSet;

import api.level.elements.tile.DoorTile;
import api.level.elements.tile.Tile;
import api.level.levelgraph.LevelNode;

public interface IRoom {
    /**
     * @return all DoorTiles in this room
     */
    LinkedHashSet<DoorTile> getDoors();

    Tile[][] getLayout();

    LevelNode getLevelNode();
}
