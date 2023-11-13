package dsl.runtime.nativefunctions;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.Value;
import dsl.semanticanalysis.ICallable;
import dsl.semanticanalysis.IScope;
import dsl.semanticanalysis.Scope;
import dsl.semanticanalysis.Symbol;
import dsl.semanticanalysis.types.BuiltInType;
import dsl.semanticanalysis.types.FunctionType;

import java.util.List;

// TODO: set FunctionType as datatype for this
// public class NativePrint extends ScopedSymbol implements ICallable {
public class NativePrint extends NativeFunction {
    public static NativePrint func = new NativePrint(Scope.NULL);

    /**
     * Constructor
     *
     * @param parentScope parent scope of this function
     */
    private NativePrint(IScope parentScope) {
        super("print", parentScope, new FunctionType(BuiltInType.noType, BuiltInType.stringType));

        // bind parameters
        Symbol param = new Symbol("param", this, BuiltInType.stringType);
        this.bind(param);
    }

    @Override
    public Object call(DSLInterpreter interperter, List<Node> parameters) {
        assert parameters != null && parameters.size() > 0;
        try {
            Value param = (Value) parameters.get(0).accept(interperter);
            String paramAsString = param.toString();
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
