package runtime.nativeFunctions;

import interpreter.DSLInterpreter;
import symboltable.*;

import java.util.List;

// TODO: how to enable semantic analysis for this? e.g. parameter-count, etc.
public class NativePrint extends ScopedSymbol implements ICallable {
    public NativePrint(IScope parentScope) {
        super("print", parentScope, BuiltInType.intType);

        // bind parameters
        Symbol param = new Symbol("param", this, BuiltInType.stringType);
        this.Bind(param);
    }

    @Override
    public Object call(DSLInterpreter interperter, List<Object> parameters) {
        assert parameters != null && parameters.size() > 0;
        try {
            String paramAsString = (String)parameters.get(0);
            System.out.println(paramAsString);
        } catch (ClassCastException ex) {
            // TODO: handle
        }
        return null;
    }
}
