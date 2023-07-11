package semanticanalysis.types.CallbackAdapter;

import semanticanalysis.FunctionSymbol;

public interface ICallbackAdapterBuilder {
    ICallbackAdapter buildAdapter(FunctionSymbol functionSymbol);
}
