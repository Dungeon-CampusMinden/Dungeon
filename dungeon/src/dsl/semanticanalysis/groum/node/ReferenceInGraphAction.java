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
public class ReferenceInGraphAction extends ActionNode {
  @Transient private final IType referencedVariableType;
  @Transient private final Symbol referencedSymbol;

  // @Relationship private final IType referencedVariableType;
  // @Relationship private final Symbol referencedSymbol;

  @Override
  public Map<String, Tuple<String, List<Long>>> getSimpleRelationships() {
    var superMap = super.getSimpleRelationships();
    superMap.put(
        "REFERENCED_VARIABLE_TYPE", new Tuple<>("IType", List.of(referencedVariableType.getId())));
    superMap.put("REFERENCED_SYMBOL", new Tuple<>("Symbol", List.of(referencedSymbol.getIdx())));
    return superMap;
  }

  public ReferenceInGraphAction(Symbol referencedSymbol, long referenceId) {
    super(ActionType.referencedInGraph);
    this.referencedVariableType = referencedSymbol.getDataType();
    this.referencedSymbol = referencedSymbol;
    this.referencedInstanceId(referenceId);
    this.updateLabels();
  }

  public ReferenceInGraphAction() {
    super(ActionType.referencedInGraph);
    this.referencedVariableType = BuiltInType.noType;
    this.referencedSymbol = Symbol.NULL;

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
