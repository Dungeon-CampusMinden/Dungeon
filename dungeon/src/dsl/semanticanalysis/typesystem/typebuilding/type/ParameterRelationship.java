package dsl.semanticanalysis.typesystem.typebuilding.type;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity()
public class ParameterRelationship {
  @Id @GeneratedValue Long id;
  @Property private int idx;

  @StartNode private IType functionType;
  @EndNode private IType parameterType;

  public ParameterRelationship() {
    this.functionType = BuiltInType.noType;
    this.parameterType = BuiltInType.noType;
    this.idx = -1;
  }

  public ParameterRelationship(IType functionType, IType parameterType, int idx) {
    this.functionType = functionType;
    this.parameterType = parameterType;
    this.idx = idx;
  }
}
