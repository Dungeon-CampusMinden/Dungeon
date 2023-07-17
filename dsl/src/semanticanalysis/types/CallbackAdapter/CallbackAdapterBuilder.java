package semanticanalysis.types.CallbackAdapter;

import interpreter.DSLInterpreter;

import semanticanalysis.FunctionSymbol;

public class CallbackAdapterBuilder {
    DSLInterpreter interpreter;

    public CallbackAdapterBuilder(DSLInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    public CallbackAdapter buildAdapter(FunctionSymbol functionSymbol) {
        return new CallbackAdapter(
            interpreter.getRuntimeEnvironment(), functionSymbol, interpreter);
    }
}
