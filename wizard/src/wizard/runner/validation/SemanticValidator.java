package wizard.runner.validation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import wizard.runner.contract.ContractCapabilities;
import wizard.runner.contract.IssueCode;
import wizard.runner.contract.IssueCollector;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationIssue.Entity;
import wizard.runner.contract.ValidationPhase;
import wizard.runner.contract.ValidationSeverity;
import wizard.runner.model.ProjectDefinition;
import wizard.runner.model.ProjectDefinition.Asset;
import wizard.runner.model.ProjectDefinition.CollectionInput;
import wizard.runner.model.ProjectDefinition.GraphEdge;
import wizard.runner.model.ProjectDefinition.GraphNode;
import wizard.runner.model.ProjectDefinition.GraphNodeKind;
import wizard.runner.model.ProjectDefinition.InformationSource;
import wizard.runner.model.ProjectDefinition.Input;
import wizard.runner.model.ProjectDefinition.NumericInput;
import wizard.runner.model.ProjectDefinition.Resource;
import wizard.runner.model.ProjectDefinition.ResourceKind;
import wizard.runner.model.ProjectDefinition.Riddle;
import wizard.runner.model.ProjectDefinition.Surface;
import wizard.runner.model.ProjectDefinition.SurfaceKind;

/** Deterministic cross-reference, graph, capability, asset, and feasibility validation. */
final class SemanticValidator {
  void validate(final ProjectDefinition project, final IssueCollector issues) {
    validateCapacities(project, issues);
    validateIdentifiers(project, issues);
    validateGraph(project, issues);
    validateSurfaces(project, issues);
    validatePlayerCount(project, issues);
    validateAssetSemantics(project, issues);
    validateWarnings(project, issues);
  }

  private void validateCapacities(final ProjectDefinition project, final IssueCollector issues) {
    capacity(
        "riddles", project.riddles().size(), ContractCapabilities.MAX_RIDDLES, "/riddles", issues);
    int resources =
        project.riddles().stream()
            .flatMap(riddle -> riddle.informationSources().stream())
            .mapToInt(source -> source.resources().size())
            .sum();
    capacity("resources", resources, ContractCapabilities.MAX_RESOURCES, "/riddles", issues);
    int hints = project.riddles().stream().mapToInt(riddle -> riddle.hints().size()).sum();
    capacity("hints", hints, ContractCapabilities.MAX_HINTS, "/riddles", issues);
  }

  private void capacity(
      final String kind,
      final int actual,
      final int maximum,
      final String path,
      final IssueCollector issues) {
    if (actual > maximum) {
      issues.add(
          issue(
              ValidationSeverity.ERROR,
              ValidationPhase.FEASIBILITY,
              IssueCode.RUNNER_CAPACITY_EXCEEDED,
              "validation.feasibility.capacity_exceeded",
              Map.of("actual", actual, "kind", kind, "limit", maximum),
              path));
    }
  }

