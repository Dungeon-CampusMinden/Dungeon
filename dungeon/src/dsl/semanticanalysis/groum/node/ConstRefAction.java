package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class ConstRefAction extends ActionNode {
  private Object value;
  @Relationship private final IType referencedType;

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
    this.referencedType = BuiltInType.noType;
    this.updateLabels();
  }

  public ConstRefAction(Symbol type, Object value, long instanceId) {
    super(ActionType.constRef);
    this.referencedType = (IType) type;
    this.value = value;
    this.referencedInstanceId(instanceId);
    this.updateLabels();
  }

  public IType referencedType() {
    return this.referencedType;
  }

  public Object value() {
    return this.value;
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
