package dsl.interpreter;

import dsl.parser.ast.Node;
import dsl.runtime.callable.NativeFunction;
import dsl.runtime.callable.ICallable;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.types.FunctionType;

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