  private static void validateIdentifiers(
      final ProjectDefinition project, final IssueCollector issues) {
    Map<String, LocatedId> seen = new LinkedHashMap<>();
    for (int index = 0; index < project.surfaces().size(); index++) {
      addId(project.surfaces().get(index).id(), "surface", "/surfaces/" + index, seen, issues);
    }
    for (int index = 0; index < project.riddleGraph().nodes().size(); index++) {
      addId(
          project.riddleGraph().nodes().get(index).id(),
          "graph_node",
          "/riddleGraph/nodes/" + index,
          seen,
          issues);
    }
    for (int riddleIndex = 0; riddleIndex < project.riddles().size(); riddleIndex++) {
      Riddle riddle = project.riddles().get(riddleIndex);
      String riddlePath = "/riddles/" + riddleIndex;
      addId(riddle.id(), "riddle", riddlePath, seen, issues);
      for (int sourceIndex = 0; sourceIndex < riddle.informationSources().size(); sourceIndex++) {
        InformationSource source = riddle.informationSources().get(sourceIndex);
        String sourcePath = riddlePath + "/informationSources/" + sourceIndex;
        addId(source.id(), "information_source", sourcePath, seen, issues);
        for (int resourceIndex = 0; resourceIndex < source.resources().size(); resourceIndex++) {
          addId(
              source.resources().get(resourceIndex).id(),
              "resource",
              sourcePath + "/resources/" + resourceIndex,
              seen,
              issues);
        }
      }
      for (int inputIndex = 0; inputIndex < riddle.inputs().size(); inputIndex++) {
        addId(
            riddle.inputs().get(inputIndex).id(),
            "input",
            riddlePath + "/inputs/" + inputIndex,
            seen,
            issues);
      }
      for (int hintIndex = 0; hintIndex < riddle.hints().size(); hintIndex++) {
        addId(
            riddle.hints().get(hintIndex).id(),
            "hint",
            riddlePath + "/hints/" + hintIndex,
            seen,
            issues);
      }
    }
    for (int index = 0; index < project.assets().size(); index++) {
      addId(project.assets().get(index).id(), "asset", "/assets/" + index, seen, issues);
    }
  }

  private static void addId(
      final String id,
      final String kind,
      final String path,
      final Map<String, LocatedId> seen,
      final IssueCollector issues) {
    LocatedId previous = seen.putIfAbsent(id, new LocatedId(kind, path));
    if (previous != null) {
      issues.add(
          new ValidationIssue(
              ValidationSeverity.ERROR,
              ValidationPhase.REFERENCES,
              IssueCode.ID_DUPLICATE,
              "validation.references.id_duplicate",
              Map.of("id", id),
              path,
              Optional.of(new Entity(kind, id)),
              List.of(previous.path())));
    }
  }

