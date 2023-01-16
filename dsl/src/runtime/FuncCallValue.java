package runtime;

import semanticAnalysis.types.IType;

public class FuncCallValue extends Value {
    int functionSymbolIdx;

    /**
     * @return index of the symbol representing the function definition, which is called by this
     *     Value
     */
    public int getFunctionSymbolIdx() {
        return functionSymbolIdx;
    }

    /**
     * Constructor
     *
     * @param functionReturnValue {@link IType} representing the return value of the called function
     * @param functionSymbolIdx index of the symbol which represents the function definition to call
     */
    public FuncCallValue(IType functionReturnValue, int functionSymbolIdx) {
        super(functionReturnValue, null);
        this.functionSymbolIdx = functionSymbolIdx;
    }

    @Override
    public Object clone() {
        return new FuncCallValue(this.dataType, this.functionSymbolIdx);
    }
}
