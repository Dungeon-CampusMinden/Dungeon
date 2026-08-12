package wizard.runner.room;

import escaperoom.foundation.room.model.ComponentPlacement;
import escaperoom.foundation.room.model.RiddlePlacement;
import escaperoom.foundation.room.model.RoomLayout;
import escaperoom.foundation.room.model.RoomPoint;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import wizard.runner.model.ProjectDefinition;
import wizard.runner.model.ProjectDefinition.GraphNode;
import wizard.runner.model.ProjectDefinition.GraphNodeKind;
import wizard.runner.model.ProjectDefinition.NumericInput;
import wizard.runner.model.ProjectDefinition.Riddle;

/** Deterministic internal planner for the bounded {@code foundation_single_room_v1} profile. */
final class SingleRoomPlanner {
  private static final int ROOM_INTERIOR_WIDTH = 15;
  private static final int ROW_WIDTH = 19;
  private static final int PLACEMENTS_PER_ROW = 8;
  private static final int DOOR_X = 16;
  private static final int EXIT_X = 17;
  private static final String WALL_ROW = "#".repeat(ROW_WIDTH);
  private static final String ROOM_ROW = "#" + ".".repeat(ROOM_INTERIOR_WIDTH) + "###";
  private static final String EXIT_ROW = "#" + ".".repeat(ROOM_INTERIOR_WIDTH) + "DE#";

  static Plan planRoom(final ProjectDefinition project) {
    Objects.requireNonNull(project, "project");
    List<String> riddleIds = stableRiddleOrder(project);
    return new Plan(riddleIds, layout(project, riddleIds));
  }

  private static List<String> stableRiddleOrder(final ProjectDefinition project) {
    Map<String, GraphNode> nodes = indexNodes(project);
    Map<String, Set<String>> outgoing = outgoing(project);
    Map<String, Integer> indegrees = new HashMap<>();
    Map<String, Integer> distances = new HashMap<>();
    nodes.keySet().forEach(id -> indegrees.put(id, 0));
    project.riddleGraph().edges().forEach(edge -> indegrees.merge(edge.to(), 1, Integer::sum));
    PriorityQueue<String> ready = new PriorityQueue<>(nodeOrder(nodes));
    indegrees.forEach(
        (id, count) -> {
          if (count == 0) {
            ready.add(id);
          }
        });
    distances.put(nodeId(nodes, GraphNodeKind.START), 0);
    while (!ready.isEmpty()) {
      String current = ready.remove();
      for (String successor : outgoing.getOrDefault(current, Set.of())) {
        distances.merge(successor, distances.get(current) + 1, Math::max);
        if (indegrees.merge(successor, -1, Integer::sum) == 0) {
          ready.add(successor);
        }
      }
    }
    return nodes.values().stream()
        .filter(node -> node.kind() == GraphNodeKind.RIDDLE)
        .sorted(
            Comparator.comparingInt((GraphNode node) -> distances.get(node.id()))
                .thenComparing(node -> node.riddleId().orElseThrow())
                .thenComparing(GraphNode::id))
        .map(node -> node.riddleId().orElseThrow())
        .toList();
  }

  private static String nodeId(
      final Map<String, GraphNode> nodes, final GraphNodeKind expectedKind) {
    return nodes.values().stream()
        .filter(node -> node.kind() == expectedKind)
        .map(GraphNode::id)
        .findFirst()
        .orElseThrow();
  }

  private static Map<String, GraphNode> indexNodes(final ProjectDefinition project) {
    Map<String, GraphNode> nodes = new TreeMap<>();
    project.riddleGraph().nodes().forEach(node -> nodes.put(node.id(), node));
    return nodes;
  }

  private static Map<String, Set<String>> outgoing(final ProjectDefinition project) {
    Map<String, Set<String>> outgoing = new TreeMap<>();
    project
        .riddleGraph()
        .edges()
        .forEach(
            edge ->
                outgoing.computeIfAbsent(edge.from(), ignored -> new TreeSet<>()).add(edge.to()));
    return outgoing;
  }

  private static Comparator<String> nodeOrder(final Map<String, GraphNode> nodes) {
    return Comparator.comparing((String id) -> nodes.get(id).riddleId().orElse(""))
        .thenComparing(Comparator.naturalOrder());
  }

  private static RoomLayout layout(final ProjectDefinition project, final List<String> riddleIds) {
    Map<String, Riddle> riddles = indexRiddles(project);
    int placementCount =
        riddleIds.stream()
            .map(riddles::get)
            .mapToInt(riddle -> componentSpecs(riddle).size())
            .sum();
    int placementRows = (placementCount + PLACEMENTS_PER_ROW - 1) / PLACEMENTS_PER_ROW;
    int doorY = 3 + Math.max(0, placementRows - 1) * 2 + 2;
    List<String> rows = new ArrayList<>();
    rows.add(WALL_ROW);
    for (int y = 1; y < doorY; y++) {
      rows.add(ROOM_ROW);
    }
    rows.add(EXIT_ROW);
    rows.add(WALL_ROW);

    boolean mirrored = (project.seed() & 1L) != 0;
    List<RiddlePlacement> placements = new ArrayList<>();
    int placementIndex = 0;
    for (String riddleId : riddleIds) {
      Riddle riddle = riddles.get(riddleId);
      List<ComponentPlacement> components = new ArrayList<>();
      for (ComponentSpec spec : componentSpecs(riddle)) {
        int column = placementIndex % PLACEMENTS_PER_ROW;
        int x = mirrored ? ROOM_INTERIOR_WIDTH - column * 2 : 1 + column * 2;
        int y = 3 + (placementIndex / PLACEMENTS_PER_ROW) * 2;
        components.add(
            new ComponentPlacement(spec.componentId(), spec.surfaceId(), new RoomPoint(x, y)));
        placementIndex++;
      }
      ComponentPlacement first = components.getFirst();
      placements.add(
          new RiddlePlacement(
              riddle.id(),
              components,
              riddle.hints().isEmpty()
                  ? Optional.empty()
                  : Optional.of(new RoomPoint(first.point().x(), first.point().y() + 1))));
    }
    return new RoomLayout(
        rows,
        new RoomPoint(1, 1),
        placements,
        new RoomPoint(DOOR_X, doorY),
        new RoomPoint(EXIT_X, doorY));
  }

  private static List<ComponentSpec> componentSpecs(final Riddle riddle) {
    List<ComponentSpec> components = new ArrayList<>();
    riddle
        .informationSources()
        .forEach(source -> components.add(new ComponentSpec(source.id(), source.surfaceId())));
    riddle.inputs().stream()
        .filter(NumericInput.class::isInstance)
        .map(NumericInput.class::cast)
        .forEach(input -> components.add(new ComponentSpec(input.id(), input.surfaceId())));
    return List.copyOf(components);
  }

  private static Map<String, Riddle> indexRiddles(final ProjectDefinition project) {
    Map<String, Riddle> riddles = new TreeMap<>();
    project.riddles().forEach(riddle -> riddles.put(riddle.id(), riddle));
    return riddles;
  }

  record Plan(List<String> riddleIds, RoomLayout layout) {}

  private record ComponentSpec(String componentId, String surfaceId) {}
}
