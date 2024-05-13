package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import java.util.List;

public class PassAsParameterAction extends ExpressionAction {
  // TODO: should this reference a concrete object? or could the referenced objects
  //  be determined from the child nodes?
  public PassAsParameterAction(List<GroumNode> childNodes, long expressionId) {
    super(ActionType.passAsParameter, childNodes, expressionId);
  }

  @Override
  public String getLabel() {
    return "<pass as param [" + this.referencedInstanceId() + "]>";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
