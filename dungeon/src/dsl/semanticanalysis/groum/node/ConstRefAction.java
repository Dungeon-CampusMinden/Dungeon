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
public class ConstRefAction extends ActionNode {
  private Object value;
  @Relate @Transient protected final IType referencedType;

  @Override
  public String getLabel() {
    return "<ref CONST "
        + this.referencedType().getName()
        + " ["
        + this.referencedInstanceId()
        + "]> value: '"
        + this.value
        + "'";
  }

  /*@Override
  public Map<String, Tuple<String, List<Long>>> getSimpleRelationships() {
    var superMap = super.getSimpleRelationships();
    superMap.put("REFERENCED_TYPE", new Tuple<>("IType", List.of(this.referencedType.getId())));
    return superMap;
  }*/

  ConstRefAction() {
    super(ActionType.constRef);
    this.referencedType = BuiltInType.noType;
    this.updateLabels();
    // TODO: not sure, if this should be done in the default ctor, which likely will only be called
    // from
    //  neo4j anyways
    RelationshipRecorder.instance.addRelatable(this);
  }

  public ConstRefAction(Symbol type, Object value, long instanceId) {
    super(ActionType.constRef);
    this.referencedType = (IType) type;
    this.value = value;
    this.referencedInstanceId(instanceId);
    this.updateLabels();
    RelationshipRecorder.instance.addRelatable(this);
  }

  public IType referencedType() {
    return this.referencedType;
  }

  public Object value() {
    return this.value;
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
