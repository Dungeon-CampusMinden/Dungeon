package dsl.semanticanalysis.groum.node;

import dsl.programmanalyzer.Relate;
import dsl.programmanalyzer.RelationshipRecorder;
import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity
public class ParameterInstantiationAction extends ActionNode {
  @Relate @Transient protected final IType instanceType;
  @Relate @Transient protected final Symbol instanceSymbol;

  public ParameterInstantiationAction(Symbol parameterSymbol, long instanceId) {
    super(ActionType.parameterInstantiation);
    this.instanceType = parameterSymbol.getDataType();
    this.instanceSymbol = parameterSymbol;
    this.referencedInstanceId(instanceId);
    this.updateLabels();
    RelationshipRecorder.instance.addRelatable(this);
  }

  public ParameterInstantiationAction() {
    super(ActionType.parameterInstantiation);
    this.instanceType = BuiltInType.noType;
    this.instanceSymbol = Symbol.NULL;
    this.updateLabels();
  }

  /*@Override
  public Map<String, Tuple<String, List<Long>>> getSimpleRelationships() {
    var superMap = super.getSimpleRelationships();
    superMap.put("INSTANCE_TYPE", new Tuple<>("IType", List.of(instanceType.getId())));
    superMap.put("INSTANCE_SYMBOL", new Tuple<>("Symbol", List.of(instanceSymbol.getId())));
    return superMap;
  }*/

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
