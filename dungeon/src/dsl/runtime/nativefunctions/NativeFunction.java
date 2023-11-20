package dsl.runtime.nativefunctions;

import dsl.semanticanalysis.ICallable;
import dsl.semanticanalysis.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.types.FunctionType;
import dsl.semanticanalysis.types.IType;

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
