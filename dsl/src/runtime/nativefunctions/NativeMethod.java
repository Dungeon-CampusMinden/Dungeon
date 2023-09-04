package runtime.nativefunctions;

import interpreter.DSLInterpreter;

import parser.ast.Node;

import runtime.Value;

import semanticanalysis.ICallable;
import semanticanalysis.IInstanceCallable;
import semanticanalysis.IScope;
import semanticanalysis.Symbol;
import semanticanalysis.types.FunctionType;

import java.util.List;

public class NativeMethod extends Symbol implements ICallable {
    private final IInstanceCallable instanceCallable;

    public NativeMethod(
            String name,
            IScope parentScope,
            FunctionType functionType,
            IInstanceCallable callable) {
        super(name, parentScope, functionType);
        this.instanceCallable = callable;
    }

    @Override
    public Object call(DSLInterpreter interperter, List<Node> parameters) {
        // resolve "THIS_VALUE"
        Value instance = interperter.getCurrentMemorySpace().resolve(Value.THIS_NAME);
        return this.instanceCallable.call(interperter, instance, parameters);
    }

    @Override
    public ICallable.Type getCallableType() {
        return null;
    }

    @Override
    public FunctionType getFunctionType() {
        return null;
    }
}
