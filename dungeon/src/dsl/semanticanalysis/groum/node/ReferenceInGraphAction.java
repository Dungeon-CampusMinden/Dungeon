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
public class ReferenceInGraphAction extends ActionNode {
  @Relate @Transient protected final IType referencedVariableType;
  @Relate @Transient protected final Symbol referencedSymbol;

  /*@Override
  public Map<String, Tuple<String, List<Long>>> getSimpleRelationships() {
    var superMap = super.getSimpleRelationships();
    superMap.put(
        "REFERENCED_VARIABLE_TYPE", new Tuple<>("IType", List.of(referencedVariableType.getId())));
    superMap.put("REFERENCED_SYMBOL", new Tuple<>("Symbol", List.of(referencedSymbol.getId())));
    return superMap;
  }*/

  public ReferenceInGraphAction(Symbol referencedSymbol, long referenceId) {
    super(ActionType.referencedInGraph);
    this.referencedVariableType = referencedSymbol.getDataType();
    this.referencedSymbol = referencedSymbol;
    this.referencedInstanceId(referenceId);
    this.updateLabels();
    RelationshipRecorder.instance.addRelatable(this);
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
