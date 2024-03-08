package dsl.interpreter.taskgraph;

import dsl.interpreter.DSLInterpreter;
// CHECKSTYLE:ON: AvoidStarImport
import dsl.parser.ast.*;
import dsl.runtime.value.Value;
import graph.taskdependencygraph.TaskDependencyGraph;
// CHECKSTYLE:OFF: AvoidStarImport
import graph.taskdependencygraph.TaskEdge;
import graph.taskdependencygraph.TaskNode;
import java.util.*;
import task.Task;

/** WTF? . */
public class Interpreter implements AstVisitor<TaskNode> {
  private final DSLInterpreter dslInterpreter;

  // how to build graph?
  // - need nodes -> hashset, quasi symboltable
  Dictionary<String, TaskNode> graphNodes = new Hashtable<>();

  // - need edges (between two nodes)
  //      -> hashset with string-concat of Names with edge_op as key
  Dictionary<String, TaskEdge> graphEdges = new Hashtable<>();

  ArrayList<TaskDependencyGraph> graphs = new ArrayList<>();

  /**
   * WTF? .
   *
   * @param dslInterpreter foo
   */
  public Interpreter(DSLInterpreter dslInterpreter) {
    this.dslInterpreter = dslInterpreter;
  }

  /**
   * Parses a dot definition and creates a {@link TaskDependencyGraph} from it.
   *
   * @param dotDefinition The DotDefNode to parse as a graph
   * @return The {@link TaskDependencyGraph} object created from the dotDefinition
   */
  public TaskDependencyGraph getGraph(DotDefNode dotDefinition) {
    graphNodes = new Hashtable<>();
    graphEdges = new Hashtable<>();

    dotDefinition.accept(this);

    // sort edges
    var edgeIter = graphEdges.elements().asIterator();
    ArrayList<TaskEdge> edgeList = new ArrayList<>(graphEdges.size());

    while (edgeIter.hasNext()) {
      edgeList.add(edgeIter.next());
    }

    Collections.sort(edgeList);

    // sort nodes
    var nodeIter = graphNodes.elements().asIterator();
    ArrayList<TaskNode> nodeList = new ArrayList<>(graphNodes.size());

    while (nodeIter.hasNext()) {
      nodeList.add(nodeIter.next());
    }

    Collections.sort(nodeList);

    return new TaskDependencyGraph(edgeList, nodeList);
  }

  @Override
  public TaskNode visit(Node node) {
    // traverse down..
    for (Node child : node.getChildren()) {
      if (child.type == Node.Type.DotDefinition) {
        var graph = getGraph((DotDefNode) child);
        graphs.add(graph);
      } else {
        child.accept(this);
      }
    }
    return null;
  }

  @Override
  public TaskNode visit(IdNode node) {
    String name = node.getName();
    var task = (Value) node.accept(dslInterpreter);

    if (!(task.getInternalValue() instanceof Task)) {
      throw new RuntimeException("Reference to undefined Task '" + name + "'");
    }

    // lookup and create, if not present previously
    if (graphNodes.get(name) == null) {
      graphNodes.put(name, new TaskNode((Task) task.getInternalValue()));
    }

    // return Dot-Node
    return graphNodes.get(name);
  }

  @Override
  public TaskNode visit(DotDefNode node) {
    this.graphEdges = new Hashtable<>();
    this.graphNodes = new Hashtable<>();

    for (Node edgeStmt : node.getStmtNodes()) {
      edgeStmt.accept(this);
    }

    return null;
  }

  protected TaskEdge.Type getEdgeType(DotEdgeStmtNode node) {
    List<Node> attributes = node.getAttributes();
    var typeAttributes =
        attributes.stream()
            .filter(attrNode -> ((DotAttrNode) attrNode).getLhsIdName().equals("type"))
            .toList();
    if (typeAttributes.size() == 0) {
      throw new RuntimeException("No type attribute found on graph edge!");
    } else if (typeAttributes.size() > 1) {
      throw new RuntimeException("Too many type attributes found on graph edge!");
    }

    DotDependencyTypeAttrNode attr = (DotDependencyTypeAttrNode) typeAttributes.get(0);
    return attr.getDependencyType();
  }

  @Override
  public TaskNode visit(DotNodeStmtNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public TaskNode visit(DotEdgeStmtNode node) {
    TaskEdge.Type edgeType = getEdgeType(node);

    List<DotIdList> dotIdLists = node.getIdLists();
    var iter = dotIdLists.iterator();
    DotIdList lhsDotIdList = iter.next();
    DotIdList rhsDotIdList;

    do {
      rhsDotIdList = iter.next();

      for (Node lhsId : lhsDotIdList.getIdNodes()) {
        TaskNode taskNodeLhs = lhsId.accept(this);
        for (Node rhsId : rhsDotIdList.getIdNodes()) {
          TaskNode taskNodeRhs = rhsId.accept(this);

          var graphEdge = new TaskEdge(edgeType, taskNodeLhs, taskNodeRhs);
          graphEdges.put(graphEdge.name(), graphEdge);
        }
      }

      lhsDotIdList = rhsDotIdList;
    } while (iter.hasNext());

    return null;
  }
}
