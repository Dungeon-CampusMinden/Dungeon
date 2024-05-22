package dsl.parser.ast;

import dsl.error.ErrorRecord;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity
public class ASTErrorNode extends Node {
  @Transient private final ErrorNode internalErrorNode;

  public ASTErrorNode() {
    super(Type.ErrorNode);
    this.internalErrorNode = null;
    this.fileVersion = -1;
  }

  public ASTErrorNode(ErrorNode errorNode) {
    super(Type.ErrorNode);
    this.internalErrorNode = errorNode;
  }

  public ASTErrorNode(ErrorNode errorNode, ErrorRecord errorRecord) {
    this(errorNode);
    this.setErrorRecord(errorRecord);
    this.setSourceFileReference(
        new SourceFileReference(errorRecord.line() - 1, errorRecord.charPositionInLine()));
  }

  public ErrorNode internalErrorNode() {
    return this.internalErrorNode;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