  private static void validateGraph(final ProjectDefinition project, final IssueCollector issues) {
    List<GraphNode> nodes = project.riddleGraph().nodes();
    List<GraphEdge> edges = project.riddleGraph().edges();
    Map<String, GraphNode> nodeById = uniqueById(nodes, GraphNode::id);
    Map<String, Riddle> riddleById = uniqueById(project.riddles(), Riddle::id);
    GraphNode start =
        nodes.stream().filter(node -> node.kind() == GraphNodeKind.START).findFirst().orElseThrow();
    GraphNode end =
        nodes.stream().filter(node -> node.kind() == GraphNodeKind.END).findFirst().orElseThrow();

    Map<String, List<String>> outgoing = new HashMap<>();
    Map<String, List<String>> incoming = new HashMap<>();
    Set<String> edgePairs = new HashSet<>();
    boolean edgeInvalid = false;
    for (int index = 0; index < edges.size(); index++) {
      GraphEdge edge = edges.get(index);
      String path = "/riddleGraph/edges/" + index;
      if (!nodeById.containsKey(edge.from()) || !nodeById.containsKey(edge.to())) {
        boolean unknownFrom = !nodeById.containsKey(edge.from());
        String unknown = unknownFrom ? edge.from() : edge.to();
        issues.add(
            issue(
                ValidationSeverity.ERROR,
                ValidationPhase.REFERENCES,
                IssueCode.REFERENCE_UNKNOWN,
                "validation.references.graph_node_unknown",
                Map.of("id", unknown),
                path + (unknownFrom ? "/from" : "/to")));
        edgeInvalid = true;
        continue;
      }
      String pair = edge.from() + "\u0000" + edge.to();
      if (edge.from().equals(edge.to()) || !edgePairs.add(pair)) {
        issues.add(
            issue(
                ValidationSeverity.ERROR,
                ValidationPhase.GRAPH,
                IssueCode.GRAPH_EDGE_INVALID,
                "validation.graph.edge_invalid",
                Map.of("reason", edge.from().equals(edge.to()) ? "self" : "duplicate"),
                path + "/to"));
        edgeInvalid = true;
      }
      outgoing.computeIfAbsent(edge.from(), ignored -> new ArrayList<>()).add(edge.to());
      incoming.computeIfAbsent(edge.to(), ignored -> new ArrayList<>()).add(edge.from());
    }
    if (start != null && !incoming.getOrDefault(start.id(), List.of()).isEmpty()) {
      graphProfile("start_has_incoming", "/riddleGraph/nodes", issues);
    }
    if (end != null && !outgoing.getOrDefault(end.id(), List.of()).isEmpty()) {
      graphProfile("end_has_outgoing", "/riddleGraph/nodes", issues);
    }

    Map<String, Integer> riddleBindings = new HashMap<>();
    for (int index = 0; index < nodes.size(); index++) {
      GraphNode node = nodes.get(index);
      if (node.kind() == GraphNodeKind.RIDDLE) {
        String riddleId = node.riddleId().orElseThrow();
        if (!riddleById.containsKey(riddleId)) {
          issues.add(
              entityIssue(
                  ValidationSeverity.ERROR,
                  ValidationPhase.REFERENCES,
                  IssueCode.REFERENCE_UNKNOWN,
                  "validation.references.riddle_unknown",
                  Map.of("id", riddleId),
                  "/riddleGraph/nodes/" + index + "/riddleId",
                  "graph_node",
                  node.id()));
        }
        riddleBindings.merge(riddleId, 1, Integer::sum);
      }
    }
    for (int index = 0; index < project.riddles().size(); index++) {
      Riddle riddle = project.riddles().get(index);
      if (riddleBindings.getOrDefault(riddle.id(), 0) != 1) {
        issues.add(
            entityIssue(
                ValidationSeverity.ERROR,
                ValidationPhase.GRAPH,
                IssueCode.GRAPH_RIDDLE_UNREACHABLE,
                "validation.graph.riddle_binding_invalid",
                Map.of("count", riddleBindings.getOrDefault(riddle.id(), 0)),
                "/riddles/" + index,
                "riddle",
                riddle.id()));
      }
    }

    if (edgeInvalid) {
      return;
    }
    Set<String> reachable = traverse(start.id(), outgoing);
    Set<String> toEnd = traverse(end.id(), incoming);
    for (int index = 0; index < nodes.size(); index++) {
      GraphNode node = nodes.get(index);
      if (!reachable.contains(node.id())) {
        graphNodeIssue(IssueCode.GRAPH_NODE_UNREACHABLE, node, index, issues);
      }
      if (!toEnd.contains(node.id())) {
        graphNodeIssue(IssueCode.GRAPH_NODE_NO_PATH_TO_END, node, index, issues);
      }
    }
    if (hasCycle(nodes, outgoing)) {
      issues.add(
          issue(
              ValidationSeverity.ERROR,
              ValidationPhase.GRAPH,
              IssueCode.GRAPH_CYCLE,
              "validation.graph.cycle",
              Map.of(),
              "/riddleGraph"));
      return;
    }
    if (reachable.size() == nodes.size() && toEnd.size() == nodes.size()) {
      validateAndProfile(start.id(), end.id(), nodes, edges, incoming, issues);
    }
  }

