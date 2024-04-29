package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class ConstRefAction extends ActionNode {
  public static int referencedTypeIdx = 0;

  @Override
  public String getLabel() {
    return "<ref const "
        + this.referencedType().getName()
        + " ["
        + this.referencedInstanceId()
        + "]>";
  }

  public ConstRefAction() {
    super(ActionType.constRef);
  }

  public ConstRefAction(Symbol type) {
    super(ActionType.constRef);
    this.addSymbolReference(type);
  }

  public IType referencedType() {
    return (IType) this.symbolReferences().get(referencedTypeIdx);
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
