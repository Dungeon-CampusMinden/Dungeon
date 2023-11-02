package runtime.nativefunctions;

import semanticanalysis.ICallable;
import semanticanalysis.IScope;
import semanticanalysis.ScopedSymbol;
import semanticanalysis.Symbol;
import semanticanalysis.types.FunctionType;
import semanticanalysis.types.IType;

public abstract class NativeFunction extends ScopedSymbol implements ICallable {
    protected NativeFunction(String name, IScope parentScope, FunctionType type) {
        super(name, parentScope, type);

        // create generically named parameter symbols
        for (int i = 0; i < type.getParameterTypes().size(); i++) {
            IType parameterType = type.getParameterTypes().get(i);
            String parameterName = "param" + i;
            Symbol parameterSymbol = new Symbol(parameterName, this, parameterType);
            this.bind(parameterSymbol);
        }
    }

    public void overwriteFunctionType(FunctionType type) {
        this.dataType = type;
    }

    @Override
    public FunctionType getFunctionType() {
        return (FunctionType) this.getDataType();
    }
}
