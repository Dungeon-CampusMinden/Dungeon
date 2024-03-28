package dsl.semanticanalysis.typesystem.callbackadapter;

import dsl.interpreter.DSLInterpreter;
import dsl.runtime.callable.ICallable;
import dsl.runtime.environment.RuntimeEnvironment;
import java.util.function.BiFunction;

/** WTF? . */
public class BiFunctionCallbackAdapter extends CallbackAdapter implements BiFunction {

  /**
   * WTF? .
   *
   * @param rtEnv foo
   * @param callable foo
   * @param interpreter foo
   */
  BiFunctionCallbackAdapter(
      RuntimeEnvironment rtEnv, ICallable callable, DSLInterpreter interpreter) {
    super(rtEnv, callable, interpreter);
  }

  @Override
  public Object apply(Object o, Object o2) {
    return this.call(o, o2);
  }
}
