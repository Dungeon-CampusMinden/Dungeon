package semanticanalysis.types.CallbackAdapter;

import runtime.IMemorySpace;
import semanticanalysis.FunctionSymbol;

public interface ICallbackAdapterBuilder {
    ICallbackAdapter buildAdapter(
        FunctionSymbol functionSymbol,
        IMemorySpace parentMemorySpace);
}
