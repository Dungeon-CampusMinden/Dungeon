package graphconverter;

import core.level.elements.ILevel;
import core.level.generator.graphBased.RoombasedLevelGenerator;
import core.level.generator.graphBased.levelGraph.Direction;
import core.level.generator.graphBased.levelGraph.GraphGenerator;
import core.level.generator.graphBased.levelGraph.LevelGraph;
import core.level.utils.DesignLabel;
import core.level.utils.GeneratorUtils;
import core.utils.Tuple;

import graph.Edge;
import graph.Graph;
import graph.Node;

import petriNet.Place;

import task.Task;
import task.components.DoorComponent;

import java.util.*;

/**
 * Offers functions to generate a {@link LevelGraph} or Petri-Net for a TaskGraph .
 *
 * <p>Use {@link #levelGraphFor(Graph)} to generate a room-based level for the TaskGraph.
 *
 * <p>Use {@link #petriNetFor(Graph)} to generate a Petri net for the TaskGraph to.
 */
public class TaskGraphConverter {

    /**
     * Generate a room-based level for the given TaskGraph.
     *
     * <p>For each Node, a level graph is generated (this will consist of multiple rooms for each
     * Entity-Set in the Node).
     *
     * <p>Then, the different level graphs for the different nodes will be connected based on the
     * TaskGraph-Edges. The doors between the graphs will be closed, and a {@link DoorComponent} for
     * the manager Entity of the Task to which the door leads will be created, so the entity can
     * open the door if it gets activated.
     *
     * <p>Note: The Nodes in the Graph need to store a Task that has a Manager Entity.
     *
     * <p>Note: The Edges in the graph should not be duplicated (so only one Edge from A to B and
     * not one from B to A as well).
     *
     * @param taskGraph
     * @return the start room
     * @see RoombasedLevelGenerator
     */
    public static ILevel levelGraphFor(Graph taskGraph) {
        // Map the node of the task-graph to a levelGraph
        Map<Node, LevelGraph> nodeToLevelGraph = new LinkedHashMap<>();
        // Store information to find the door to a task
        Map<Optional<Tuple<core.level.generator.graphBased.levelGraph.Node, Direction>>, Task>
                doorEdges = new HashMap();

        // Create a Levelgraph for each Node in the TaskGraph
        taskGraph
                .nodeIterator()
                .forEachRemaining(
                        node -> {
                            LevelGraph levelGraph =
                                    GraphGenerator.generate(((Node) node).entities());
                            nodeToLevelGraph.put((Node) node, levelGraph);
                        });

        // Connect each LevelGraph based on the Edges in the TaskGraph
        taskGraph
                .edgeIterator()
                .forEachRemaining(
                        edge -> {
                            Node start = ((Edge) edge).startNode();
                            Node end = ((Edge) edge).endNode();

                            // Store door information
                            doorEdges.put(
                                    nodeToLevelGraph
                                            .get(start)
                                            .add(
                                                    nodeToLevelGraph.get(end),
                                                    nodeToLevelGraph.get(start)),
                                    end.task());
                        });

        // Generate the level
        ILevel level =
                RoombasedLevelGenerator.level(
                        nodeToLevelGraph.values().stream().findFirst().get(),
                        DesignLabel.randomDesign());

        // Close doors and add the door to the manager entity
        for (Optional<Tuple<core.level.generator.graphBased.levelGraph.Node, Direction>> tuple :
                doorEdges.keySet()) {
            tuple.flatMap(t -> GeneratorUtils.doorAt(t.a().level(), t.b()))
                    .ifPresent(
                            doorTile -> {
                                doorTile.close();
                                doorEdges
                                        .get(tuple)
                                        .managerEntity()
                                        .ifPresent(
                                                entity ->
                                                        entity.addComponent(
                                                                new DoorComponent(doorTile)));
                            });
        }

        return level;
    }

    // TODO
    public static Place petriNetFor(Graph taskGraph) {
        return null;
    }
}
