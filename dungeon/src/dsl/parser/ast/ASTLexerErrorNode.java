package dsl.parser.ast;

import dsl.error.ErrorRecord;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class ASTLexerErrorNode extends Node {
  public ASTLexerErrorNode(ErrorRecord errorRecord) {
    super(Type.ErrorNode);
    this.setErrorRecord(errorRecord);
  }

  public ASTLexerErrorNode() {
    super(Type.ErrorNode);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
