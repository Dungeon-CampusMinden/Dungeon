package dsl.semanticanalysis.groum;

import dsl.IndexGenerator;
import org.neo4j.ogm.annotation.Index;

// TODO: add way to specify scope -> store child nodes?
public class ControlNode extends GroumNode {
  // TODO: maybe this should be implemented like ASTNodes? with all the different subclasses?
  public enum ControlType {
    none,
    whileLoop,
    forLoop,
    countingForLoop,
    ifStmt,
    ifElseStmt,
    elseStmt,
    block,
    returnStmt
  }

  private final long id;

  private final ControlType controlType;

  public ControlNode() {
    this.controlType = ControlType.none;
    this.id = IndexGenerator.getIdx();
  }

  @Override
  public String getLabel() {
    return this.controlType.toString()+"["+id+"]";
  }

  @Override
  public String toString() {
    return this.getLabel();
  }

  public ControlNode(ControlType type) {
    this.controlType = type;
    this.id = IndexGenerator.getIdx();
  }

  public ControlType controlType() {
    return controlType;
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
