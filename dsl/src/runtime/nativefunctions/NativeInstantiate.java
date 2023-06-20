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
        super(
                "instantiate",
                parentScope,
                new FunctionType(BuiltInType.noType, BuiltInType.stringType));

        // bind parameters
        Symbol param = new Symbol("param", this, EntityType.ENTITY_TYPE);
        this.bind(param);
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
        assert parameters != null && parameters.size() > 0;

        Value param = (Value) parameters.get(0).accept(interpreter);
        if (param.getDataType() != EntityType.ENTITY_TYPE) {
            throw new RuntimeException("Wrong type ('" +param.getDataType().getName() + "') of parameter for call of instantiate()!");
        } else {
            var dslEntityInstance = (AggregateValue) interpreter.instantiateDSLValue((EntityType)param);
            var entityType =
                    (AggregateType)
                            interpreter
                                    .getRuntimeEnvironment()
                                    .getGlobalScope()
                                    .resolve("entity");
            return interpreter.instantiateRuntimeValue(dslEntityInstance, entityType);
        }
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.Native;
    }
}
