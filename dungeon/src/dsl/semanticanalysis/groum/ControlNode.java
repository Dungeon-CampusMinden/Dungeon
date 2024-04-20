package dsl.semanticanalysis.groum;

import java.util.ArrayList;
import java.util.List;

public class ControlNode extends GroumNode {

  // TODO: maybe this should be implemented like ASTNodes? with all the different subclasses?
  public enum ControlType {
    none,
    whileLoop,
    forLoop,
    countingForLoop,
    ifStmt,
    ifElseStmt,
    block,
    returnStmt
  }

  final private ControlType controlType;

  private List<GroumNode> containedNodes = new ArrayList<>();

  public ControlNode() {
    this.controlType = ControlType.none;
  }

  @Override
  public String getLabel() {
    return this.controlType.toString();
  }

  @Override
  public String toString() {
    return this.getLabel();
  }

  public ControlNode(ControlType type) {
    this.controlType = type;
  }

  public ControlType controlType() {
    return controlType;
  }

  public List<GroumNode> containedNodes() {
    return containedNodes;
  }
}
