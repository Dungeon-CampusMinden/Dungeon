package graph;

import contrib.level.generator.GeneratorUtils;
import contrib.level.generator.graphBased.LevelGraphGenerator;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import dsl.interpreter.DSLInterpreter;
import graph.petrinet.PetriNet;
import graph.petrinet.PetriNetFactory;
import graph.taskdependencygraph.TaskDependencyGraph;
import graph.taskdependencygraph.TaskNode;
import java.util.*;
import task.Task;
import task.game.components.DoorComponent;
import task.game.components.TaskComponent;

/**
 * Offers functions to generate a {@link LevelGraph} or Petri-Net for a TaskGraph .
 *
 * <p>Use {@link #callTaskBuilderFor(TaskDependencyGraph, DSLInterpreter)} to execute the
 * TaskBuilder for each {@link Task} in a {@link TaskDependencyGraph}.
 *
 * <p>Use {@link #levelGraphFor(TaskDependencyGraph)} to generate a room-based level for the
 * TaskGraph.
 *
 * <p>Use {@link #petriNetFor(TaskDependencyGraph)} to generate a Petri net for the TaskGraph to.
 *
 * <p>Use {@link #convert(TaskDependencyGraph, DSLInterpreter)} to execute the complete chain.
 */
public class TaskGraphConverter {

  /**
   * Execute the complete chain of {@link #callTaskBuilderFor(TaskDependencyGraph, DSLInterpreter)},
   * {@link #levelGraphFor(TaskDependencyGraph)}, and {@link #petriNetFor(TaskDependencyGraph)}.
   *
   * @param graph Graph to execute the full chain of conversion on.
   * @param dslInterpreter foo
   * @return the start room
   */
  public static ILevel convert(
      final TaskDependencyGraph graph, final DSLInterpreter dslInterpreter) {
    callTaskBuilderFor(graph, dslInterpreter);
    ILevel level = levelGraphFor(graph);
    petriNetFor(graph);
    return level;
  }

  /**
   * Execute the TaskBuilder for each {@link Task} in the given graph.
   *
   * @param graph graph that contains the tasks.
   * @param interpreter foo
   */
  public static void callTaskBuilderFor(
      final TaskDependencyGraph graph, final DSLInterpreter interpreter) {
    graph
        .nodeIterator()
        .forEachRemaining(
            taskNode ->
                interpreter
                    .buildTask(taskNode.task())
                    .ifPresent(
                        buildTask -> taskNode.task().entitieSets((Set<Set<Entity>>) buildTask)));
  }

  /**
   * Generate a room-based level for the given TaskGraph.
   *
   * <p>For each Node, a level graph is generated (this will consist of multiple rooms for each
   * Entity-Set in the Node).
   *
   * <p>Then, the different level graphs for the different nodes will be connected based on the
   * TaskGraph-Edges. The doors between the graphs will be closed, and a {@link DoorComponent} for
   * the manager Entity of the Task to which the door leads will be created, so the entity can open
   * the door if it gets activated.
   *
   * <p>Note: The Nodes in the Graph need to store a Task that has a Manager Entity.
   *
   * <p>Note: The Edges in the graph should not be duplicated (so only one Edge from A to B and not
   * one from B to A as well).
   *
   * <p>Important: Call the TaskBuilder for each Task before.
   *
   * @param taskGraph graph to create the level for
   * @return the start room
   * @see RoomBasedLevelGenerator
   */
  public static ILevel levelGraphFor(final TaskDependencyGraph taskGraph) {
    // Map the node of the task-graph to a levelGraph
    Map<TaskNode, LevelGraph> nodeToLevelGraph = new LinkedHashMap<>();
    // used to connect the doors to the task manager later
    Map<LevelGraph, Task> graphToTask = new LinkedHashMap<>();
    // Create a Level-graph for each Node in the TaskGraph
    taskGraph
        .nodeIterator()
        .forEachRemaining(
            node -> {
              LevelGraph levelGraph = LevelGraphGenerator.generate(node.task().entitySets());
              nodeToLevelGraph.put(node, levelGraph);
              graphToTask.put(levelGraph, node.task());
            });

    // Connect each LevelGraph based on the Edges in the TaskGraph
    taskGraph
        .edgeIterator()
        .forEachRemaining(
            edge -> {
              TaskNode start = edge.startNode();
              TaskNode end = edge.endNode();
              LevelGraph.add(nodeToLevelGraph.get(start), nodeToLevelGraph.get(end));
            });

    // the first level-graph is set at the root-graph (the game will start in this graph)
    // each other node needs to be connected (directly, or indirectly) with this one.
    LevelGraph rootGraph =
        nodeToLevelGraph.values().stream()
            .findFirst()
            .orElseThrow(
                () -> new RuntimeException("There should be a Room to this Node but is not!"));

    connectUnconnectedGraphs(rootGraph, nodeToLevelGraph.values());

    // Generate the level
    ILevel level = RoomBasedLevelGenerator.level(rootGraph, DesignLabel.randomDesign());
    connectDoorsWithTaskManager(graphToTask);

    return level;
  }

