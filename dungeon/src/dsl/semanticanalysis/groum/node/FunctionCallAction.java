package dsl.semanticanalysis.groum.node;

import dsl.programmanalyzer.Relate;
import dsl.programmanalyzer.RelationshipRecorder;
import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity
public class FunctionCallAction extends ActionNode {
  @Relate @Transient protected final Symbol functionSymbol;

  public FunctionCallAction(Symbol functionSymbol, long instanceId) {
    super(ActionType.functionCall);
    this.functionSymbol = functionSymbol;
    this.referencedInstanceId(instanceId);
    this.updateLabels();
    RelationshipRecorder.instance.addRelatable(this);
  }

  public FunctionCallAction() {
    super(ActionType.functionCall);
    this.functionSymbol = Symbol.NULL;
    this.updateLabels();
  }

  /*@Override
  public Map<String, Tuple<String, List<Long>>> getSimpleRelationships() {
    var superMap = super.getSimpleRelationships();
    superMap.put("FUNCTION_SYMBOL", new Tuple<>("Symbol", List.of(functionSymbol.getId())));
    return superMap;
  }*/

  public Symbol functionSymbol() {
    return this.functionSymbol;
  }

  @Override
  public String getLabel() {
    return "<call '" + this.functionSymbol().getName() + "' [" + this.referencedInstanceId() + "]>";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
