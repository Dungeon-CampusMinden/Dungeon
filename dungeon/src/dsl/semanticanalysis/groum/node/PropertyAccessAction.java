package dsl.semanticanalysis.groum.node;

import core.utils.Tuple;
import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.util.List;
import java.util.Map;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity
public class PropertyAccessAction extends ActionNode {
  @Transient private final IType instanceType;
  @Transient private final Symbol instanceSymbol;
  @Transient private final Symbol propertySymbol;
  // @Relationship private final IType instanceType;
  // @Relationship private final Symbol instanceSymbol;
  // @Relationship private final Symbol propertySymbol;
  public final long propertyInstanceId;

  @Override
  public Map<String, Tuple<String, List<Long>>> getSimpleRelationships() {
    var superMap = super.getSimpleRelationships();
    superMap.put("INSTANCE_TYPE", new Tuple<>("IType", List.of(instanceType.getId())));
    superMap.put("INSTANCE_SYMBOL", new Tuple<>("Symbol", List.of(instanceSymbol.getIdx())));
    superMap.put("PROPERTY_SYMBOL", new Tuple<>("Symbol", List.of(propertySymbol.getIdx())));

    return superMap;
  }

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

  public PropertyAccessAction() {
    super(ActionType.propertyAccess);
    this.instanceType = BuiltInType.noType;
    this.instanceSymbol = Symbol.NULL;
    this.propertySymbol = Symbol.NULL;
    this.propertyInstanceId = -1;
    this.updateLabels();
  }

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
