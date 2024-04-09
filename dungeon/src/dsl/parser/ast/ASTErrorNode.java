package dsl.parser.ast;

import dsl.error.ErrorListener;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class ASTErrorNode extends Node {
  private final ErrorNode internalErrorNode;

  public ASTErrorNode() {
    super(Type.ErrorNode);
    this.internalErrorNode = null;
  }

  public ASTErrorNode(ErrorNode errorNode) {
    super(Type.ErrorNode);
    this.internalErrorNode = errorNode;
  }

  public ASTErrorNode(ErrorNode errorNode, ErrorListener.ErrorRecord errorRecord) {
    this(errorNode);
    this.setErrorRecord(errorRecord);
  }

  public ErrorNode internalErrorNode() {
    return this.internalErrorNode;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
