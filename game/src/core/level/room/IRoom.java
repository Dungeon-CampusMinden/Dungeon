package core.level.room;

import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.levelgraph.LevelNode;

import java.util.LinkedHashSet;

public interface IRoom {
    /**
     * @return all DoorTiles in this room
     */
    LinkedHashSet<DoorTile> getDoors();

    Tile[][] getLayout();

    LevelNode getLevelNode();
}
