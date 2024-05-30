package dsl.parser.ast;

import dsl.IndexGenerator;
import org.neo4j.ogm.annotation.*;

// Note: obsolete
// @RelationshipEntity(type = "PARENT_OF")
public class ParentOf {
  @Id @GeneratedValue private Long id;
  @Property public Long internalId = IndexGenerator.getUniqueIdx();
  @Property private int idx;
  @StartNode private Node parentNode;
  @EndNode private Node childNode;

  public ParentOf() {
    this.parentNode = Node.NONE;
    this.childNode = Node.NONE;
    this.idx = -1;
  }

  public ParentOf(Node parentNode, Node childNode, int idx) {
    this.parentNode = parentNode;
    this.childNode = childNode;
    this.idx = idx;
  }
}
