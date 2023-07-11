package semanticanalysis.types.CallbackAdapter;

import interpreter.DSLInterpreter;
import runtime.IMemorySpace;
import runtime.RuntimeEnvironment;
import semanticanalysis.FunctionSymbol;

public interface ICallbackAdapter {

    Object call(Object ... params);
}
