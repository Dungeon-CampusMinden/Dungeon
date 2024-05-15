package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class FunctionCallAction extends ActionNode {
  @Relationship private final Symbol functionSymbol;

  public FunctionCallAction(Symbol functionSymbol, long instanceId) {
    super(ActionType.functionCall);
    this.functionSymbol = functionSymbol;
    this.referencedInstanceId(instanceId);
    this.updateLabels();
  }

  public Symbol functionSymbol() {
    return this.functionSymbol;
  }

  @Override
  public String getLabel() {
    return "<call '" + this.functionSymbol().getName() + "' [" + this.referencedInstanceId() + "]>";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
