package wizard.runner.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import foundation.definition.HintSeverity;
import foundation.room.model.RiddlePlacement;
import foundation.room.model.RoomPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import wizard.runner.model.ProjectDefinition;
import wizard.runner.model.ProjectDefinition.Asset;
import wizard.runner.model.ProjectDefinition.GraphEdge;
import wizard.runner.model.ProjectDefinition.GraphNode;
import wizard.runner.model.ProjectDefinition.GraphNodeKind;
import wizard.runner.model.ProjectDefinition.Hint;
import wizard.runner.model.ProjectDefinition.LimitMode;
import wizard.runner.model.ProjectDefinition.Metadata;
import wizard.runner.model.ProjectDefinition.NumericInput;
import wizard.runner.model.ProjectDefinition.PlayerCount;
import wizard.runner.model.ProjectDefinition.Riddle;
import wizard.runner.model.ProjectDefinition.RiddleGraph;
import wizard.runner.model.ProjectDefinition.Scenario;
import wizard.runner.model.ProjectDefinition.Session;
import wizard.runner.model.ProjectDefinition.Surface;
import wizard.runner.model.ProjectDefinition.SurfaceKind;
import wizard.runner.model.ProjectDefinition.TimeLimit;
import wizard.runner.room.SingleRoomPlanner.Plan;

/** Deterministic planning proofs for the one supported Foundation room profile. */
final class SingleRoomPlannerTest {
  @Test
  void ordersCompleteAndGraphByLongestDistanceThenRiddleId() {
    ProjectDefinition project =
        project(
            "and_room", 3, List.of(List.of("r_zulu", "r_alpha"), List.of("r_charlie", "r_bravo")));

    Plan plan = plan(project, 0);

    assertEquals(List.of("r_alpha", "r_zulu", "r_bravo", "r_charlie"), plan.riddleIds());
    assertEquals(new RoomPoint(1, 1), plan.layout().startPoint());
    assertEquals(plan, plan(project, 0));
    assertEquals(plan, plan(reversedTopology(project), 0));
  }

  @Test
  void staggeredDagUsesLongestDistanceAndIgnoresInputArrayOrderForLayout() {
    ProjectDefinition base =
        project(
            "staggered_room",
            2,
            List.of(List.of("r_zulu", "r_alpha"), List.of("r_bravo", "r_charlie")));
    ProjectDefinition staggered =
        withEdges(
            base,
            List.of(
                new GraphEdge("n_start", nodeId("r_alpha")),
                new GraphEdge("n_start", nodeId("r_zulu")),
                new GraphEdge(nodeId("r_alpha"), nodeId("r_bravo")),
                new GraphEdge(nodeId("r_bravo"), nodeId("r_charlie")),
                new GraphEdge(nodeId("r_zulu"), nodeId("r_charlie")),
                new GraphEdge(nodeId("r_charlie"), "n_end")));

    Plan plan = SingleRoomPlanner.planRoom(staggered);
    Plan reordered = SingleRoomPlanner.planRoom(reversedTopology(staggered));

    assertEquals(List.of("r_alpha", "r_zulu", "r_bravo", "r_charlie"), plan.riddleIds());
    assertEquals(plan.riddleIds(), reordered.riddleIds());
    assertEquals(plan.layout(), reordered.layout());
  }

  @Test
  void honorsSeedAndUsesTheSharedStartForEverySupportedPlayerMaximum() {
    ProjectDefinition onePlayer = project("seed_room", 1, List.of(List.of("r_one", "r_two")));
    Plan left = plan(onePlayer, 0);
    Plan right = plan(onePlayer, 1);

    assertNotEquals(
        left.layout().riddlePlacements().getFirst().components().getFirst().point(),
        right.layout().riddlePlacements().getFirst().components().getFirst().point());
    assertEquals(left.layout().rows(), right.layout().rows());
    assertEquals(left.layout().startPoint(), right.layout().startPoint());
    for (int players = 1; players <= 4; players++) {
      assertEquals(
          new RoomPoint(1, 1),
          plan(project("room_" + players, players, List.of(List.of("r_only"))), 42)
              .layout()
              .startPoint());
    }
  }

  @Test
  void mapsSharedStartAndAuthoredHintsIntoTheRuntimeLayout() {
    ProjectDefinition project =
        withSeed(
            withHints(
                project("runtime_room", 4, List.of(List.of("r_zulu", "r_alpha"))),
                Set.of("r_alpha")),
            42L);

    Plan plan = SingleRoomPlanner.planRoom(project);

    assertEquals(new RoomPoint(1, 1), plan.layout().startPoint());
    assertEquals(Optional.of(new RoomPoint(1, 4)), placement(plan, "r_alpha").hintPoint());
    assertEquals(Optional.empty(), placement(plan, "r_zulu").hintPoint());
    assertEquals("surface_r_alpha", placement(plan, "r_alpha").components().getFirst().surfaceId());
  }

  private static Plan plan(final ProjectDefinition project, final long seed) {
    return SingleRoomPlanner.planRoom(withSeed(project, seed));
  }

