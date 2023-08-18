package core.level.generator.graphBased;

import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LevelGenerator {

    public static ILevel level(Set<Set<Entity>> entities, DesignLabel designLabel) {
        LevelGraph graph = GraphGenerator.generate(entities);
        RoomGenerator roomG = new RoomGenerator();
        graph.nodes()
                .forEach(
                        node ->
                                node.level(
                                        new TileLevel(
                                                roomG.layout(
                                                        LevelSize.randomSize(), node.neighbours()),
                                                designLabel)));

        for (LevelGraph.Node node : graph.nodes()) {
            ILevel level = node.level();
            List<Tile> exits = new ArrayList<>(level.exitTiles());
            exits.forEach(exit -> level.changeTileElementType(exit, LevelElement.FLOOR));
            configureDoors(node);
            node.level().onFirstLoad(() -> node.entities().forEach(Game::add));
        }
        return graph.root().level();
    }

    private static void configureDoors(LevelGraph.Node node) {

        for (DoorTile door : node.level().doorTiles()) {
            LevelGraph.Direction doorDirection = doorDirection(node, door);
            LevelGraph.Node neighbour = node.neighbours()[doorDirection.value()];
            DoorTile neighbourDoor = null;
            for (DoorTile doorTile : neighbour.level().doorTiles())
                if (doorDirection == doorDirection(neighbour, doorTile)) {
                    neighbourDoor = doorTile;
                    break;
                }

            door.setOtherDoor(neighbourDoor);
            // place door steps
            Tile doorStep = null;
            switch (doorDirection) {
                case NORTH -> door.level()
                        .tileAt(new Coordinate(door.coordinate().x, door.coordinate().y - 1));
                case EAST -> door.level()
                        .tileAt(new Coordinate(door.coordinate().x - 1, door.coordinate().y));
                case SOUTH -> door.level()
                        .tileAt(new Coordinate(door.coordinate().x, door.coordinate().y + 1));
                case WEST -> door.level()
                        .tileAt(new Coordinate(door.coordinate().x + 1, door.coordinate().y));
            }
            door.setDoorstep(doorStep);
        }
    }

    private static LevelGraph.Direction doorDirection(LevelGraph.Node node, DoorTile door) {
        // todo
        return null;
    }
}
