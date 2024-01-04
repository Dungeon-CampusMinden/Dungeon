package dsl.runtime.callable;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.List;

/**
 * {@link ICallable} implementation for native methods, which are an integral built-in part of the
 * DungeonDSL. It is used for binding {@link IInstanceCallable} implementations as symbols in a DSL
 * data type.
 */
public class NativeMethod extends Symbol implements ICallable {
  private final IInstanceCallable instanceCallable;

  /**
   * Constructor.
   *
   * @param name The name of the method
   * @param parentScope The parent scope of this symbol
   * @param functionType The {@link FunctionType} containing the signature information of the method
   * @param callable The {@link IInstanceCallable}, which contains the actual implementation of the
   *     method's logic
   */
  public NativeMethod(
      String name, IScope parentScope, FunctionType functionType, IInstanceCallable callable) {
    super(name, parentScope, functionType);
    this.instanceCallable = callable;
  }

  @Override
  public Object call(DSLInterpreter interperter, List<Node> parameters) {
    // resolve "THIS_VALUE"
    Value instance = interperter.getCurrentInstanceMemorySpace().resolve(Value.THIS_NAME);
    return this.instanceCallable.call(interperter, instance, parameters);
  }

  @Override
  public ICallable.Type getCallableType() {
    return null;
  }

  @Override
  public FunctionType getFunctionType() {
    return (FunctionType) this.dataType;
  }
}
