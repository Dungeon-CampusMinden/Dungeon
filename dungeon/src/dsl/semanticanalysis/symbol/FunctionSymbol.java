package dsl.semanticanalysis.symbol;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.FuncDefNode;
import dsl.parser.ast.Node;
import dsl.runtime.callable.ICallable;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.List;

/** WTF? . */
public class FunctionSymbol extends ScopedSymbol implements ICallable {

  private final FuncDefNode astRootNode;

  /**
   * WTF? .
   *
   * @param name foo
   * @param parentScope foo
   * @param astRootNode foo
   * @param functionType foo
   */
  public FunctionSymbol(
      String name, IScope parentScope, FuncDefNode astRootNode, FunctionType functionType) {
    super(name, parentScope, functionType);

    this.astRootNode = astRootNode;
  }

  @Override
  public Object call(DSLInterpreter interpreter, List<Node> parameters) {
    return interpreter.executeUserDefinedFunction(this, parameters);
  }

  @Override
  public ICallable.Type getCallableType() {
    return ICallable.Type.UserDefined;
  }

  @Override
  public FunctionType getFunctionType() {
    return (FunctionType) this.getDataType();
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public FuncDefNode getAstRootNode() {
    return astRootNode;
  }
}
