package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class VariableReferenceAction extends ActionNode {
  private static final int referencedVariableTypeIdx = 0;
  private static final int referencedSymbolIdx = 1;

  public VariableReferenceAction(Symbol referencedSymbol, long referenceId) {
    super(ActionType.referencedInExpression);
    if (referenceId == 374) {
      boolean b = true;
    }
    this.addSymbolReference(getInstanceSymbolType(referencedSymbol));
    this.addSymbolReference(referencedSymbol);
    this.referencedInstanceId(referenceId);
  }

  public IType variableType() {
    return (IType) this.symbolReferences().get(referencedVariableTypeIdx);
  }

  public Symbol variableSymbol() {
    return this.symbolReferences().get(referencedSymbolIdx);
  }

  @Override
  public String getLabel() {
    return this.variableType().getName()
        + ":<ref ["
        + this.referencedInstanceId()
        + "]> (name: '"
        + this.variableSymbol().getName()
        + "')";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
