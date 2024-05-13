package dsl.semanticanalysis.groum.node;

import dsl.IndexGenerator;
import dsl.semanticanalysis.groum.GroumVisitor;

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

  public boolean isConditional() {
    return switch (this.controlType) {
      case whileLoop, forLoop, countingForLoop, ifStmt, ifElseStmt, elseStmt -> true;
      case none, block, returnStmt -> false;
      default -> throw new IllegalStateException("Unexpected value: " + this.controlType);
    };
  }

  private final long id;

  private final ControlType controlType;

  public ControlNode() {
    this.controlType = ControlType.none;
    this.id = IndexGenerator.getIdx();
    this.updateLabels();
  }

  @Override
  public String getLabel() {
    return this.controlType.toString() + "[" + id + "]";
  }

  @Override
  public String toString() {
    return this.getLabel();
  }

  public ControlNode(ControlType type) {
    this.controlType = type;
    this.id = IndexGenerator.getIdx();
    this.updateLabels();
  }

  public ControlType controlType() {
    return controlType;
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
