package dsl.semanticanalysis.groum;

import dsl.parser.ast.Node;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

import java.util.ArrayList;
import java.util.List;

// TODO: how to model prototype definition?
// TODO: How to model the passing of parameters to a function?
public abstract class ActionNode extends GroumNode {

  public static long uninitializedInstanceId = -1;

  public enum ActionType {
    none,
    definition,
    definitionByImport,
    parameterInstantiation, // parameter 'instantiation';
    // TODO: this may be two different types (by value, by reference) -> see value semantics!
    functionCall,
    propertyAccess,
    functionCallAccess,
    referencedInExpression,
    passAsParameter,
    expression,
    referencedInGraph,
    constRef
  }

  protected static Symbol getInstanceSymbolType(Symbol symbol) {
    IType instanceSymbolType;

    if (symbol instanceof IType type) {
      instanceSymbolType = type;
    } else {
      instanceSymbolType = symbol.getDataType();
    }
    return (Symbol)instanceSymbolType;
  }

  // this may model access to a specific instance, so need a unique id for modelling this
  private long referencedInstanceId = uninitializedInstanceId;

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

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
