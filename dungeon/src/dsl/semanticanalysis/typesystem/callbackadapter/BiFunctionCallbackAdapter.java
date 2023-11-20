package dsl.semanticanalysis.typesystem.callbackadapter;

import dsl.interpreter.DSLInterpreter;
import dsl.runtime.environment.RuntimeEnvironment;
import dsl.runtime.callable.ICallable;

import java.util.function.BiFunction;

public class BiFunctionCallbackAdapter extends CallbackAdapter implements BiFunction {

    BiFunctionCallbackAdapter(
            RuntimeEnvironment rtEnv, ICallable callable, DSLInterpreter interpreter) {
        super(rtEnv, callable, interpreter);
    }

    @Override
    public Object apply(Object o, Object o2) {
        return this.call(o, o2);
    }
}
