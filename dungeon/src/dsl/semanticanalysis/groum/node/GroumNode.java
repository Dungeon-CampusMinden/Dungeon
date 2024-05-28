package dsl.semanticanalysis.groum.node;

import core.utils.Tuple;
import dsl.parser.ast.Node;
import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.ogm.annotation.*;

// TODO: how is this class related to symbols and AST?
//  need to differentiate between groum (which is only
//  concerned with type information, which include methods
//  etc., and a concrete occurence of a groum
//  .
//  -> the groum-representation of a dsl program is a concrete
//  occurrence of a groum ('pattern-instantiation' if you will), naturally
//  .
//  -> the 'description' of a usage-pattern, which may be
//  instantiated during code completion or used for advanced
//  error handling, is just a pattern
@NodeEntity
public abstract class GroumNode {

  @Id @Property @GeneratedValue public Long identifier;

  // explicit null object
  public static GroumNode NONE =
      new GroumNode() {
        @Override
        public String getLabel() {
          return "NONE";
        }

        @Override
        public void addIncoming(GroumEdge edge) {}

        @Override
        public void addOutgoing(GroumEdge edge) {}
      };

  public GroumNode() {
    this.incomingEdges = new ArrayList<>();
    this.outgoingEdges = new ArrayList<>();
    this.children = new ArrayList<>();
    this.controlFlowParent = GroumNode.NONE;
    this.relatedAstNode = Node.NONE;
  }

  public Map<String, Tuple<String, List<Long>>> getSimpleRelationships() {
    var map = new HashMap<String, Tuple<String, List<Long>>>();
    if (this.relatedAstNode != null) {
      map.put("RELATED_AST_NODE", new Tuple<>("AstNode", List.of(this.relatedAstNode.getIdx())));
    }
    if (this.parent != null) {
      if (parent.identifier == null) {
        boolean b = true;
      } else {
        map.put("PARENT", new Tuple<>("GroumNode", List.of(this.parent.identifier)));
      }
    }
    map.put(
        "CHILDREN",
        new Tuple<>("GroumNode", this.children.stream().map(c -> c.identifier).toList()));
    return map;
  }

  // @Relationship private Node relatedAstNode;
  @Transient private Node relatedAstNode;
  @Relationship private GroumNode controlFlowParent;
  // @Transient private GroumNode controlFlowParent;

  @Transient private List<GroumEdge> incomingEdges;
  // @Relationship private List<GroumEdge> outgoingEdges;
  @Transient private List<GroumEdge> outgoingEdges;

  // only relevant, if contained in a larger Scope
  @Transient private GroumNode parent = GroumNode.NONE;
  @Transient private ArrayList<GroumNode> children;

  @Property private String label;

  protected void updateLabels() {
    this.label = this.getLabel();
    // this.labels = List.of(label);
  }

  public Long getId() {
    return identifier;
  }

  private long processedCounter = -1;

  public void setProcessedCounter(long counter) {
    if (this.processedCounter == -1) {
      this.processedCounter = counter;
    }
  }

  public long processedCounter() {
    return this.processedCounter;
  }

  public void relatedAstNode(Node node) {
    this.relatedAstNode = node;
  }

  public Node relatedAstNode() {
    return this.relatedAstNode;
  }

  public GroumNode controlFlowParent() {
    return this.controlFlowParent;
  }

  public void controlFlowParent(GroumNode controlFlowParent) {
    this.controlFlowParent = controlFlowParent;
  }

  public boolean isOrDescendentOf(GroumNode other) {
    var node = this;
    while (node != GroumNode.NONE && node != other) {
      node = node.parent;
    }
    return node == other;
  }

  public boolean hasAncestorLike(GroumNode other) {
    var myParent = this.parent;
    if (myParent == null) {
      return false;
    }
    while (myParent != GroumNode.NONE && myParent != other) {
      myParent = myParent.parent;
    }
    return myParent == other;
  }

  public boolean hasAncestorLikeParentOf(GroumNode other) {
    var myParent = this.parent;
    while (myParent != GroumNode.NONE) {
      myParent = myParent.parent;
      if (myParent == other.parent) {
        return true;
      }
    }
    return false;
  }

  public List<GroumEdge> getOutgoingOfType(GroumEdge.GroumEdgeType edgeType) {
    return this.outgoing().stream().filter(e -> e.edgeType().equals(edgeType)).toList();
  }

  public List<GroumEdge> getIncomingOfType(GroumEdge.GroumEdgeType edgeType) {
    return this.incoming().stream().filter(e -> e.edgeType().equals(edgeType)).toList();
  }

  public List<GroumNode> getEndsOfOutgoing(GroumEdge.GroumEdgeType edgeType) {
    return getOutgoingOfType(edgeType).stream().map(GroumEdge::end).toList();
  }

  public List<GroumNode> getStartsOfIncoming(GroumEdge.GroumEdgeType edgeType) {
    return getIncomingOfType(edgeType).stream().map(GroumEdge::start).toList();
  }

  public void addChild(GroumNode node) {
    if (node != this) {
      if (node.setParent(this)) {
        this.children.add(node);
      }
    }
  }

  public void addChildren(List<GroumNode> nodes) {
    var nodesButThis = nodes.stream().filter(c -> c != this).toList();
    for (var node : nodesButThis) {
      if (node.setParent(this)) {
        this.children.add(node);
      }
    }
    // this.children.addAll(nodesButThis);
  }

  public List<GroumNode> children() {
    return this.children;
  }

  public void addIncoming(GroumEdge edge) {
    this.incomingEdges.add(edge);
  }

  public void addOutgoing(GroumEdge edge) {
    this.outgoingEdges.add(edge);
  }

  public void removeIncomingEdge(GroumEdge edge) {
    this.incomingEdges.remove(edge);
  }

  public void removeOutgoingEdge(GroumEdge edge) {
    this.outgoingEdges.remove(edge);
  }

  public boolean setParent(GroumNode parent) {
    if (this.parent == GroumNode.NONE) {
      this.parent = parent;
      return true;
    }
    return false;
  }

  public GroumNode parent() {
    return this.parent;
  }

  public List<GroumEdge> incoming() {
    return this.incomingEdges;
  }

  public List<GroumEdge> outgoing() {
    return this.outgoingEdges;
  }

  public abstract String getLabel();

  // TODO: implement merging operations

  // TODO: attributes? (such as contained nodes in control structure-nodes, etc.)

  public Symbol getDefinitionSymbol() {
    if (this instanceof DefinitionAction definitionAction) {
      return definitionAction.instanceSymbol();
    } else if (this instanceof DefinitionByImportAction definitionByImportAction) {
      return definitionByImportAction.instanceSymbol();
    } else if (this instanceof ParameterInstantiationAction parameterInstantiationAction) {
      return parameterInstantiationAction.parameterSymbol();
    }
    return Symbol.NULL;
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
