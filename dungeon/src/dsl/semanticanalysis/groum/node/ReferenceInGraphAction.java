package dsl.semanticanalysis.groum.node;

import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class ReferenceInGraphAction extends ActionNode {
  @Relationship private final IType referencedVariableType;
  @Relationship private final Symbol referencedSymbol;

  public ReferenceInGraphAction(Symbol referencedSymbol, long referenceId) {
    super(ActionType.referencedInGraph);
    this.referencedVariableType = referencedSymbol.getDataType();
    this.referencedSymbol = referencedSymbol;
    this.referencedInstanceId(referenceId);
    this.updateLabels();
  }

  public IType variableType() {
    return this.referencedVariableType;
  }

  public Symbol variableSymbol() {
    return this.referencedSymbol;
  }

  @Override
  public String getLabel() {
    return this.variableType().getName()
        + ":<ref in graph ["
        + this.referencedInstanceId()
        + "]>"
        + "(name: '"
        + this.variableSymbol().getName()
        + "')";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
