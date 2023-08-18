package core.level.generator.graphBased.room;

import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.generator.graphBased.graph.LevelNode;

import java.util.LinkedHashSet;

public interface IRoom {
    /**
     * @return all DoorTiles in this room
     */
    LinkedHashSet<DoorTile> doors();

    Tile[][] layout();

    LevelNode levelNode();
}
