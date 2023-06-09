package interpreter;

import parser.ast.Node;
import runtime.Value;
import runtime.nativefunctions.NativeFunction;
import semanticanalysis.ICallable;
import semanticanalysis.IScope;
import semanticanalysis.Scope;
import semanticanalysis.Symbol;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.FunctionType;

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

        // bind parameters
        Symbol param = new Symbol("param", this, BuiltInType.stringType);
        this.bind(param);
    }

    @Override
    public Object call(DSLInterpreter interperter, List<Node> parameters) {
        assert parameters != null && parameters.size() > 0;
        try {
            Value param = (Value) parameters.get(0).accept(interperter);
            String paramAsString = (String) param.getInternalObject();
            System.out.println(paramAsString);
        } catch (ClassCastException ex) {
            // TODO: handle.. although this should not be a problem because
            //  of typechecking, once it is impelemented
        }
        return null;
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.Native;
    }
}
