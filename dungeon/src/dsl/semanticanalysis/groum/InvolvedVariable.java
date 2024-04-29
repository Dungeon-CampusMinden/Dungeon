package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;

public class InvolvedVariable {
  public enum TypeOfInvolvement {
    none,
    read,
    write,
    readWrite
  }

  // TODO: maybe this should be just instanceId?
  private Symbol variableSymbol;

  private TypeOfInvolvement type;

  private GroumNode definitionNode;

  public InvolvedVariable(Symbol variableSymbol, TypeOfInvolvement type, GroumNode definitionNode) {
    this.type = type;
    this.variableSymbol = variableSymbol;
    this.definitionNode = definitionNode;
  }

  public Symbol variableSymbol() {
    return this.variableSymbol;
  }

  public TypeOfInvolvement typeOfInvolvement() {
    return this.type;
  }

  public GroumNode definitionNode() {
    return this.definitionNode;
  }
}
