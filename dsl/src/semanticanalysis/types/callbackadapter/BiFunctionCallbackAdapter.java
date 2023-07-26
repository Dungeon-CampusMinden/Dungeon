package semanticanalysis.types.callbackadapter;

import interpreter.DSLInterpreter;
import runtime.RuntimeEnvironment;
import semanticanalysis.FunctionSymbol;

import java.util.function.BiFunction;

public class BiFunctionCallbackAdapter extends CallbackAdapter implements BiFunction {

    BiFunctionCallbackAdapter(RuntimeEnvironment rtEnv, FunctionSymbol functionSymbol, DSLInterpreter interpreter) {
        super(rtEnv, functionSymbol, interpreter);
    }

    @Override
    public Object apply(Object o, Object o2) {
        return this.call(o, o2);
    }
}
