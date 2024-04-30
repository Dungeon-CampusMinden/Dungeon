package dslinterop.dslnativefunction;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.callable.ICallable;
import dsl.runtime.callable.NativeFunction;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.List;

/** WTF? . */
public class NativePrint extends NativeFunction {
  /** Instance of this class. */
  public static NativePrint func = new NativePrint(Scope.NULL);

  /**
   * Constructor.
   *
   * @param parentScope parent scope of this function
   */
  private NativePrint(IScope parentScope) {
    super("print", parentScope, new FunctionType(BuiltInType.noType, BuiltInType.stringType));

    // bind parameters
    Symbol param = new Symbol("param", this, BuiltInType.stringType);
    this.bind(param);
  }

  @Override
  public Object call(DSLInterpreter interperter, List<Node> parameters) {
    assert parameters != null && parameters.size() > 0;
    try {
      Value param = (Value) parameters.get(0).accept(interperter);
      String paramAsString = param.toString();
      System.out.println(paramAsString);
    } catch (ClassCastException ex) {
      // TODO: handle.. although this should not be a problem because
      //  of typechecking, once it is impelemented
    }
    return null;
  }

  @Override
  public ICallable.Type getCallableType() {
    return ICallable.Type.Native;
  }
}