  private static void validateSurfaces(
      final ProjectDefinition project, final IssueCollector issues) {
    Map<String, Surface> surfaces = uniqueById(project.surfaces(), Surface::id);
    Map<String, Integer> owned = new HashMap<>();
    for (int index = 0; index < project.riddles().size(); index++) {
      Riddle riddle = project.riddles().get(index);
      String path = "/riddles/" + index;
      Map<String, InformationSource> sources =
          uniqueById(riddle.informationSources(), InformationSource::id);
      for (int sourceIndex = 0; sourceIndex < riddle.informationSources().size(); sourceIndex++) {
        InformationSource source = riddle.informationSources().get(sourceIndex);
        String sourcePath = path + "/informationSources/" + sourceIndex;
        Surface surface =
            validateSurface(
                riddle, source.surfaceId(), SurfaceKind.CONTAINER, sourcePath, surfaces, issues);
        if (surface != null && surface.kind() == SurfaceKind.CONTAINER) {
          owned.merge(surface.id(), 1, Integer::sum);
        }
      }
      for (int inputIndex = 0; inputIndex < riddle.inputs().size(); inputIndex++) {
        Input input = riddle.inputs().get(inputIndex);
        String inputPath = path + "/inputs/" + inputIndex;
        if (input instanceof CollectionInput collection) {
          if (!sources.containsKey(collection.informationSourceId())) {
            issues.add(
                entityIssue(
                    ValidationSeverity.ERROR,
                    ValidationPhase.REFERENCES,
                    IssueCode.REFERENCE_UNKNOWN,
                    "validation.references.information_source_unknown",
                    Map.of("id", collection.informationSourceId()),
                    inputPath + "/informationSourceId",
                    "input",
                    input.id()));
          }
        } else if (input instanceof NumericInput numeric) {
          Surface surface =
              validateSurface(
                  riddle, numeric.surfaceId(), SurfaceKind.KEYPAD, inputPath, surfaces, issues);
          if (surface != null && surface.kind() == SurfaceKind.KEYPAD) {
            owned.merge(surface.id(), 1, Integer::sum);
          }
        }
      }
    }

    long worlds =
        project.surfaces().stream().filter(surface -> surface.kind() == SurfaceKind.WORLD).count();
    long doors =
        project.surfaces().stream().filter(surface -> surface.kind() == SurfaceKind.DOOR).count();
    if (worlds != 1 || doors != 1) {
      issues.add(
          issue(
              ValidationSeverity.ERROR,
              ValidationPhase.CAPABILITY,
              IssueCode.SURFACE_CARDINALITY_INVALID,
              "validation.capability.surface_cardinality_invalid",
              Map.of("doors", doors, "worlds", worlds),
              "/surfaces"));
    }
    for (int index = 0; index < project.surfaces().size(); index++) {
      Surface surface = project.surfaces().get(index);
      if ((surface.kind() == SurfaceKind.CONTAINER || surface.kind() == SurfaceKind.KEYPAD)
          && owned.getOrDefault(surface.id(), 0) != 1) {
        issues.add(
            entityIssue(
                ValidationSeverity.ERROR,
                ValidationPhase.CAPABILITY,
                IssueCode.SURFACE_OWNERSHIP_INVALID,
                "validation.capability.surface_ownership_invalid",
                Map.of("count", owned.getOrDefault(surface.id(), 0)),
                "/surfaces/" + index,
                "surface",
                surface.id()));
      }
    }
    validateEndSurface(project, surfaces, issues);
  }

  private static Surface validateSurface(
      final Riddle riddle,
      final String surfaceId,
      final SurfaceKind expected,
      final String path,
      final Map<String, Surface> surfaces,
      final IssueCollector issues) {
    Surface surface = surfaces.get(surfaceId);
    if (surface == null) {
      issues.add(
          entityIssue(
              ValidationSeverity.ERROR,
              ValidationPhase.REFERENCES,
              IssueCode.REFERENCE_UNKNOWN,
              "validation.references.surface_unknown",
              Map.of("id", surfaceId),
              path + "/surfaceId",
              "riddle",
              riddle.id()));
    } else if (surface.kind() != expected) {
      issues.add(
          entityIssue(
              ValidationSeverity.ERROR,
              ValidationPhase.CAPABILITY,
              IssueCode.SURFACE_INCOMPATIBLE,
              "validation.capability.surface_incompatible",
              Map.of("actual", token(surface.kind()), "expected", token(expected)),
              path + "/surfaceId",
              "riddle",
              riddle.id()));
    }
    return surface;
  }

