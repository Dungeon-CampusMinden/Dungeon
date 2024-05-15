package dsl.semanticanalysis.groum.node;

import dsl.parser.ast.Node;
import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import org.neo4j.ogm.annotation.*;

// TODO: how to model prototype definition?
// TODO: How to model the passing of parameters to a function?
@NodeEntity
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

    if (symbol == Symbol.NULL) {
      return BuiltInType.noType;
    }
    if (symbol instanceof IType type) {
      instanceSymbolType = type;
    } else {
      instanceSymbolType = symbol.getDataType();
    }
    return (Symbol) instanceSymbolType;
  }

  // this may model access to a specific instance, so need a unique id for modelling this
  @Property private long referencedInstanceId = uninitializedInstanceId;

  // if the groum describes a pattern, this is empty
  @Relationship private Node astNodeReference;

  @Property private final ActionType actionType;

  public ActionNode(ActionNode.ActionType actionType) {
    this.actionType = actionType;
  }

  public ActionNode() {
    this.actionType = ActionType.none;
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
