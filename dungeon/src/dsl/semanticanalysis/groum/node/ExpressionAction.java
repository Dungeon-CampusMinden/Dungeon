package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import java.util.List;

// this class is used to groum action nodes holding variable references in an expression
public class ExpressionAction extends ActionNode {

  public enum Operator {
    none,
    greaterThan,
    greaterThenEqual,
    lessThan,
    lessThanEqual,
    plus,
    minus,
    divide,
    times,
    and,
    or
  }

  private Operator operator;

  @Override
  public String getLabel() {
    return "<expression [" + this.referencedInstanceId() + "] " + "op: " + this.operator + ">";
  }

  public ExpressionAction() {
    super(ActionType.expression);
    this.operator = Operator.none;
    this.updateLabels();
  }

  public ExpressionAction(ActionType actionType) {
    super(actionType);
    this.operator = Operator.none;
    this.updateLabels();
  }

  public ExpressionAction(List<GroumNode> childNodes, long expressionId) {
    super(ActionType.expression);
    this.addChildren(childNodes);
    this.referencedInstanceId(expressionId);
    this.operator = Operator.none;
    this.updateLabels();
  }

  protected ExpressionAction(ActionType type, List<GroumNode> childNodes, long expressionId) {
    super(type);
    this.addChildren(childNodes);
    this.referencedInstanceId(expressionId);
    this.operator = Operator.none;
    this.updateLabels();
  }

  public Operator operator() {
    return this.operator;
  }

  public void operator(Operator operator) {
    this.operator = operator;
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
