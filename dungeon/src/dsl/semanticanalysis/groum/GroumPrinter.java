package dsl.semanticanalysis.groum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
  private HashMap<GroumNode, StringBuilder> actionsWithChildren = new HashMap<>();
  private HashSet<GroumNode> alreadyPrintedNodes = new HashSet<>();
  private long actionNodeCounter = 0;
  private long controlNodeCounter = 0;

  private StringBuilder builder;

  public String print(Groum groum) {
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
      if (node instanceof ActionNode) {
        actionNode((ActionNode) node, builder);
      } else {
        controlNode((ControlNode) node, builder);
      }
    }

    for (var edge : groum.edges) {
      edge(edge, builder);
    }

    for (var rootNode : rootNodes) {
      // check, if it contains children, which also contain other nodes
      printNodeWithChildren(rootNode);
    }

    builder.append("\n").append(postamble);
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
    String nodeString = String.format(actionNodeDeclarationFmt, nodeId, node.toString());

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
    String nodeString = String.format(controlNodeDeclarationFmt, nodeId, node.toString());

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
    var start = edge.start();
    String startId = this.idMap.get(start);

    var end = edge.end();
    String endId = this.idMap.get(end);

    String edgeType = edge.edgeType().toString();
    String edgeString = String.format(edgeFmt, startId, endId, edgeType);

    // if start and end both are in expressionActionMap -> put edge in corresponding expression
    // subgraph
    if (start.parent() != GroumNode.NONE && start.parent() == end.parent()) {
      // put edge in subgraph of parent
      var parent = start.parent();
      var expressionActionStringBuilder = actionsWithChildren.get(parent);
      if (expressionActionStringBuilder == null) {
        boolean b = true;
      }
      expressionActionStringBuilder.append(edgeString).append("\n");
    } else {
      builder.append(edgeString).append("\n");
    }
  }
}