  private static RiddlePlacement placement(final Plan plan, final String riddleId) {
    return plan.layout().riddlePlacements().stream()
        .filter(candidate -> candidate.riddleId().equals(riddleId))
        .findFirst()
        .orElseThrow();
  }

  private static ProjectDefinition project(
      final String roomId, final int maxPlayers, final List<List<String>> layerIds) {
    List<Riddle> riddles =
        layerIds.stream().flatMap(List::stream).map(SingleRoomPlannerTest::riddle).toList();
    List<GraphNode> nodes = new ArrayList<>();
    nodes.add(node("n_end", GraphNodeKind.END, Optional.empty()));
    for (List<String> layer : layerIds.reversed()) {
      for (String riddleId : layer.reversed()) {
        nodes.add(node(nodeId(riddleId), GraphNodeKind.RIDDLE, Optional.of(riddleId)));
      }
    }
    nodes.add(node("n_start", GraphNodeKind.START, Optional.empty()));
    List<List<String>> nodeLayers = new ArrayList<>();
    nodeLayers.add(List.of("n_start"));
    layerIds.forEach(
        layer -> nodeLayers.add(layer.stream().map(SingleRoomPlannerTest::nodeId).toList()));
    nodeLayers.add(List.of("n_end"));
    List<GraphEdge> edges = new ArrayList<>();
    for (int layer = 0; layer + 1 < nodeLayers.size(); layer++) {
      for (String from : nodeLayers.get(layer)) {
        for (String to : nodeLayers.get(layer + 1)) {
          edges.add(new GraphEdge(from, to));
        }
      }
    }
    return new ProjectDefinition(
        1L,
        new Metadata(roomId, "Room", "de-DE"),
        new Session(new PlayerCount(1, maxPlayers), new TimeLimit(30, LimitMode.HARD)),
        new Scenario(
            "default",
            "Mission",
            List.of("Introduction"),
            List.of("Success"),
            Optional.of(List.of("Failure"))),
        surfaces(riddles),
        new RiddleGraph(List.copyOf(nodes), List.copyOf(edges.reversed())),
        List.copyOf(riddles.reversed()),
        List.<Asset>of());
  }

  private static Riddle riddle(final String id) {
    return new Riddle(
        id,
        "Riddle " + id,
        List.of(),
        List.of(new NumericInput("input_" + id, "surface_" + id, "1", false)),
        List.of());
  }

  private static ProjectDefinition withHints(
      final ProjectDefinition source, final Set<String> riddleIds) {
    List<Riddle> riddles =
        source.riddles().stream()
            .map(
                riddle ->
                    new Riddle(
                        riddle.id(),
                        riddle.title(),
                        riddle.informationSources(),
                        riddle.inputs(),
                        riddleIds.contains(riddle.id())
                            ? List.of(
                                new Hint(
                                    "hint_" + riddle.id(),
                                    "Hint",
                                    "Try this.",
                                    HintSeverity.ORIENTATION))
                            : List.of()))
            .toList();
    return new ProjectDefinition(
        source.seed(),
        source.metadata(),
        source.session(),
        source.scenario(),
        source.surfaces(),
        source.riddleGraph(),
        riddles,
        source.assets());
  }

  private static ProjectDefinition withSeed(final ProjectDefinition source, final long seed) {
    return new ProjectDefinition(
        seed,
        source.metadata(),
        source.session(),
        source.scenario(),
        source.surfaces(),
        source.riddleGraph(),
        source.riddles(),
        source.assets());
  }

  private static ProjectDefinition withEdges(
      final ProjectDefinition source, final List<GraphEdge> edges) {
    return new ProjectDefinition(
        source.seed(),
        source.metadata(),
        source.session(),
        source.scenario(),
        source.surfaces(),
        new RiddleGraph(source.riddleGraph().nodes(), edges),
        source.riddles(),
        source.assets());
  }

  private static ProjectDefinition reversedTopology(final ProjectDefinition source) {
    return new ProjectDefinition(
        source.seed(),
        source.metadata(),
        source.session(),
        source.scenario(),
        source.surfaces(),
        new RiddleGraph(
            source.riddleGraph().nodes().reversed(), source.riddleGraph().edges().reversed()),
        source.riddles().reversed(),
        source.assets());
  }

  private static GraphNode node(
      final String id, final GraphNodeKind kind, final Optional<String> riddleId) {
    return new GraphNode(
        id, kind, riddleId, kind == GraphNodeKind.END ? Optional.of("door") : Optional.empty());
  }

  private static List<Surface> surfaces(final List<Riddle> riddles) {
    List<Surface> surfaces = new ArrayList<>();
    surfaces.add(new Surface("world", SurfaceKind.WORLD, "World"));
    riddles.forEach(
        riddle ->
            surfaces.add(
                new Surface("surface_" + riddle.id(), SurfaceKind.KEYPAD, riddle.title())));
    surfaces.add(new Surface("door", SurfaceKind.DOOR, "Door"));
    return List.copyOf(surfaces);
  }

  private static String nodeId(final String riddleId) {
    return "node_" + riddleId;
  }
}