  /**
   * Connects unconnected graphs by adding them to a root graph.
   *
   * <p>This method takes a root graph and a collection of level graphs. It checks if the nodes in
   * the root graph are connected to any of the level graphs in the collection. If not, it adds the
   * first unconnected level graph to the root graph and recursively continues to connect
   * unconnected level graphs until all are connected.
   *
   * @param rootGraph The root graph to which unconnected level graphs will be connected.
   * @param levelGraphs A collection of level graphs to be connected to the root graph.
   * @return {@code true} if all level graphs are connected to the root graph, {@code false}
   *     otherwise (never, return value is used for the recursion).
   */
  private static boolean connectUnconnectedGraphs(
      final LevelGraph rootGraph, final Collection<LevelGraph> levelGraphs) {
    for (LevelGraph levelGraph : levelGraphs) {
      Set<LevelNode> tmp = rootGraph.nodes();
      tmp.removeIf(n -> n.originGraph() != levelGraph);
      // each graph contains all the nodes, also the nodes of each graph it is connected with,
      // so if this set is empty it means root and graph are not connected
      if (tmp.isEmpty()) {
        if (!LevelGraph.add(rootGraph, levelGraph))
          throw new RuntimeException("Could not connects Graphs");
        return connectUnconnectedGraphs(rootGraph, levelGraphs);
      }
    }
    return true;
  }

  /**
   * Connects each door that connects two level graphs to the manager entity responsible for the
   * respective graph part.
   *
   * <p>This function will find each door that connects tasks and will then add the task to the door
   * component of the manager component of the task that is associated with the level graph part.
   *
   * <p>The doors will be closed, and the manager will have to open them (normally this will happen
   * if the task of the manager is set to active).
   *
   * <p>The doors will be accessible for the player if both sides of the doors are open.
   *
   * @param levelGraphToTask Mapping of the Level-graphs to the Tasks, so this function knows which
   *     Task is associated with which level graph.
   */
  private static void connectDoorsWithTaskManager(Map<LevelGraph, Task> levelGraphToTask) {
    Map<Task, Set<DoorTile>> taskToDoor = new HashMap<>();
    // since all graphs are structurally the same, I just need on graph to start iterate over
    LevelGraph lg =
        levelGraphToTask.keySet().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No Level-graph given"));

    // iterate over each node in the level-graph
    for (LevelNode levelNode : lg.nodes()) {

      // find all edges that connect this node to a node of another graph (this is an edge
      // between tasks)
      Set<Direction> dirs = levelNode.whereNeighboursFromOtherGraphs();

      // for each edge to another graph, find the door and at it to the Map.
      for (Direction dir : dirs) {
        DoorTile door =
            GeneratorUtils.doorAt(levelNode.level(), dir)
                .orElseThrow(() -> new RuntimeException("There should be a door but is not!"));
        Task t = levelGraphToTask.get(levelNode.originGraph());
        if (taskToDoor.containsKey(t)) taskToDoor.get(t).add(door);
        else {
          Set<DoorTile> set = new HashSet<>();
          set.add(door);
          taskToDoor.put(t, set);
        }
      }
    }

    // Close Doors and connect it with the task manager
    taskToDoor.forEach(
        (task, doorTiles) -> {
          DoorComponent doorComponent = new DoorComponent(doorTiles);
          doorTiles.forEach(DoorTile::close);
          task.managerEntity()
              .ifPresentOrElse(
                  entity -> entity.add(doorComponent),
                  () -> {
                    Entity manager = new Entity();
                    manager.add(new TaskComponent(task, manager));
                    manager.add(doorComponent);
                  });
        });
  }

  /**
   * Creates the Petri-Net for the given {@link TaskDependencyGraph}.
   *
   * <p>For each Task, a basic Petri net will be created, and then the Petri nets will be connected
   * based on the task dependency.
   *
   * @param taskGraph graph that defines the task dependencies
   * @return Mapping of {@link TaskNode} to {@link PetriNet}
   * @see PetriNetFactory
   */
  public static Map<TaskNode, PetriNet> petriNetFor(final TaskDependencyGraph taskGraph) {
    Map<TaskNode, PetriNet> noteToNet = new LinkedHashMap<>();

    // create a basic petri net for each task
    taskGraph
        .nodeIterator()
        .forEachRemaining(
            taskNode -> noteToNet.put(taskNode, PetriNetFactory.defaultNet(taskNode.task())));

    // connect petri nets
    taskGraph
        .edgeIterator()
        .forEachRemaining(
            taskEdge ->
                PetriNetFactory.connect(
                    noteToNet.get(taskEdge.endNode()),
                    noteToNet.get(taskEdge.startNode()),
                    taskEdge.edgeType()));

    // init token
    noteToNet.values().forEach(petriNet -> petriNet.taskNotActivated().placeToken());
    return noteToNet;
  }
}
