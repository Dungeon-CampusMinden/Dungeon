package dsl.semanticanalysis.groum;

import java.util.HashMap;

public class GroumPrinter {
  private static String preamble = "digraph G {";
  private static String postamble = "}";
  private static String controlShape = "shape=diamond";
  private static String actionShape = "shape=ellipse";
  private static String actionNodeDeclarationFmt = "%s [label=\"%s\"" + actionShape + "]";
  private static String controlNodeDeclarationFmt = "%s [label=\"%s\"" + controlShape + "]";
  private static String edgeFmt = "%s -> %s [label=%s]";

  private HashMap<Object, String> idMap = new HashMap<>();
  private long actionNodeCounter = 0;
  private long controlNodeCounter = 0;

  public String print(Groum groum) {
    StringBuilder builder = new StringBuilder();
    builder.append(preamble).append("\n");

    for (var node : groum.nodes) {
      if (node instanceof ActionNode) {
        actionNode((ActionNode)node, builder);
      } else {
        controlNode((ControlNode)node, builder);
      }
    }

    for (var edge : groum.edges) {
      edge(edge, builder);
    }

    builder.append(postamble);
    return builder.toString();
  }

  private String getOrCreateIdAction(ActionNode node) {
    if (!this.idMap.containsKey(node)) {
      String id = "a"+actionNodeCounter;
      actionNodeCounter++;
      this.idMap.put(node, id);
    }
    return this.idMap.get(node);
  }

  private String getOrCreateIdControl(ControlNode node) {
    if (!this.idMap.containsKey(node)) {
      String id = "c"+controlNodeCounter;
      controlNodeCounter++;
      this.idMap.put(node, id);
    }
    return this.idMap.get(node);
  }

  private void actionNode(ActionNode node, StringBuilder builder) {
    String nodeId = getOrCreateIdAction(node);
    String nodeString = String.format(actionNodeDeclarationFmt, nodeId, node.toString());
    builder.append(nodeString).append("\n");
  }

  private void controlNode(ControlNode node, StringBuilder builder) {
    String nodeId = getOrCreateIdControl(node);
    String nodeString = String.format(controlNodeDeclarationFmt, nodeId, node.toString());
    builder.append(nodeString).append("\n");
  }

  private void edge(GroumEdge edge, StringBuilder builder) {
    var start = edge.start();
    String startId = this.idMap.get(start);

    var end = edge.end();
    String endId = this.idMap.get(end);

    String edgeType = edge.edgeType().toString();
    String edgeString = String.format(edgeFmt, startId, endId, edgeType);
    builder.append(edgeString).append("\n");
  }
}
