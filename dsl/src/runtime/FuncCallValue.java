package runtime;

import semanticAnalysis.types.IType;

public class FuncCallValue extends Value {
    int functionSymbolIdx;

    public int getFunctionSymbolIdx() {
        return functionSymbolIdx;
    }

    public FuncCallValue(IType functionReturnValue, int functionSymbolIdx) {
        super(functionReturnValue, null);
        this.functionSymbolIdx = functionSymbolIdx;
    }

    @Override
    public Object clone() {
        return new FuncCallValue(this.dataType, this.functionSymbolIdx);
    }
}
