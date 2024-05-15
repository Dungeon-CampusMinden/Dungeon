package dsl.semanticanalysis.groum.node;

import dsl.IndexGenerator;
import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class MethodAccessAction extends ActionNode {
  private final long methodCallInstanceId;

  @Relationship private final IType instanceType;
  @Relationship private final Symbol instanceSymbol;
  @Relationship private final Symbol methodSymbol;

  public MethodAccessAction(Symbol instanceSymbol, Symbol method, long instanceId) {
    super(ActionType.functionCallAccess);

    this.instanceType = (IType) getInstanceSymbolType(instanceSymbol);
    this.instanceSymbol = instanceSymbol;
    this.methodSymbol = method;

    this.referencedInstanceId(instanceId);
    this.methodCallInstanceId = IndexGenerator.getIdx();
    this.updateLabels();
  }

  private DefinitionAction instanceRedefinitionNode;

  public void instanceRedefinitionNode(DefinitionAction node) {
    this.instanceRedefinitionNode = node;
  }

  public DefinitionAction instanceRedefinitionNode() {
    return this.instanceRedefinitionNode;
  }

  public IType instanceDataType() {
    return this.instanceType;
  }

  public Symbol instanceSymbol() {
    return this.instanceSymbol;
  }

  public Symbol methodSymbol() {
    return this.methodSymbol;
  }

  public long methodCallInstanceId() {
    return this.methodCallInstanceId;
  }

  @Override
  public String getLabel() {
    return this.instanceDataType().getName()
        + ":<method access '"
        + this.methodSymbol().getName()
        + "' ["
        + this.referencedInstanceId()
        + "]>";
  }

  public <T> T accept(GroumVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
