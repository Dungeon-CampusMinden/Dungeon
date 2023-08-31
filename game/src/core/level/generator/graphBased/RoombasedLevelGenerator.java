package core.level.generator.graphBased;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.generator.graphBased.levelGraph.Direction;
import core.level.generator.graphBased.levelGraph.GraphGenerator;
import core.level.generator.graphBased.levelGraph.LevelGraph;
import core.level.generator.graphBased.levelGraph.Node;
import core.level.utils.*;
import core.utils.IVoidFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * This generator will use the {@link GraphGenerator} and {@link RoomGenerator} to generate a
 * room-based level.
 *
 * <p>Use {@link #level(Set, DesignLabel)} to generate a level. For each Entity-Set, there will be
 * one Room in the level with the given entities.
 *
 * <p>Note that a Room-based Level has no {@link core.level.elements.tile.ExitTile}, so by default,
 * no new level will be loaded by the {@link core.systems.LevelSystem}.
 *
 * <p>Therefore, this Generator does not need to implement the {@link
 * core.level.generator.IGenerator} interface. This Generator also cannot be set as the
 * Level-Generator of the {@link core.systems.LevelSystem}.
 *
 * <p>To use a Room-based Level, use the {@link Game#userOnSetup(IVoidFunction)} method to set your
 * own setup method in your Main method. In your Setup-Method, generate a level by using the {@link
 * #level(Set, DesignLabel)} method and use {@link Game#currentLevel(ILevel)} to set the generated
 * level.
 *
 * <p>Now you can get a dot representation of the level graph in the log.
 */
public class RoombasedLevelGenerator {

    /** Rooms with this amount or fewer entities will be generated small. */
    private static final int MAX_ENTITIES_FOR_SMALL_ROOMS = 2;
    /** Rooms with this amount or more entities will be generated large. */
    private static final int MIN_ENTITIES_FOR_BIG_ROOM = 5;

    private static final Logger LOGGER = Logger.getLogger(RoombasedLevelGenerator.class.getName());

    /**
     * Get a room-based level with a room for each given entity-set.
     *
     * <p>Now you can get a dot representation of the level graph in the log.
     *
     * @param entities Collection of Entity-Sets. For each Entity-Set, one room will be added to the
     *     level, and the entities will be placed in this room.
     * @param designLabel Design of the level.
     * @return The generated level.
     */
    public static ILevel level(Set<Set<Entity>> entities, DesignLabel designLabel) {
        LevelGraph graph = GraphGenerator.generate(entities);
        RoomGenerator roomG = new RoomGenerator();
        LOGGER.info(graph.toDot());
        // generate TileLevel for each Node
        graph.nodes()
                .forEach(
                        node ->
                                node.level(
                                        new TileLevel(
                                                roomG.layout(sizeFor(node), node.neighbours()),
                                                designLabel)));

        for (Node node : graph.nodes()) {
            ILevel level = node.level();
            // remove trapdoor exit, in rooms we only use doors
            List<Tile> exits = new ArrayList<>(level.exitTiles());
            exits.forEach(exit -> level.changeTileElementType(exit, LevelElement.FLOOR));
            configureDoors(node);
            node.level().onFirstLoad(() -> node.entities().forEach(Game::add));
        }
        return graph.root().level();
    }

    private static LevelSize sizeFor(Node node) {
        AtomicInteger count = new AtomicInteger();
        node.entities()
                .forEach(
                        e -> {
                            if (e.isPresent(PositionComponent.class)
                                    && e.isPresent(DrawComponent.class)) count.getAndIncrement();
                        });

        if (count.get() <= MAX_ENTITIES_FOR_SMALL_ROOMS) return LevelSize.SMALL;
        else if (count.get() >= MIN_ENTITIES_FOR_BIG_ROOM) return LevelSize.LARGE;
        else return LevelSize.MEDIUM;
    }

    /**
     * Find each door in each room and connect it to the corresponding door in the other room.
     *
     * <p>Will also set the doorstep coordinate, so you will not spawn on the door after you have
     * entered it.
     *
     * @param node Node to configure the doors for.
     */
    private static void configureDoors(Node node) {
        for (DoorTile door : node.level().doorTiles()) {
            Direction doorDirection =
                    core.level.utils.GeneratorUtils.doorDirection(node.level(), door);

            // find neighbour door
            Node neighbour = node.neighbours()[doorDirection.value()];
            DoorTile neighbourDoor = null;
            for (DoorTile doorTile : neighbour.level().doorTiles())
                if (Direction.opposite(doorDirection)
                        == core.level.utils.GeneratorUtils.doorDirection(
                                neighbour.level(), doorTile)) {
                    neighbourDoor = doorTile;
                    break;
                }
            door.setOtherDoor(neighbourDoor);

            // place door steps
            Tile doorStep = null;
            switch (doorDirection) {
                case NORTH -> doorStep =
                        door.level()
                                .tileAt(
                                        new Coordinate(
                                                door.coordinate().x, door.coordinate().y - 1));
                case EAST -> doorStep =
                        door.level()
                                .tileAt(
                                        new Coordinate(
                                                door.coordinate().x - 1, door.coordinate().y));
                case SOUTH -> doorStep =
                        door.level()
                                .tileAt(
                                        new Coordinate(
                                                door.coordinate().x, door.coordinate().y + 1));
                case WEST -> doorStep =
                        door.level()
                                .tileAt(
                                        new Coordinate(
                                                door.coordinate().x + 1, door.coordinate().y));
            }
            door.setDoorstep(doorStep);
        }
    }
}
