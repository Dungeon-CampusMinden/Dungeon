package dsl.interpreter;

import dsl.parser.ast.Node;
import dsl.runtime.callable.ICallable;
import dsl.runtime.callable.NativeFunction;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.List;

/** WTF? . */
public class DummyNativeFunction extends NativeFunction {
  /**
   * WTF? .
   *
   * @param name foo
   * @param type foo
   */
  public DummyNativeFunction(String name, FunctionType type) {
    super(name, Scope.NULL, type);
  }

  @Override
  public Object call(DSLInterpreter interperter, List<Node> parameters) {
    return null;
  }

  @Override
  public ICallable.Type getCallableType() {
    return ICallable.Type.Native;
  }
}
