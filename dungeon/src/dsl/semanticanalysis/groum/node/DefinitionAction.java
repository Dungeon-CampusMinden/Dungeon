package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class DefinitionAction extends ActionNode {
  @Relationship private final IType instanceType;
  @Relationship private final Symbol instanceSymbol;

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
