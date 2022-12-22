package runtime.nativeFunctions;

import interpreter.DSLInterpreter;
import java.util.List;
import parser.AST.Node;
import semanticAnalysis.ICallable;
import semanticAnalysis.IScope;
import semanticAnalysis.Scope;
import semanticAnalysis.ScopedSymbol;
import semanticAnalysis.Symbol;
import semanticAnalysis.types.BuiltInType;

// TODO: how to enable semantic analysis for this? e.g. parameter-count, etc.
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
            String paramAsString = (String) parameters.get(0).accept(interperter);
            System.out.println(paramAsString);
        } catch (ClassCastException ex) {
            // TODO: handle
        }
        return null;
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.Native;
    }
}