  private static void validateEndSurface(
      final ProjectDefinition project,
      final Map<String, Surface> surfaces,
      final IssueCollector issues) {
    int endIndex = -1;
    for (int index = 0; index < project.riddleGraph().nodes().size(); index++) {
      if (project.riddleGraph().nodes().get(index).kind() == GraphNodeKind.END) {
        endIndex = index;
        break;
      }
    }
    if (endIndex < 0) {
      return;
    }
    GraphNode end = project.riddleGraph().nodes().get(endIndex);
    String surfaceId = end.surfaceId().orElseThrow();
    Surface surface = surfaces.get(surfaceId);
    if (surface == null) {
      issues.add(
          entityIssue(
              ValidationSeverity.ERROR,
              ValidationPhase.REFERENCES,
              IssueCode.REFERENCE_UNKNOWN,
              "validation.references.surface_unknown",
              Map.of("id", surfaceId),
              "/riddleGraph/nodes/" + endIndex + "/surfaceId",
              "graph_node",
              end.id()));
    } else if (surface.kind() != SurfaceKind.DOOR) {
      issues.add(
          entityIssue(
              ValidationSeverity.ERROR,
              ValidationPhase.CAPABILITY,
              IssueCode.SURFACE_INCOMPATIBLE,
              "validation.capability.exit_surface_incompatible",
              Map.of("actual", token(surface.kind()), "expected", "door"),
              "/riddleGraph/nodes/" + endIndex + "/surfaceId",
              "graph_node",
              end.id()));
    }
  }

  private static void validateAndProfile(
      final String startId,
      final String endId,
      final List<GraphNode> nodes,
      final List<GraphEdge> edges,
      final Map<String, List<String>> incoming,
      final IssueCollector issues) {
    Map<String, Integer> depths = new HashMap<>();
    depths.put(startId, 0);
    boolean changed = true;
    while (changed) {
      changed = false;
      for (GraphNode node : nodes) {
        if (depths.containsKey(node.id())) {
          continue;
        }
        List<String> predecessors = incoming.getOrDefault(node.id(), List.of());
        if (!predecessors.isEmpty() && predecessors.stream().allMatch(depths::containsKey)) {
          int depth = predecessors.stream().mapToInt(depths::get).max().orElseThrow() + 1;
          depths.put(node.id(), depth);
          changed = true;
        }
      }
    }
    if (depths.size() != nodes.size()) {
      graphProfile("not_layered", "/riddleGraph", issues);
      return;
    }
    int endDepth = depths.get(endId);
    Map<Integer, Set<String>> layers = new HashMap<>();
    depths.forEach(
        (id, depth) -> layers.computeIfAbsent(depth, ignored -> new LinkedHashSet<>()).add(id));
    if (layers.size() != endDepth + 1
        || !layers.getOrDefault(0, Set.of()).equals(Set.of(startId))
        || !layers.getOrDefault(endDepth, Set.of()).equals(Set.of(endId))) {
      graphProfile("boundary_layers", "/riddleGraph", issues);
      return;
    }
    Set<String> actual =
        edges.stream().map(edge -> edge.from() + "\u0000" + edge.to()).collect(Collectors.toSet());
    Set<String> expected = new HashSet<>();
    for (int depth = 0; depth < endDepth; depth++) {
      for (String from : layers.getOrDefault(depth, Set.of())) {
        for (String to : layers.getOrDefault(depth + 1, Set.of())) {
          expected.add(from + "\u0000" + to);
        }
      }
    }
    if (!actual.equals(expected)) {
      graphProfile("not_complete_ordered_sections", "/riddleGraph/edges", issues);
    }
  }

  private static Set<String> traverse(
      final String first, final Map<String, List<String>> adjacency) {
    Set<String> result = new HashSet<>();
    ArrayDeque<String> pending = new ArrayDeque<>();
    pending.add(first);
    while (!pending.isEmpty()) {
      String next = pending.removeFirst();
      if (result.add(next)) {
        pending.addAll(adjacency.getOrDefault(next, List.of()));
      }
    }
    return result;
  }

