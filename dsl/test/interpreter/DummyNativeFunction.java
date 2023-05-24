package interpreter;

import parser.ast.Node;

import runtime.nativefunctions.NativeFunction;

import semanticanalysis.ICallable;
import semanticanalysis.Scope;
import semanticanalysis.types.FunctionType;

import java.util.List;

public class DummyNativeFunction extends NativeFunction {
    public DummyNativeFunction(String name, FunctionType type) {
        super(name, Scope.NULL, type);
    }

    @Override
    public Object call(DSLInterpreter interperter, List<Node> parameters) {
        return null;
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.Native;
    }
}
