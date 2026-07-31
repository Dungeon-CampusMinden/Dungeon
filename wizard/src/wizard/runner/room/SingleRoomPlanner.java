package wizard.runner.room;

import foundation.room.model.ComponentPlacement;
import foundation.room.model.RiddlePlacement;
import foundation.room.model.RoomLayout;
import foundation.room.model.RoomPoint;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import wizard.runner.canonical.CanonicalJson;
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
    List<Section> sections = reconstructSections(project);
    return new Plan(sections, layout(project, sections));
  }

  private static List<Section> reconstructSections(final ProjectDefinition project) {
    Map<String, GraphNode> nodes = indexNodes(project);
    Map<String, Set<String>> outgoing = outgoing(project);
    String endNodeId = nodeId(nodes, GraphNodeKind.END);
    List<String> current = List.of(nodeId(nodes, GraphNodeKind.START));
    List<Section> sections = new ArrayList<>();
    while (!current.equals(List.of(endNodeId))) {
      Set<String> nextIds = new TreeSet<>();
      current.forEach(nodeId -> nextIds.addAll(outgoing.getOrDefault(nodeId, Set.of())));
      if (nextIds.isEmpty()) {
        throw new IllegalStateException("validated graph has no complete path to its end");
      }
      List<String> next = canonicalNodeOrder(nextIds, nodes);
      if (!next.equals(List.of(endNodeId))) {
        List<String> riddleIds =
            next.stream()
                .map(nodeId -> nodes.get(nodeId).riddleId().orElseThrow())
                .sorted()
                .toList();
        sections.add(new Section(sectionId(riddleIds), riddleIds));
      }
      current = next;
    }
    return List.copyOf(sections);
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

  private static List<String> canonicalNodeOrder(
      final Set<String> nodeIds, final Map<String, GraphNode> nodes) {
    return nodeIds.stream()
        .sorted(
            Comparator.comparing((String id) -> nodes.get(id).riddleId().orElse("\uffff"))
                .thenComparing(Comparator.naturalOrder()))
        .toList();
  }

  private static String sectionId(final List<String> riddleIds) {
    return "section_" + sha256(CanonicalJson.encode(riddleIds)).substring(0, 56);
  }

  private static RoomLayout layout(final ProjectDefinition project, final List<Section> sections) {
    List<String> riddleIds =
        sections.stream().flatMap(section -> section.riddleIds().stream()).toList();
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

  private static String sha256(final String source) {
    return HexFormat.of().formatHex(digest(source.getBytes(StandardCharsets.UTF_8)));
  }

  private static byte[] digest(final byte[] source) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(source);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  record Plan(List<Section> sections, RoomLayout layout) {}

  record Section(String id, List<String> riddleIds) {}

  private record ComponentSpec(String componentId, String surfaceId) {}
}