  private static boolean hasCycle(
      final List<GraphNode> nodes, final Map<String, List<String>> outgoing) {
    Map<String, Integer> indegree = new HashMap<>();
    nodes.forEach(node -> indegree.put(node.id(), 0));
    outgoing
        .values()
        .forEach(targets -> targets.forEach(target -> indegree.merge(target, 1, Integer::sum)));
    ArrayDeque<String> ready = new ArrayDeque<>();
    indegree.forEach(
        (id, count) -> {
          if (count == 0) ready.add(id);
        });
    int visited = 0;
    while (!ready.isEmpty()) {
      String current = ready.removeFirst();
      visited++;
      for (String target : outgoing.getOrDefault(current, List.of())) {
        if (indegree.merge(target, -1, Integer::sum) == 0) {
          ready.add(target);
        }
      }
    }
    return visited != nodes.size();
  }

  private static void validatePlayerCount(
      final ProjectDefinition project, final IssueCollector issues) {
    if (project.session().playerCount().min() > project.session().playerCount().max()) {
      issues.add(
          issue(
              ValidationSeverity.ERROR,
              ValidationPhase.CAPABILITY,
              IssueCode.PLAYER_COUNT_INVALID,
              "validation.capability.player_count_invalid",
              Map.of(
                  "max", project.session().playerCount().max(),
                  "min", project.session().playerCount().min()),
              "/session/playerCount"));
    }
  }

  private static void validateAssetSemantics(
      final ProjectDefinition project, final IssueCollector issues) {
    Map<String, Asset> assets = uniqueById(project.assets(), Asset::id);
    Map<String, Integer> uses = new HashMap<>();
    for (int riddleIndex = 0; riddleIndex < project.riddles().size(); riddleIndex++) {
      Riddle riddle = project.riddles().get(riddleIndex);
      for (int sourceIndex = 0; sourceIndex < riddle.informationSources().size(); sourceIndex++) {
        InformationSource source = riddle.informationSources().get(sourceIndex);
        for (int resourceIndex = 0; resourceIndex < source.resources().size(); resourceIndex++) {
          Resource resource = source.resources().get(resourceIndex);
          if (resource.kind() != ResourceKind.ASSET) {
            continue;
          }
          String assetId = resource.assetId().orElseThrow();
          Asset asset = assets.get(assetId);
          if (asset == null) {
            issues.add(
                entityIssue(
                    ValidationSeverity.ERROR,
                    ValidationPhase.REFERENCES,
                    IssueCode.REFERENCE_UNKNOWN,
                    "validation.references.asset_unknown",
                    Map.of("id", assetId),
                    "/riddles/"
                        + riddleIndex
                        + "/informationSources/"
                        + sourceIndex
                        + "/resources/"
                        + resourceIndex
                        + "/assetId",
                    "resource",
                    resource.id()));
          } else {
            uses.merge(assetId, 1, Integer::sum);
          }
        }
      }
    }
    for (int index = 0; index < project.assets().size(); index++) {
      Asset asset = project.assets().get(index);
      if (uses.getOrDefault(asset.id(), 0) == 0) {
        issues.add(
            entityIssue(
                ValidationSeverity.WARNING,
                ValidationPhase.ASSETS,
                IssueCode.ASSET_DECLARED_UNUSED,
                "validation.assets.declared_unused",
                Map.of("id", asset.id()),
                "/assets/" + index,
                "asset",
                asset.id()));
      }
    }
  }

