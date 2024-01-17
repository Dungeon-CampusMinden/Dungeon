package dsl.semanticanalysis.symbol;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.FuncDefNode;
import dsl.parser.ast.Node;
import dsl.runtime.callable.ICallable;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.List;

public class ImportFunctionSymbol extends FunctionSymbol {
  private final FunctionSymbol originalSymbol;

  public FunctionSymbol originalFunctionSymbol() {
    return originalSymbol;
  }

  public ImportFunctionSymbol(FunctionSymbol originalSymbol, IScope parentScope) {
    super(
        originalSymbol.getName(),
        parentScope,
        originalSymbol.getAstRootNode(),
        originalSymbol.getFunctionType());
    this.originalSymbol = originalSymbol;
  }

  public ImportFunctionSymbol(FunctionSymbol originalSymbol, String name, IScope parentScope) {
    super(name, parentScope, originalSymbol.getAstRootNode(), originalSymbol.getFunctionType());
    this.originalSymbol = originalSymbol;
  }

  @Override
  public Object call(DSLInterpreter interpreter, List<Node> parameters) {
    return this.originalSymbol.call(interpreter, parameters);
  }

  @Override
  public ICallable.Type getCallableType() {
    return this.originalSymbol.getCallableType();
  }

  @Override
  public FunctionType getFunctionType() {
    return this.originalSymbol.getFunctionType();
  }

  @Override
  public FuncDefNode getAstRootNode() {
    return this.originalSymbol.getAstRootNode();
  }

  @Override
  public boolean bind(Symbol symbol) {
    // only allowed via original symbol
    throw new RuntimeException("This operation is not allowed via an imported symbol");
  }

  @Override
  public Symbol resolve(String name, boolean resolveInParent) {
    return this.originalSymbol.resolve(name, resolveInParent);
  }

  @Override
  public Symbol resolve(String name) {
    return this.originalSymbol.resolve(name);
  }

  @Override
  public IScope getParent() {
    return this.originalSymbol.getParent();
  }

  @Override
  public List<Symbol> getSymbols() {
    return this.originalSymbol.getSymbols();
  }
}
