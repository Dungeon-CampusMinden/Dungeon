package dsl.semanticanalysis.typesystem.callbackadapter;

import dsl.interpreter.DSLInterpreter;
import dsl.runtime.callable.ICallable;
import dsl.runtime.environment.RuntimeEnvironment;
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
