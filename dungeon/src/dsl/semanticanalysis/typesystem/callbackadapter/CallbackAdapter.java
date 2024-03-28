package dsl.semanticanalysis.typesystem.callbackadapter;

import core.utils.TriConsumer;
import dsl.interpreter.DSLInterpreter;
import dsl.runtime.callable.ICallable;
import dsl.runtime.environment.RuntimeEnvironment;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * WTF? erster Satz ist ein kurzer Satz! .
 *
 * <p>Encapsulates the {@link RuntimeEnvironment} and {@link DSLInterpreter} needed to execute a
 * callback-function defined in the DSL. Implements the functional interfaces needed for assigning
 * an instance of this class to the callback-fields in the components of the Dungeons ECS.
 */
public class CallbackAdapter implements Consumer, TriConsumer, BiConsumer {

  private final RuntimeEnvironment rtEnv;
  private final FunctionType functionType;
  private final ICallable callable;
  private final DSLInterpreter interpreter;

  /**
   * WTF? .
   *
   * @param rtEnv foo
   * @param callable foo
   * @param interpreter foo
   */
  CallbackAdapter(RuntimeEnvironment rtEnv, ICallable callable, DSLInterpreter interpreter) {
    this.rtEnv = rtEnv;
    this.functionType = callable.getFunctionType();
    this.callable = callable;
    this.interpreter = interpreter;
  }

  /**
   * WTF? .
   *
   * @param params foo
   * @return foo
   */
  public Object call(Object... params) {
    Value returnValue =
        (Value)
            interpreter.callCallableRawParameters(this.callable, Arrays.stream(params).toList());

    return convertValueToObject(returnValue);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public ICallable callable() {
    return this.callable;
  }

  protected Object convertValueToObject(Value value) {
    return this.rtEnv.getTypeInstantiator().instantiate(value);
  }

  // region interface implementation
  @Override
  public void accept(Object o) {
    this.call(o);
  }

  @Override
  public void accept(Object o, Object o2, Object o3) {
    this.call(o, o2, o3);
  }

  @Override
  public void accept(Object o, Object o2) {
    this.call(o, o2);
  }
  // endregion
}
