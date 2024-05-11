package dsl.semanticanalysis.groum.node;

import dsl.IndexGenerator;
import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class ConstRefAction extends ActionNode {
  public static int referencedTypeIdx = 0;

  private Object value;

  @Override
  public String getLabel() {
    return "<ref CONST "
        + this.referencedType().getName()
        + " ["
        + this.referencedInstanceId()
        + "]> value: '"
        + this.value
        + "'";
  }

  public ConstRefAction() {
    super(ActionType.constRef);
  }

  public ConstRefAction(Symbol type, Object value) {
    super(ActionType.constRef);
    this.addSymbolReference(type);
    this.value = value;
    this.referencedInstanceId(IndexGenerator.getIdx());
  }

  public IType referencedType() {
    return (IType) this.symbolReferences().get(referencedTypeIdx);
  }

  public Object value() {
    return this.value;
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
