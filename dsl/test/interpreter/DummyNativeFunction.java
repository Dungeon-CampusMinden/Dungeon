package interpreter;

import parser.AST.Node;

import runtime.nativeFunctions.NativeFunction;

import semanticAnalysis.ICallable;
import semanticAnalysis.Scope;
import semanticAnalysis.types.FunctionType;

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
