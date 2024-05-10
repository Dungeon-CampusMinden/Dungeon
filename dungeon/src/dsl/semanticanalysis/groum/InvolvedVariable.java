package dsl.semanticanalysis.groum;

public class InvolvedVariable {
  public enum TypeOfInvolvement {
    none,
    read,
    write,
    readWrite
  }

  // TODO: maybe this should be just instanceId?
  private Long variableInstanceId;

  private TypeOfInvolvement type;

  private GroumNode definitionNode;

  public InvolvedVariable(
      Long variableInstanceId, TypeOfInvolvement type, GroumNode definitionNode) {
    this.type = type;
    this.variableInstanceId = variableInstanceId;
    this.definitionNode = definitionNode;
  }

  public Long variableInstanceId() {
    return this.variableInstanceId;
  }

  public TypeOfInvolvement typeOfInvolvement() {
    return this.type;
  }

  public GroumNode definitionNode() {
    return this.definitionNode;
  }
}
