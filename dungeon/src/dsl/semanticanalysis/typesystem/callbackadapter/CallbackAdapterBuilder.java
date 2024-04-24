package dsl.semanticanalysis.typesystem.callbackadapter;

import dsl.interpreter.DSLInterpreter;
import dsl.runtime.callable.ICallable;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;

/**
 * Builder class for {@link CallbackAdapter}. Stores a reference to the {@link DSLInterpreter},
 * because it is needed to actually perform the execution of a callback defined in the DSL.
 */
public class CallbackAdapterBuilder {
  DSLInterpreter interpreter;

  /**
   * WTF? .
   *
   * @param interpreter foo
   */
  public CallbackAdapterBuilder(DSLInterpreter interpreter) {
    this.interpreter = interpreter;
  }

  /**
   * Build a {@link CallbackAdapter} for a concrete DSL function for assigning to a callback-Field
   * in a Component of the Dungeons ECS.
   *
   * @param callable The {@link ICallable} representing the function
   * @return The created {@link CallbackAdapter}
   */
  public CallbackAdapter buildAdapter(ICallable callable) {
    FunctionType functionType = callable.getFunctionType();
    if (functionType.getReturnType() != BuiltInType.noType
        && functionType.getParameterTypes().size() == 1) {
      return new FunctionCallbackAdapter(
          interpreter.getRuntimeEnvironment(), callable, interpreter);
    } else if (functionType.getReturnType() != BuiltInType.noType
        && functionType.getParameterTypes().size() == 2) {
      return new BiFunctionCallbackAdapter(
          interpreter.getRuntimeEnvironment(), callable, interpreter);
    } else {
      return new CallbackAdapter(interpreter.getRuntimeEnvironment(), callable, interpreter);
    }
  }
}
