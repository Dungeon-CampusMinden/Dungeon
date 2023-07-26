package semanticanalysis.types.callbackadapter;

import interpreter.DSLInterpreter;

import runtime.RuntimeEnvironment;

import semanticanalysis.FunctionSymbol;

import java.util.function.Function;

public class FunctionCallbackAdapter extends CallbackAdapter implements Function {

    FunctionCallbackAdapter(
            RuntimeEnvironment rtEnv, FunctionSymbol functionSymbol, DSLInterpreter interpreter) {
        super(rtEnv, functionSymbol, interpreter);
    }

    @Override
    public Object apply(Object o) {
        return this.call(o);
    }
}
