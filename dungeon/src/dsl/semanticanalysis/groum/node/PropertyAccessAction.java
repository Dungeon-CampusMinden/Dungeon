package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class PropertyAccessAction extends ActionNode {
  @Relationship private final IType instanceType;
  @Relationship private final Symbol instanceSymbol;
  @Relationship private final Symbol propertySymbol;

  public PropertyAccessAction(
      Symbol instanceSymbol, Symbol property, long instanceId, long propertyInstanceId) {
    super(ActionType.propertyAccess);

    this.instanceType = (IType) getInstanceSymbolType(instanceSymbol);
    this.instanceSymbol = instanceSymbol;
    this.propertySymbol = property;
    this.referencedInstanceId(instanceId);
    this.propertyInstanceId = propertyInstanceId;
    this.updateLabels();
  }

  public final long propertyInstanceId;

  public IType instanceDataType() {
    return this.instanceType;
  }

  public Symbol propertySymbol() {
    return this.propertySymbol;
  }

  public Symbol instanceSymbol() {
    return this.instanceSymbol;
  }

  @Override
  public String getLabel() {
    return this.instanceDataType().getName()
        + " ["
        + this.referencedInstanceId()
        + "]"
        + ":<property access '"
        + this.propertySymbol().getName()
        + "' ["
        + this.propertyInstanceId
        + "]>";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
