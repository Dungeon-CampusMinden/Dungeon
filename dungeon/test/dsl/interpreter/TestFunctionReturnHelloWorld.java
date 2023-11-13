package dsl.interpreter;

import dsl.parser.ast.Node;
import dsl.runtime.nativefunctions.NativeFunction;
import dsl.semanticanalysis.ICallable;
import dsl.semanticanalysis.IScope;
import dsl.semanticanalysis.Scope;
import dsl.semanticanalysis.types.BuiltInType;
import dsl.semanticanalysis.types.FunctionType;

import java.util.List;

public class TestFunctionReturnHelloWorld extends NativeFunction {
    public static TestFunctionReturnHelloWorld func = new TestFunctionReturnHelloWorld(Scope.NULL);

    /**
     * Constructor
     *
     * @param parentScope parent scope of this function
     */
    private TestFunctionReturnHelloWorld(IScope parentScope) {
        super("testReturnHelloWorld", parentScope, new FunctionType(BuiltInType.stringType));
    }

    @Override
    public Object call(DSLInterpreter interperter, List<Node> parameters) {
        assert parameters != null && parameters.size() == 0;
        return "Hello, World!";
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.Native;
    }
}
