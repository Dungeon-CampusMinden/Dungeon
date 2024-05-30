package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.IndexGenerator;
import org.neo4j.ogm.annotation.*;

// TODO: obsolete
@RelationshipEntity()
public class ParameterRelationship {
  @Id @GeneratedValue private Long id;
  @Property public Long internalId = IndexGenerator.getUniqueIdx();
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
