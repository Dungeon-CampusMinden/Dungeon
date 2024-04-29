package dsl.semanticanalysis.groum;

public class GroumEdge {
  public enum GroumEdgeType {
    none,
    temporal,
    dataDependencyRead,
    dataDependencyWrite
  }

  private final GroumNode start;
  private final GroumNode end;
  private final GroumEdgeType edgeType;

  public GroumEdge(GroumNode start, GroumNode end, GroumEdgeType edgeType) {
    this.start = start;
    start.addOutgoing(this);

    this.end = end;
    end.addIncoming(this);

    this.edgeType = edgeType;
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
