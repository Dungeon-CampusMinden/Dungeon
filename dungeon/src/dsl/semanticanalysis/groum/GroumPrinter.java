package dsl.semanticanalysis.groum;

import java.util.ArrayList;
import java.util.HashMap;

public class GroumPrinter {
  private static String preamble = "digraph G {";
  private static String postamble = "}";
  private static String controlShape = "shape=diamond";
  private static String actionShape = "shape=ellipse";
  private static String actionNodeDeclarationFmt = "%s [label=\"%s\"" + actionShape + "]";
  private static String controlNodeDeclarationFmt = "%s [label=\"%s\"" + controlShape + "]";
  private static String edgeFmt = "%s -> %s [label=%s]";
  private static String nodeWithChildrenStartFmt = "subgraph cluster_%s {\n label=\"%s\";\n";
  private static String nodeWithChildrenEnd = "}";

  private HashMap<Object, String> idMap = new HashMap<>();
  private HashMap<GroumNode, GroumNode> expressionActionMap = new HashMap<>();
  private HashMap<GroumNode, StringBuilder> expressionActionStrings = new HashMap<>();
  private long actionNodeCounter = 0;
  private long controlNodeCounter = 0;

  public String print(Groum groum) {
    StringBuilder builder = new StringBuilder();
    builder.append(preamble).append("\n");

    // collect all expression nodes
    var expressionActions = groum.nodes.stream().filter(n -> n instanceof ExpressionAction).map(n -> (ExpressionAction)n).toList();
    for (var node : expressionActions) {
      // fill expressionActionMap
      for (var childNode : node.childNodes()) {
        expressionActionMap.put(childNode, node);
      }

      // start Strings
      String init = String.format(nodeWithChildrenStartFmt, getOrCreateIdAction(node), node.getLabel());
      expressionActionStrings.put(node, new StringBuilder(init));
    }

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

    for (var expressionActionStringBuilder : expressionActionStrings.values()) {
      expressionActionStringBuilder.append(nodeWithChildrenEnd);
      builder.append(expressionActionStringBuilder);
    }

    builder.append("\n").append(postamble);
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

    if (expressionActionStrings.containsKey(node)) {
      var expressionNodeStringBuilder = expressionActionStrings.get(node);
      for (var child : ((ExpressionAction) node).childNodes()) {
        if (child instanceof ActionNode) {
          actionNode((ActionNode)child, expressionNodeStringBuilder);
        } else {
          controlNode((ControlNode)child, expressionNodeStringBuilder);
        }
      }

      // put expression node definition in expressionNodeStringBuilder
      expressionNodeStringBuilder.append(nodeString).append("\n");
    } else {
      builder.append(nodeString).append("\n");
    }
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

    // if start and end both are in expressionActionMap -> put edge in corresponding expression subgraph
    if (expressionActionMap.containsKey(start) && expressionActionMap.containsKey(end) &&
    expressionActionMap.get(start) == expressionActionMap.get(end)) {
      // put edge in subgraph of parent
      var parent = expressionActionMap.get(start);
      var expressionActionStringBuilder = expressionActionStrings.get(parent);
      expressionActionStringBuilder.append(edgeString).append("\n");
    } else {
      builder.append(edgeString).append("\n");
    }
  }
}
