package graphconverter;

import contrib.level.generator.graphBased.LevelGraphGenerator;
import contrib.level.generator.graphBased.RoombasedLevelGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;

import core.level.elements.ILevel;
import core.level.utils.DesignLabel;

import petriNet.Place;

import task.components.DoorComponent;

import taskdependencygraph.TaskDependencyGraph;
import taskdependencygraph.TaskEdge;
import taskdependencygraph.TaskNode;

import java.util.*;

/**
 * Offers functions to generate a {@link LevelGraph} or Petri-Net for a TaskGraph .
 *
 * <p>Use {@link #levelGraphFor(TaskDependencyGraph)} to generate a room-based level for the
 * TaskGraph.
 *
 * <p>Use {@link #petriNetFor(TaskDependencyGraph)} to generate a Petri net for the TaskGraph to.
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
    public static ILevel levelGraphFor(TaskDependencyGraph taskGraph) {
        // Map the node of the task-graph to a levelGraph
        Map<TaskNode, LevelGraph> nodeToLevelGraph = new LinkedHashMap<>();
        // TODO find other solution Store information to find the door to a task
        // Map<Optional<Tuple<core.level.generator.graphBased.levelGraph.Node, Direction>>, Task>
        //        doorEdges = new HashMap();

        // Create a Levelgraph for each Node in the TaskGraph
        taskGraph
                .nodeIterator()
                .forEachRemaining(
                        node -> {
                            LevelGraph levelGraph =
                                    LevelGraphGenerator.generate(
                                            ((TaskNode) node).task().entitySets());
                            nodeToLevelGraph.put((TaskNode) node, levelGraph);
                        });

        // Connect each LevelGraph based on the Edges in the TaskGraph
        taskGraph
                .edgeIterator()
                .forEachRemaining(
                        edge -> {
                            TaskNode start = ((TaskEdge) edge).startNode();
                            TaskNode end = ((TaskEdge) edge).endNode();

                            // todo Store door information
                            /* doorEdges.put(
                            nodeToLevelGraph
                                    .get(start)
                                    .add(
                                            nodeToLevelGraph.get(end),
                                            nodeToLevelGraph.get(start)),
                            end.task());*/
                        });

        // Generate the level
        ILevel level =
                RoombasedLevelGenerator.level(
                        nodeToLevelGraph.values().stream().findFirst().get(),
                        DesignLabel.randomDesign());

        // todo find other solution Close doors and add the door to the manager entity
        /*for (Optional<Tuple<LevelNode, Direction>> tuple :
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
        }*/

        return level;
    }

    // TODO
    public static Place petriNetFor(TaskDependencyGraph taskGraph) {
        return null;
    }
}
