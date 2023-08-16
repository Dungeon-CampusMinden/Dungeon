package runtime.nativefunctions;

import semanticanalysis.ICallable;
import semanticanalysis.IScope;
import semanticanalysis.ScopedSymbol;
import semanticanalysis.types.FunctionType;

public abstract class NativeFunction extends ScopedSymbol implements ICallable {
    protected NativeFunction(String name, IScope parentScope, FunctionType type) {
        super(name, parentScope, type);
    }

    public void overwriteFunctionType(FunctionType type) {
        this.dataType = type;
    }
    @Override
    public FunctionType getFunctionType() {
        return (FunctionType) this.getDataType();
    }
}
