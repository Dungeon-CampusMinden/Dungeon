package dsl.semanticanalysis.groum.node;

import dsl.IndexGenerator;
import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class MethodAccessAction extends ActionNode {
  public static final int instanceTypeIdx = 0;
  public static final int instanceSymbolIdx = 1;
  public static final int methodSymbolIdx = 2;
  private final long methodCallInstanceId;

  public MethodAccessAction(Symbol instanceSymbol, Symbol method, long instanceId) {
    super(ActionType.functionCallAccess);

    this.addSymbolReference(getInstanceSymbolType(instanceSymbol));
    this.addSymbolReference(instanceSymbol);
    this.addSymbolReference(method);
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
    return (IType) this.symbolReferences().get(instanceTypeIdx);
  }

  public Symbol instanceSymbol() {
    return this.symbolReferences().get(instanceSymbolIdx);
  }

  public Symbol methodSymbol() {
    return this.symbolReferences().get(methodSymbolIdx);
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
