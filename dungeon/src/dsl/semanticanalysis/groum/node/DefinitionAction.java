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
public class DefinitionAction extends ActionNode {
  @Transient private final IType instanceType;
  @Transient private final Symbol instanceSymbol;

  // @Relationship private final IType instanceType;
  // @Relationship private final Symbol instanceSymbol;

  public DefinitionAction(Symbol symbol, long instanceId) {
    super(ActionType.definition);

    // just always using the return type of a function symbol here is not correct, because
    // sometimes the value can be of a function type (setting a specific scenario_builer
    // in task definitions for example...)
    this.instanceType = (IType) getInstanceSymbolType(symbol);
    this.instanceSymbol = symbol;
    this.referencedInstanceId(instanceId);
    this.updateLabels();
  }

  public DefinitionAction() {
    super(ActionType.definition);
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

  public IType instancedType() {
    return this.instanceType;
  }

  // how to handle this in plain pattern groum?
  public Symbol instanceSymbol() {
    return this.instanceSymbol;
  }

  @Override
  public String getLabel() {
    return this.instancedType().toString()
        + ":<def ["
        + this.referencedInstanceId()
        + "]>"
        + "(name: '"
        + this.instanceSymbol().getName()
        + "')";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
