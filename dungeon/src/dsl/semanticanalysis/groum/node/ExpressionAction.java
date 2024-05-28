package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import java.util.List;

// this class is used to groum action nodes holding variable references in an expression
public class ExpressionAction extends ActionNode {

  @Override
  public String getLabel() {
    return "<expression [" + this.referencedInstanceId() + "]>";
  }

  public ExpressionAction() {
    super(ActionType.expression);
    this.updateLabels();
  }

  public ExpressionAction(ActionType actionType) {
    super(actionType);
    this.updateLabels();
  }

  public ExpressionAction(List<GroumNode> childNodes, long expressionId) {
    super(ActionType.expression);
    this.addChildren(childNodes);
    this.referencedInstanceId(expressionId);
    this.updateLabels();
  }

  protected ExpressionAction(ActionType type, List<GroumNode> childNodes, long expressionId) {
    super(type);
    this.addChildren(childNodes);
    this.referencedInstanceId(expressionId);
    this.updateLabels();
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
