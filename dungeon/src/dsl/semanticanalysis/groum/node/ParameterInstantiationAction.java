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
public class ParameterInstantiationAction extends ActionNode {
  @Transient private final IType instanceType;
  @Transient private final Symbol instanceSymbol;

  // @Relationship private final IType instanceType;
  // @Relationship private final Symbol instanceSymbol;

  public ParameterInstantiationAction(Symbol parameterSymbol, long instanceId) {
    super(ActionType.parameterInstantiation);
    this.instanceType = parameterSymbol.getDataType();
    this.instanceSymbol = parameterSymbol;
    this.referencedInstanceId(instanceId);
    this.updateLabels();
  }

  public ParameterInstantiationAction() {
    super(ActionType.parameterInstantiation);
    this.instanceType = BuiltInType.noType;
    this.instanceSymbol = Symbol.NULL;
    this.updateLabels();
  }

  @Override
  public Map<String, Tuple<String, List<Long>>> getSimpleRelationships() {
    var superMap = super.getSimpleRelationships();
    superMap.put("INSTANCE_TYPE", new Tuple<>("IType", List.of(instanceType.getId())));
    superMap.put("INSTANCE_SYMBOL", new Tuple<>("Symbol", List.of(instanceSymbol.getIdx())));
    return superMap;
  }

  public IType instantiatedType() {
    return this.instanceType;
  }

  public Symbol parameterSymbol() {
    return this.instanceSymbol;
  }

  @Override
  public String getLabel() {
    return this.instantiatedType().toString()
        + ":<param_init ["
        + this.referencedInstanceId()
        + "]>"
        + "(name: '"
        + this.parameterSymbol().getName()
        + "')";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
