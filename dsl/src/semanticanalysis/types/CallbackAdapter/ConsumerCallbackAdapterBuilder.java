package semanticanalysis.types.CallbackAdapter;

import interpreter.DSLInterpreter;
import runtime.IMemorySpace;
import semanticanalysis.FunctionSymbol;

public class ConsumerCallbackAdapterBuilder implements ICallbackAdapterBuilder {
    DSLInterpreter interpreter;

    public ConsumerCallbackAdapterBuilder(DSLInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public ICallbackAdapter buildAdapter(FunctionSymbol functionSymbol, IMemorySpace parentMemorySpace) {
        return new ConsumerCallbackAdapter(interpreter.getRuntimeEnvironment(), functionSymbol, parentMemorySpace, interpreter);
    }
}
