package dsl.semanticanalysis.groum;

import java.util.ArrayList;
import java.util.List;

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
public abstract class GroumNode {

  // explicit null object
  public static GroumNode NONE = new GroumNode() {
    @Override
    public String getLabel() {
      return "NONE";
    }

    @Override
    public void addIncoming(GroumEdge edge) { }

    @Override
    public void addOutgoing(GroumEdge edge) { }
  };

  private List<GroumEdge> incomingEdges;
  private List<GroumEdge> outgoingEdges;

  public GroumNode() {
    this.incomingEdges = new ArrayList<>();
    this.outgoingEdges = new ArrayList<>();
  }

  public void addIncoming(GroumEdge edge) {
    this.incomingEdges.add(edge);
  }

  public void addOutgoing(GroumEdge edge) {
    this.outgoingEdges.add(edge);
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
}

