package semanticanalysis.types.CallbackAdapter;

import interpreter.DSLInterpreter;
import runtime.IMemorySpace;
import semanticanalysis.FunctionSymbol;

public class FunctionCallbackAdapterBuilder implements ICallbackAdapterBuilder {
    public DSLInterpreter interpreter;

    public FunctionCallbackAdapterBuilder(DSLInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public ICallbackAdapter buildAdapter(FunctionSymbol functionSymbol) {
        return new FunctionCallbackAdapter(interpreter.getRuntimeEnvironment(), functionSymbol, interpreter);
    }
}
