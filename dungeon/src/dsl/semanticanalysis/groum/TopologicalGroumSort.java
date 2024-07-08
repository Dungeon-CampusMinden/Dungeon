package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.groum.node.GroumEdge;
import dsl.semanticanalysis.groum.node.GroumNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TopologicalGroumSort {
  public static List<GroumNode> sortChildren(GroumNode node) {
    HashSet<GroumNode> allUnmarkedNodes = new HashSet<>();
    addAllChildren(node, allUnmarkedNodes);
    int allUnmarkedNodesSize = allUnmarkedNodes.size();
    // var childList = allChildren.stream().toList();
    var sortList = new ArrayList<GroumNode>();
    HashSet<GroumNode> permanentMarks = new HashSet<>();
    HashSet<GroumNode> temporaryMarks = new HashSet<>();
    while (permanentMarks.size() < allUnmarkedNodesSize && !allUnmarkedNodes.isEmpty()) {
      // select an unmarked node
      var first = allUnmarkedNodes.stream().findFirst().get();
      visitNode(first, permanentMarks, temporaryMarks, sortList, allUnmarkedNodes);
    }

    // sort
    return sortList.reversed();
  }

  private static void addAllChildren(GroumNode node, HashSet<GroumNode> nodes) {
    nodes.add(node);
    for (var child : node.children()) {
      nodes.add(child);
      addAllChildren(child, nodes);
    }
  }

  private static void visitNode(
      GroumNode node,
      HashSet<GroumNode> permanentMarks,
      HashSet<GroumNode> temporaryMarks,
      List<GroumNode> sortedList,
      HashSet<GroumNode> allUnmarkedChildren) {
    /*
    function visit(node n)
    if n has a permanent mark then
    return
    if n has a temporary mark then
    stop   (graph has at least one cycle)

    mark n with a temporary mark

    for each node m with an edge from n to m do
      visit(m)

    remove temporary mark from n
    mark n with a permanent mark
    add n to head of L
     */
    if (permanentMarks.contains(node)) {
      return;
    }
    if (temporaryMarks.contains(node)) {
      throw new RuntimeException("cycle");
    }
    temporaryMarks.add(node);
    allUnmarkedChildren.remove(node);

    for (var m : node.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_TEMPORAL)) {
      visitNode(m, permanentMarks, temporaryMarks, sortedList, allUnmarkedChildren);
    }

    temporaryMarks.remove(node);
    permanentMarks.add(node);
    allUnmarkedChildren.remove(node);
    sortedList.add(node);
  }
}
