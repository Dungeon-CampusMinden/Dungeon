package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.groum.node.GroumNode;

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

  // TODO:
  //  this is a leftover from the first implementation attempt, when the mechanics of groumScope
  // weren't clear!
  //  this should be refactored out and whenever a involved variable is used to determine, from
  // which node a data
  //  dependency edge should be added to the current node, the groumScope should be used to get
  // definition Nodes, not this
  //  internal definitionNode
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
