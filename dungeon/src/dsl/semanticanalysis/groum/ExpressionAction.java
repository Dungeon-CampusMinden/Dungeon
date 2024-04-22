package dsl.semanticanalysis.groum;

import java.util.ArrayList;
import java.util.List;

// this class is used to groum action nodes holding variable references in an expression
public class ExpressionAction extends ActionNode {

  @Override
  public String getLabel() {
    return "<expression ["+this.referencedInstanceId()+"]>";
  }

  private ArrayList<GroumNode> childNodes = new ArrayList<>();

  public ExpressionAction() {
    super (ActionType.expression);
  }

  public ExpressionAction(List<GroumNode> childNodes, long expressionId) {
    this.childNodes.addAll(childNodes);
    this.referencedInstanceId(expressionId);
  }

  public List<GroumNode> childNodes() {
    return this.childNodes;
  }
}
