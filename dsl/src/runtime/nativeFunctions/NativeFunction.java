package runtime.nativeFunctions;

import semanticAnalysis.ICallable;
import semanticAnalysis.IScope;
import semanticAnalysis.ScopedSymbol;
import semanticAnalysis.types.FunctionType;

public abstract class NativeFunction extends ScopedSymbol implements ICallable {
    protected NativeFunction(String name, IScope parentScope, FunctionType type) {
        super(name, parentScope, type);
    }

    public void overwriteFunctionType(FunctionType type) {
        this.dataType = type;
    }
}
