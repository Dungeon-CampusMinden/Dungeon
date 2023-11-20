package dsl.semanticanalysis.types.callbackadapter;

import dsl.interpreter.DSLInterpreter;
import dsl.runtime.environment.RuntimeEnvironment;
import dsl.runtime.callable.ICallable;

import java.util.function.Function;

public class FunctionCallbackAdapter extends CallbackAdapter implements Function {

    FunctionCallbackAdapter(
            RuntimeEnvironment rtEnv, ICallable callable, DSLInterpreter interpreter) {
        super(rtEnv, callable, interpreter);
    }

    @Override
    public Object apply(Object o) {
        return this.call(o);
    }
}
