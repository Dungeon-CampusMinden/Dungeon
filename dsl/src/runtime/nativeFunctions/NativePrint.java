package runtime.nativeFunctions;

import interpreter.DSLInterpreter;
import java.util.List;
import parser.AST.Node;
import runtime.Value;
import semanticAnalysis.ICallable;
import semanticAnalysis.IScope;
import semanticAnalysis.Scope;
import semanticAnalysis.ScopedSymbol;
import semanticAnalysis.Symbol;
import semanticAnalysis.types.BuiltInType;

public class NativePrint extends ScopedSymbol implements ICallable {
    public static NativePrint func = new NativePrint(Scope.NULL);

    /**
     * Constructor
     *
     * @param parentScope parent scope of this function
     */
    private NativePrint(IScope parentScope) {
        super("print", parentScope, BuiltInType.intType);

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
