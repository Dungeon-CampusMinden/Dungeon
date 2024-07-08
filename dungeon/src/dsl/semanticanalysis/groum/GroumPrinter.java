package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.groum.node.ActionNode;
import dsl.semanticanalysis.groum.node.ControlNode;
import dsl.semanticanalysis.groum.node.GroumEdge;
import dsl.semanticanalysis.groum.node.GroumNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class GroumPrinter {
  private static final int nodeFontSize = 20;
  private static final int edgeFontSize = 20;
  private static final int subGraphFontSize = 20;

  private static final String preamble =
      "digraph G {\n"
          + "graph [ranksep=0.1];\n"
          + "node [width=0.1, fontsize="
          + nodeFontSize
          + "];\n"
          + "edge [minlen=1, fontsize="
          + edgeFontSize
          + "];\n";
  private static final String epilog = "}";
  private static final String controlShape = "shape=diamond";
  private static final String actionShape = "shape=ellipse";
  private static final String actionNodeDeclarationFmt = "%s [label=\"%s\"" + actionShape + "]";
  private static final String controlNodeDeclarationFmt = "%s [label=\"%s\"" + controlShape + "]";
  private static final String invisibleEdgeStyle = "invis";
  private static final String normaleEdgeStyle = "solid";
  private static final String edgeFmt = "%s -> %s [label=%s, style=%s]";
  private static final String nodeWithChildrenStartFmt =
      "subgraph cluster_%s {\n label=\"%s\";\n fontsize=" + subGraphFontSize + ";\n\n";
  private static final String nodeWithChildrenEnd = "}";

  private final HashMap<Object, String> idMap = new HashMap<>();
  private final HashMap<GroumNode, StringBuilder> actionsWithChildren = new HashMap<>();
  private final HashSet<GroumNode> alreadyPrintedNodes = new HashSet<>();
  private long actionNodeCounter = 0;
  private long controlNodeCounter = 0;
  private boolean invisibleTemporalEdge = false;

  private StringBuilder builder;

  public String print(Groum groum) {
    return this.print(groum, false);
  }

  public String print(Groum groum, boolean invisibleTemporalEdges) {
    this.invisibleTemporalEdge = invisibleTemporalEdges;
    builder = new StringBuilder();
    builder.append(preamble).append("\n");

    // collect all expression nodes
    ArrayList<GroumNode> rootNodes = new ArrayList<>();
    var actionsWithChildren =
        groum.nodes.stream().filter(n -> !n.children().isEmpty()).collect(Collectors.toSet());
    for (var node : actionsWithChildren) {
      // start Strings
      String id =
          node instanceof ActionNode
              ? getOrCreateIdAction((ActionNode) node)
              : getOrCreateIdControl((ControlNode) node);
      String init = String.format(nodeWithChildrenStartFmt, id, node.getLabel());
      this.actionsWithChildren.put(node, new StringBuilder(init));

      if (node.parent() == GroumNode.NONE) {
        rootNodes.add(node);
      }
    }

    for (var node : groum.nodes) {
      var builderToUse = this.builder;
      if (node.parent() != GroumNode.NONE) {
        builderToUse = this.actionsWithChildren.get(node.parent());
      }
      if (node instanceof ActionNode) {
        actionNode((ActionNode) node, builderToUse);
      } else {
        controlNode((ControlNode) node, builderToUse);
      }
    }

    for (var edge : groum.edges) {
      edge(edge, builder);
    }

    for (var rootNode : rootNodes) {
      // check, if it contains children, which also contain other nodes
      printNodeWithChildren(rootNode);
    }

    builder.append("\n").append(epilog);
    return builder.toString();
  }

  private void printNodeWithChildren(GroumNode node) {
    List<GroumNode> children = node.children();

    for (var child : children) {
      // check, whether the child contains children
      if (this.actionsWithChildren.containsKey(child)) {
        printNodeWithChildren(child);
      }
    }

    // get stringbuilder
    var stringBuilder = this.actionsWithChildren.get(node);
    stringBuilder.append(nodeWithChildrenEnd);
    if (node.parent() == GroumNode.NONE) {
      // print globally
      this.builder.append(stringBuilder);
    } else {
      // print to string builder of parent
      var parent = node.parent();
      var parentsBuilder = this.actionsWithChildren.get(parent);
      parentsBuilder.append(stringBuilder);
    }
  }

  private String getOrCreateIdAction(ActionNode node) {
    if (!this.idMap.containsKey(node)) {
      String id = "a" + actionNodeCounter;
      actionNodeCounter++;
      this.idMap.put(node, id);
    }
    return this.idMap.get(node);
  }

  private String getOrCreateIdControl(ControlNode node) {
    if (!this.idMap.containsKey(node)) {
      String id = "c" + controlNodeCounter;
      controlNodeCounter++;
      this.idMap.put(node, id);
    }
    return this.idMap.get(node);
  }

  private void actionNode(ActionNode node, StringBuilder builder) {
    if (this.alreadyPrintedNodes.contains(node)) {
      // skip
      return;
    }

    String nodeId = getOrCreateIdAction(node);
    String nodeContent =
        node.toString()
            + (node.processedCounter() != -1 ? " proc idx: " + node.processedCounter() : "");
    String nodeString = String.format(actionNodeDeclarationFmt, nodeId, nodeContent);

    if (actionsWithChildren.containsKey(node)) {
      var expressionNodeStringBuilder = actionsWithChildren.get(node);

      // iteration over children
      for (var child : node.children()) {
        if (child instanceof ActionNode) {
          actionNode((ActionNode) child, expressionNodeStringBuilder);
        } else {
          controlNode((ControlNode) child, expressionNodeStringBuilder);
        }
      }

      // put expression node definition in expressionNodeStringBuilder
      expressionNodeStringBuilder.append(nodeString).append("\n");
    } else {
      builder.append(nodeString).append("\n");
    }
    alreadyPrintedNodes.add(node);
  }

  private void controlNode(ControlNode node, StringBuilder builder) {
    if (this.alreadyPrintedNodes.contains(node)) {
      // skip
      return;
    }
    String nodeId = getOrCreateIdControl(node);
    String nodeContent =
        node.toString()
            + (node.processedCounter() != -1 ? " proc idx: " + node.processedCounter() : "");
    String nodeString = String.format(controlNodeDeclarationFmt, nodeId, nodeContent);

    if (actionsWithChildren.containsKey(node)) {
      var expressionNodeStringBuilder = actionsWithChildren.get(node);
      for (var child : node.children()) {
        if (child instanceof ActionNode) {
          actionNode((ActionNode) child, expressionNodeStringBuilder);
        } else {
          controlNode((ControlNode) child, expressionNodeStringBuilder);
        }
      }

      // put expression node definition in expressionNodeStringBuilder
      expressionNodeStringBuilder.append(nodeString).append("\n");
    } else {
      builder.append(nodeString).append("\n");
    }
    this.alreadyPrintedNodes.add(node);
  }

  private void edge(GroumEdge edge, StringBuilder builder) {
    if (!edge.draw()) {
      return;
    }

    var start = edge.start();
    String startId = this.idMap.get(start);

    var end = edge.end();
    String endId = this.idMap.get(end);

    String edgeTypeStr = edge.edgeType().toString();
    var edgeType = edge.edgeType();
    String edgeStyle = normaleEdgeStyle;
    if (edgeType == GroumEdge.GroumEdgeType.EDGE_TEMPORAL && this.invisibleTemporalEdge) {
      edgeStyle = invisibleEdgeStyle;
    }
    String edgeString = String.format(edgeFmt, startId, endId, edgeTypeStr, edgeStyle);

    // if start and end both are in expressionActionMap -> put edge in corresponding expression
    // subgraph
    if (start.parent() != GroumNode.NONE && start.parent() == end.parent()) {
      // put edge in subgraph of parent
      var parent = start.parent();
      var expressionActionStringBuilder = actionsWithChildren.get(parent);
      expressionActionStringBuilder.append(edgeString).append("\n");
    } else {
      builder.append(edgeString).append("\n");
    }
  }
}
