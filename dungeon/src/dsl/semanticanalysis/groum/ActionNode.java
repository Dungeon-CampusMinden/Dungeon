package dsl.semanticanalysis.groum;

import dsl.parser.ast.Node;
import dsl.semanticanalysis.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;

// TODO: how to model prototype definition?
// TODO: How to model the passing of parameters to a function?
public abstract class ActionNode extends GroumNode {

  /*@Override
  public String getLabel() {
    return switch (this.actionType) {
      case none -> "NONE";
      case instantiation -> this.symbolReference.getDataType().getName() + ":<init>";
      // this may be interpreted as an <init>, based on the type of the parameter...lets see about it
      case passedAsParameter -> this.symbolReference.getDataType().getName()+ ":<param>";
      case functionParameter -> "<passed as param>:" + this.symbolReference.getDataType().getName();
      case functionCall -> "<func_call>:" + this.symbolReference.getDataType().getName();
      case propertyAccess -> ""; //should be: type name + property name
      case functionCallAccess -> ""; // should be: type name + function name
      case referencedInExpression -> this.symbolReference.getDataType().getName();
      // TODO: this is a control statement
      case referencedInReturnStmt -> this.symbolReference.getDataType().getName();
      case propertyDefinition -> "";
      case componentDefinition -> "";
    };
  }*/

  public enum ActionType {
    none,
    instantiation,
    parameterInstantiation,  // parameter 'instantiation';
                        // TODO: this may be two different types (by value, by reference) -> see value semantics!
    functionCall,
    propertyAccess,
    functionCallAccess,
    referencedInExpression,
    referencedInReturnStmt,
    passAsParameter,
    // TODO: how to model prototype definition???
    propertyDefinition,
    componentDefinition,
    expression
  }

  // this may model access to a specific instance, so need a unique id for modelling this
  private long referencedInstanceId;

  // if the groum describes a pattern, this is empty
  private Node astNodeReference;

  // this may be a type, this may be a function, this may be a property, who knows..
  private List<Symbol> symbolReferences;

  private final ActionType actionType;

  public ActionNode(ActionNode.ActionType actionType) {
    this.actionType = actionType;
    this.symbolReferences = new ArrayList<>();
  }

  public ActionNode() {
    this.actionType = ActionType.none;
    this.symbolReferences = new ArrayList<>();
  }

  public List<Symbol> symbolReferences() {
    return this.symbolReferences;
  }

  public void addSymbolReference(Symbol symbol) {
    this.symbolReferences.add(symbol);
  }

  public ActionType actionType() {
    return actionType;
  }

  public void astNodeReference(Node node) {
    this.astNodeReference = node;
  }

  public Node astNodeReference() {
    return astNodeReference;
  }

  public long referencedInstanceId() {
    return referencedInstanceId;
  }

  public void referencedInstanceId(long instanceId) {
    this.referencedInstanceId = instanceId;
  }

  @Override
  public String toString() {
    return this.getLabel();
  }
}

