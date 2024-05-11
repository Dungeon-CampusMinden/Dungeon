package dsl.semanticanalysis.groum.node;

import org.neo4j.ogm.annotation.*;

import java.util.List;

@RelationshipEntity
public class GroumEdge {
  public enum GroumEdgeType {
    none,
    temporal,
    dataDependencyRead,
    dataDependencyRedefinition
  }

  @Id
  @GeneratedValue
  private Long id;

  @StartNode
  private final GroumNode start;
  @EndNode
  private final GroumNode end;
  @Property
  private final int idxOnStart;
  @Property
  private final int idxOnEnd;
  @Property
  private final GroumEdgeType edgeType;
  //@Labels
  //private List<String> labels;

  public GroumEdge() {
    this.start = GroumNode.NONE;
    this.end = GroumNode.NONE;
    this.idxOnStart = -1;
    this.idxOnEnd = -1;
    this.edgeType = GroumEdgeType.none;
    //labels = List.of(toString());
  }

  public GroumEdge(GroumNode start, GroumNode end, GroumEdgeType edgeType) {
    this.start = start;
    this.idxOnStart = start.outgoing().size();
    start.addOutgoing(this);

    this.end = end;
    this.idxOnEnd = end.incoming().size();
    end.addIncoming(this);

    this.edgeType = edgeType;
    //labels = List.of(toString());
  }

  public GroumNode start() {
    return start;
  }

  public GroumNode end() {
    return end;
  }

  public GroumEdgeType edgeType() {
    return edgeType;
  }

  @Override
  public String toString() {
    return start().getLabel() + " -[" + this.edgeType.toString() + "]-> " + end.toString();
  }
}
