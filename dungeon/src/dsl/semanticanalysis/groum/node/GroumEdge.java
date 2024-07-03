package dsl.semanticanalysis.groum.node;

import dsl.IndexGenerator;
import org.neo4j.ogm.annotation.*;

// TODO: adapt for RelationshipRecorder
@RelationshipEntity
public class GroumEdge {
  public enum GroumEdgeType {
    EDGE_NONE,
    EDGE_TEMPORAL,
    EDGE_DATA_READ,
    EDGE_DATA_WRITE,
    EDGE_CONTROL_PARENT
  }

  @Id @GeneratedValue private Long id;
  @Property public Long internalId = IndexGenerator.getUniqueIdx();

  @StartNode private final GroumNode start;
  @EndNode private final GroumNode end;
  @Property private final int idxOnStart;
  @Property private final int idxOnEnd;
  @Property private final GroumEdgeType edgeType;
  @Property private String label;
  @Transient private boolean draw;
  @Transient private boolean ignoreInDataAnalysis;

  @Transient private String cypherCreationString;

  public String cypherCreationString() {
    return cypherCreationString;
  }

  public GroumEdge() {
    this.start = GroumNode.NONE;
    this.end = GroumNode.NONE;
    this.idxOnStart = -1;
    this.idxOnEnd = -1;
    this.edgeType = GroumEdgeType.EDGE_NONE;
    this.label = this.toString();
    generateCypherString();
  }

  public GroumEdge(GroumNode start, GroumNode end, GroumEdgeType edgeType, boolean draw, boolean ignoreInDataAnalysis) {
    this.start = start;
    this.idxOnStart = start.outgoing().size();
    start.addOutgoing(this);

    this.end = end;
    this.idxOnEnd = end.incoming().size();
    end.addIncoming(this);

    this.edgeType = edgeType;

    this.label = this.toString();

    if (start.processedCounter() == 4 && edgeType.equals(GroumEdgeType.EDGE_DATA_READ)) {
      boolean b = true;
    }
    this.draw = draw;
    this.ignoreInDataAnalysis = ignoreInDataAnalysis;
    generateCypherString();
  }

  public GroumEdge(GroumNode start, GroumNode end, GroumEdgeType edgeType) {
    this(start, end, edgeType, true, false);
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

  public boolean draw() {
    return draw;
  }

  public boolean ignoreInDataAnalysis() {
    return ignoreInDataAnalysis;
  }

  @Override
  public String toString() {
    return start().getLabel() + " -[" + this.edgeType.toString() + "]-> " + end.toString();
  }

  private void generateCypherString() {
    this.cypherCreationString =
        String.format(
            "[:GROUM_EDGE {edgeType:\"%s\", idxOnStart:%s, idxOnEnd:%s, label:\"%s\"}]",
            edgeType, idxOnStart, idxOnEnd, label);
  }
}
