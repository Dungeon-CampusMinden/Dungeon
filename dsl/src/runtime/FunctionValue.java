package runtime;

import semanticanalysis.ICallable;
import semanticanalysis.types.IType;

public class FunctionValue extends Value {
    ICallable callable;

    /**
     * @return index of the symbol representing the function definition, which is called by this
     *     Value
     */
    public ICallable getCallable() {
        return callable;
    }

    /**
     * Constructor
     *
     * @param functionReturnValue {@link IType} representing the return value of the called function
     * @param callable the callable represented by this value
     */
    public FunctionValue(IType functionReturnValue, ICallable callable) {
        super(functionReturnValue, null);
        this.callable = callable;
    }

    @Override
    public Object clone() {
        return new FunctionValue(this.dataType, this.callable);
    }
}