  private void validateWarnings(final ProjectDefinition project, final IssueCollector issues) {
    int playerTextLimit = ContractCapabilities.MAX_PLAYER_FACING_TEXT_WARNING_CODE_POINTS;
    warnLongText(
        project.metadata().title(), playerTextLimit, "/metadata/title", "player_facing", issues);
    warnLongText(
        project.scenario().mission(),
        playerTextLimit,
        "/scenario/mission",
        "player_facing",
        issues);
    warnLongTexts(
        project.scenario().introText(),
        playerTextLimit,
        "/scenario/introText",
        "player_facing",
        issues);
    warnLongTexts(
        project.scenario().successText(),
        playerTextLimit,
        "/scenario/successText",
        "player_facing",
        issues);
    project
        .scenario()
        .failureText()
        .ifPresent(
            texts ->
                warnLongTexts(
                    texts, playerTextLimit, "/scenario/failureText", "player_facing", issues));
    for (int index = 0; index < project.riddles().size(); index++) {
      Riddle riddle = project.riddles().get(index);
      for (int sourceIndex = 0; sourceIndex < riddle.informationSources().size(); sourceIndex++) {
        InformationSource source = riddle.informationSources().get(sourceIndex);
        for (int resourceIndex = 0; resourceIndex < source.resources().size(); resourceIndex++) {
          Resource resource = source.resources().get(resourceIndex);
          if (resource.text().isPresent()) {
            warnLongText(
                resource.text().orElseThrow(),
                playerTextLimit,
                "/riddles/"
                    + index
                    + "/informationSources/"
                    + sourceIndex
                    + "/resources/"
                    + resourceIndex
                    + "/text",
                "player_facing",
                issues);
          }
        }
      }
      for (int hintIndex = 0; hintIndex < riddle.hints().size(); hintIndex++) {
        warnLongText(
            riddle.hints().get(hintIndex).text(),
            ContractCapabilities.MAX_HINT_TEXT_WARNING_CODE_POINTS,
            "/riddles/" + index + "/hints/" + hintIndex + "/text",
            "hint",
            issues);
      }
    }
  }

  private static void warnLongText(
      final String text,
      final int limit,
      final String path,
      final String kind,
      final IssueCollector issues) {
    int actual = text.codePointCount(0, text.length());
    if (actual > limit) {
      issues.add(
          issue(
              ValidationSeverity.WARNING,
              ValidationPhase.FEASIBILITY,
              IssueCode.TEXT_LONG,
              "validation.feasibility.text_long",
              Map.of("actual", actual, "kind", kind, "limit", limit),
              path));
    }
  }

  private static void warnLongTexts(
      final List<String> texts,
      final int limit,
      final String path,
      final String kind,
      final IssueCollector issues) {
    for (int index = 0; index < texts.size(); index++) {
      warnLongText(texts.get(index), limit, path + "/" + index, kind, issues);
    }
  }

  private static void graphNodeIssue(
      final IssueCode code, final GraphNode node, final int index, final IssueCollector issues) {
    issues.add(
        entityIssue(
            ValidationSeverity.ERROR,
            ValidationPhase.GRAPH,
            code,
            code == IssueCode.GRAPH_NODE_UNREACHABLE
                ? "validation.graph.node_unreachable"
                : "validation.graph.node_no_path_to_end",
            Map.of(),
            "/riddleGraph/nodes/" + index,
            "graph_node",
            node.id()));
  }

  private static void graphProfile(
      final String reason, final String path, final IssueCollector issues) {
    issues.add(
        issue(
            ValidationSeverity.ERROR,
            ValidationPhase.GRAPH,
            IssueCode.GRAPH_PROFILE_INVALID,
            "validation.graph.profile_invalid",
            Map.of("reason", reason),
            path));
  }

  private static <T> Map<String, T> uniqueById(final List<T> values, final Function<T, String> id) {
    Map<String, T> result = new LinkedHashMap<>();
    values.forEach(value -> result.putIfAbsent(id.apply(value), value));
    return result;
  }

  private static String token(final Enum<?> value) {
    return value.name().toLowerCase(Locale.ROOT);
  }

  private static ValidationIssue entityIssue(
      final ValidationSeverity severity,
      final ValidationPhase phase,
      final IssueCode code,
      final String message,
      final Map<String, Object> arguments,
      final String path,
      final String kind,
      final String id) {
    return new ValidationIssue(
        severity,
        phase,
        code,
        message,
        arguments,
        path,
        Optional.of(new Entity(kind, id)),
        List.of());
  }

  private static ValidationIssue issue(
      final ValidationSeverity severity,
      final ValidationPhase phase,
      final IssueCode code,
      final String message,
      final Map<String, Object> arguments,
      final String path) {
    return new ValidationIssue(
        severity, phase, code, message, arguments, path, Optional.empty(), List.of());
  }

  private record LocatedId(String kind, String path) {}
}
