package dsl.parser.ast;

import dsl.error.ErrorRecord;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity
public class ASTErrorNode extends Node {
  @Transient private final ErrorNode internalErrorNode;

  @Property private String optionalMatchedText;

  public ASTErrorNode() {
    super(Type.ErrorNode);
    this.internalErrorNode = null;
    this.fileVersion = -1;
  }

  public ASTErrorNode(ErrorNode errorNode) {
    super(Type.ErrorNode);
    this.internalErrorNode = errorNode;
  }

  public ASTErrorNode(ErrorRecord errorRecord) {
    super(Type.ErrorNode);
    this.internalErrorNode = null;
    this.setErrorRecord(errorRecord);
    this.setSourceFileReference(
        new SourceFileReference(errorRecord.line() - 1, errorRecord.charPositionInLine()));
    this.setOptionalMatchedText(errorRecord);
  }

  public ASTErrorNode(ErrorNode errorNode, ErrorRecord errorRecord) {
    this(errorNode);
    this.setErrorRecord(errorRecord);
    this.setSourceFileReference(
        new SourceFileReference(errorRecord.line() - 1, errorRecord.charPositionInLine()));
    this.setOptionalMatchedText(errorRecord);
  }

  public ErrorNode internalErrorNode() {
    return this.internalErrorNode;
  }

  public String optionalMatchedText() {
    return this.optionalMatchedText;
  }

  private void setOptionalMatchedText(ErrorRecord record) {
    if (record.errorType().equals(ErrorRecord.ErrorType.unwantedToken)) {
      this.optionalMatchedText = record.offendingSymbol().getText();
    }
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
