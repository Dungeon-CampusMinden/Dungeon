package dsl.semanticanalysis.groum;

import java.util.List;

// this class is used to groum action nodes holding variable references in an expression
public class ExpressionAction extends ActionNode {

  @Override
  public String getLabel() {
    return "<expression [" + this.referencedInstanceId() + "]>";
  }

  public ExpressionAction() {
    super(ActionType.expression);
  }

  public ExpressionAction(List<GroumNode> childNodes, long expressionId) {
    this.addChildren(childNodes);
    this.referencedInstanceId(expressionId);
  }
}
