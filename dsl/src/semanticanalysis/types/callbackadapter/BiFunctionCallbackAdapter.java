package semanticanalysis.types.callbackadapter;

import interpreter.DSLInterpreter;

import runtime.RuntimeEnvironment;

import semanticanalysis.ICallable;

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
