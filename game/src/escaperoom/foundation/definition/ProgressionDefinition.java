package escaperoom.foundation.definition;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable mandatory AND-DAG progression for one Foundation room.
 *
 * @param startNodeId stable start-node identifier
 * @param endNodeId stable end-node identifier
 * @param riddleNodes stable riddle nodes
 * @param edges exact mandatory dependency edges
 */
public record ProgressionDefinition(
    String startNodeId, String endNodeId, List<RiddleNode> riddleNodes, List<Edge> edges) {
  /** Creates a closed progression and validates all runtime invariants. */
  public ProgressionDefinition {
    startNodeId = DefinitionChecks.requireId(startNodeId, "progression start node id");
    endNodeId = DefinitionChecks.requireId(endNodeId, "progression end node id");
    if (startNodeId.equals(endNodeId)) {
      throw new IllegalArgumentException("progression start and end must differ");
    }
    riddleNodes = List.copyOf(Objects.requireNonNull(riddleNodes, "riddleNodes"));
    edges = List.copyOf(Objects.requireNonNull(edges, "edges"));
    if (riddleNodes.isEmpty()) {
      throw new IllegalArgumentException("progression must contain at least one riddle");
    }

    Set<String> nodeIds = new LinkedHashSet<>();
    nodeIds.add(startNodeId);
    nodeIds.add(endNodeId);
    Set<String> riddleIds = new LinkedHashSet<>();
    for (RiddleNode node : riddleNodes) {
      if (!nodeIds.add(node.id())) {
        throw new IllegalArgumentException("duplicate progression node id: " + node.id());
      }
      if (!riddleIds.add(node.riddle().id())) {
        throw new IllegalArgumentException(
            "duplicate progression riddle id: " + node.riddle().id());
      }
    }

    Map<String, Set<String>> outgoing = adjacency(nodeIds);
    Map<String, Set<String>> incoming = adjacency(nodeIds);
    Set<Edge> uniqueEdges = new HashSet<>();
    for (Edge edge : edges) {
      if (!nodeIds.contains(edge.from()) || !nodeIds.contains(edge.to())) {
        throw new IllegalArgumentException("progression edge references an unknown node");
      }
      if (edge.from().equals(edge.to())) {
        throw new IllegalArgumentException("progression self edges are not allowed");
      }
      if (!uniqueEdges.add(edge)) {
        throw new IllegalArgumentException(
            "duplicate progression edge: " + edge.from() + " -> " + edge.to());
      }
      outgoing.get(edge.from()).add(edge.to());
      incoming.get(edge.to()).add(edge.from());
    }
    if (!incoming.get(startNodeId).isEmpty()) {
      throw new IllegalArgumentException("progression start must not have incoming edges");
    }
    if (!outgoing.get(endNodeId).isEmpty()) {
      throw new IllegalArgumentException("progression end must not have outgoing edges");
    }
    if (!reachable(startNodeId, outgoing).equals(nodeIds)) {
      throw new IllegalArgumentException("every progression node must be reachable from start");
    }
    if (!reachable(endNodeId, incoming).equals(nodeIds)) {
      throw new IllegalArgumentException("every progression node must have a path to end");
    }
    validateAcyclic(nodeIds, outgoing, incoming);
  }

  private static Map<String, Set<String>> adjacency(final Set<String> nodeIds) {
    Map<String, Set<String>> result = new LinkedHashMap<>();
    nodeIds.forEach(id -> result.put(id, new LinkedHashSet<>()));
    return result;
  }

  private static Set<String> reachable(
      final String origin, final Map<String, Set<String>> adjacency) {
    Set<String> visited = new LinkedHashSet<>();
    ArrayDeque<String> pending = new ArrayDeque<>();
    pending.add(origin);
    while (!pending.isEmpty()) {
      String current = pending.removeFirst();
      if (visited.add(current)) {
        pending.addAll(adjacency.get(current));
      }
    }
    return visited;
  }

  private static void validateAcyclic(
      final Set<String> nodeIds,
      final Map<String, Set<String>> outgoing,
      final Map<String, Set<String>> incoming) {
    Map<String, Integer> remainingPredecessors = new HashMap<>();
    ArrayDeque<String> ready = new ArrayDeque<>();
    for (String id : nodeIds) {
      int count = incoming.get(id).size();
      remainingPredecessors.put(id, count);
      if (count == 0) {
        ready.add(id);
      }
    }
    int visited = 0;
    while (!ready.isEmpty()) {
      String current = ready.removeFirst();
      visited++;
      for (String successor : outgoing.get(current)) {
        int remaining = remainingPredecessors.merge(successor, -1, Integer::sum);
        if (remaining == 0) {
          ready.add(successor);
        }
      }
    }
    if (visited != nodeIds.size()) {
      throw new IllegalArgumentException("progression must be acyclic");
    }
  }

  /**
   * One stable graph node bound to one complete riddle definition.
   *
   * @param id stable graph-node identifier
   * @param riddle complete riddle definition
   */
  public record RiddleNode(String id, ComposedRiddleDefinition riddle) {
    /** Creates a riddle node. */
    public RiddleNode {
      id = DefinitionChecks.requireId(id, "progression riddle node id");
      Objects.requireNonNull(riddle, "riddle");
    }
  }

  /**
   * One exact authored mandatory dependency edge.
   *
   * @param from source node identifier
   * @param to target node identifier
   */
  public record Edge(String from, String to) {
    /** Creates an edge. */
    public Edge {
      from = DefinitionChecks.requireId(from, "progression edge source");
      to = DefinitionChecks.requireId(to, "progression edge target");
    }
  }
}
