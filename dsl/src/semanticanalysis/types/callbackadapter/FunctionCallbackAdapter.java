package semanticanalysis.types.callbackadapter;

import interpreter.DSLInterpreter;

import runtime.RuntimeEnvironment;

import semanticanalysis.ICallable;

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
