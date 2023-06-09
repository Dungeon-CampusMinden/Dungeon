package runtime.nativefunctions;

import interpreter.DSLInterpreter;
import parser.ast.Node;
import runtime.AggregateValue;
import runtime.EntityType;
import runtime.Value;
import semanticanalysis.ICallable;
import semanticanalysis.IScope;
import semanticanalysis.Scope;
import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.FunctionType;

import java.util.List;

public class NativeInstantiate extends NativeFunction {
    public static NativeInstantiate func = new NativeInstantiate(Scope.NULL);

    /**
     * Constructor
     *
     * @param parentScope parent scope of this function
     */
    private NativeInstantiate(IScope parentScope) {
        super("instantiate", parentScope, new FunctionType(BuiltInType.noType, BuiltInType.stringType));

        // bind parameters
        Symbol param = new Symbol("param", this, EntityType.ENTITY_TYPE);
        this.bind(param);
    }

    @Override
    public Object call(DSLInterpreter interperter, List<Node> parameters) {
        assert parameters != null && parameters.size() > 0;
        try {
            Value param = (Value) parameters.get(0).accept(interperter);
            if (param.getDataType() == EntityType.ENTITY_TYPE) {
                var entityType = (AggregateType) interperter.getRuntimeEnvironment().getGlobalScope().resolve("entity");
                return interperter.instantiateRuntimeValue((AggregateValue) param, entityType);
            }
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
