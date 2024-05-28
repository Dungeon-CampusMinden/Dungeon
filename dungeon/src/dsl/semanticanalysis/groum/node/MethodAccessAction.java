package dsl.semanticanalysis.groum.node;

import core.utils.Tuple;
import dsl.IndexGenerator;
import dsl.semanticanalysis.groum.GroumVisitor;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.util.List;
import java.util.Map;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity
public class MethodAccessAction extends ActionNode {
  private final long methodCallInstanceId;

  // @Relationship private final IType instanceType;
  // @Relationship private final Symbol instanceSymbol;
  // @Relationship private final Symbol methodSymbol;
  @Transient private final IType instanceType;
  @Transient private final Symbol instanceSymbol;
  @Transient private final Symbol methodSymbol;

  public MethodAccessAction(Symbol instanceSymbol, Symbol method, long instanceId) {
    super(ActionType.functionCallAccess);

    this.instanceType = (IType) getInstanceSymbolType(instanceSymbol);
    this.instanceSymbol = instanceSymbol;
    this.methodSymbol = method;

    this.referencedInstanceId(instanceId);
    this.methodCallInstanceId = IndexGenerator.getIdx();
    this.updateLabels();
  }

  @Override
  public Map<String, Tuple<String, List<Long>>> getSimpleRelationships() {
    var superMap = super.getSimpleRelationships();
    superMap.put("INSTANCE_TYPE", new Tuple<>("IType", List.of(instanceType.getId())));
    superMap.put("INSTANCE_SYMBOL", new Tuple<>("Symbol", List.of(instanceSymbol.getIdx())));
    superMap.put("METHOD_SYMBOL", new Tuple<>("Symbol", List.of(methodSymbol.getIdx())));
    return superMap;
  }

  public MethodAccessAction() {
    super(ActionType.functionCallAccess);
    this.instanceType = BuiltInType.noType;
    this.instanceSymbol = Symbol.NULL;
    this.methodSymbol = Symbol.NULL;
    this.methodCallInstanceId = -1;
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
