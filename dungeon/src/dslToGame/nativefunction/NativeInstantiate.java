package dslToGame.nativefunction;

import core.Component;
import core.Entity;

import interpreter.DSLInterpreter;

import parser.ast.Node;

import runtime.AggregateValue;
import runtime.Prototype;
import runtime.RuntimeEnvironment;
import runtime.Value;
import runtime.nativefunctions.NativeFunction;

import semanticanalysis.ICallable;
import semanticanalysis.IScope;
import semanticanalysis.Scope;
import semanticanalysis.Symbol;
import semanticanalysis.types.*;

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
        Symbol param = new Symbol("param", this, Prototype.PROTOTYPE);
        this.bind(param);
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
        assert parameters != null && parameters.size() > 0;

        RuntimeEnvironment rtEnv = interpreter.getRuntimeEnvironment();
        Value param = (Value) parameters.get(0).accept(interpreter);
        if (param.getDataType() != Prototype.PROTOTYPE) {
            throw new RuntimeException(
                    "Wrong type ('"
                            + param.getDataType().getName()
                            + "') of parameter for call of instantiate()!");
        } else {
            var dslEntityInstance =
                    (AggregateValue) interpreter.instantiateDSLValue((Prototype) param);
            var entityType = (AggregateType) rtEnv.getGlobalScope().resolve("entity");
            var entityObject = interpreter.instantiateRuntimeValue(dslEntityInstance, entityType);

            TypeInstantiator instantiator =
                    interpreter.getRuntimeEnvironment().getTypeInstantiator();

            // TODO: for testing
            String contextName = "entity";
            instantiator.pushContextMember(contextName, entityObject);

            for (var valueEntry : dslEntityInstance.getMemorySpace().getValueSet()) {
                if (valueEntry.getKey().equals(Value.THIS_NAME)) {
                    continue;
                }
                Value memberValue = valueEntry.getValue();
                if (memberValue instanceof AggregateValue) {
                    // TODO: this is needed, because Prototype does not extend AggregateType
                    // currently,
                    //  which should be fixed
                    AggregateType membersOriginalType =
                            interpreter.getOriginalTypeOfPrototype(
                                    (Prototype) memberValue.getDataType());

                    // instantiate object as a new java Object
                    Object memberObject =
                            interpreter.instantiateRuntimeValue(
                                    (AggregateValue) memberValue, membersOriginalType);
                    try {
                        Component component = (Component) memberObject;
                        Entity entity = (Entity) entityObject;
                        entity.addComponent(component);
                    } catch (ClassCastException ex) {
                        //
                    }
                }
            }

            instantiator.removeContextMember(contextName);

            return rtEnv.translateRuntimeObject(entityObject, interpreter.getCurrentMemorySpace());
        }
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.Native;
    }
}
