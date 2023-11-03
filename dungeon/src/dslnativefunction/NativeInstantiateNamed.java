package dslnativefunction;

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
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.FunctionType;
import semanticanalysis.types.TypeInstantiator;

import java.util.List;

public class NativeInstantiateNamed extends NativeFunction {
    public static NativeInstantiateNamed func = new NativeInstantiateNamed(Scope.NULL);

    /**
     * Constructor
     *
     * @param parentScope parent scope of this function
     */
    private NativeInstantiateNamed(IScope parentScope) {
        super(
                "instantiate_named",
                parentScope,
                new FunctionType(BuiltInType.noType, Prototype.PROTOTYPE, BuiltInType.stringType));
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
        assert parameters != null && parameters.size() > 0;

        RuntimeEnvironment rtEnv = interpreter.getRuntimeEnvironment();

        var parameterValues = interpreter.evaluateNodes(parameters);
        Value prototypeValue = parameterValues.get(0);
        Value nameValue = parameterValues.get(1);

        if (prototypeValue.getDataType() != Prototype.PROTOTYPE) {
            throw new RuntimeException(
                    "Wrong type ('"
                            + prototypeValue.getDataType().getName()
                            + "') of parameter for call of instantiate()!");
        } else {
            var dslEntityInstance =
                    (AggregateValue) interpreter.instantiateDSLValue((Prototype) prototypeValue);
            var entityType = (AggregateType) rtEnv.getGlobalScope().resolve("entity");
            var entityObject =
                    (core.Entity)
                            interpreter.instantiateRuntimeValue(dslEntityInstance, entityType);
            entityObject.name(nameValue.toString());

            TypeInstantiator instantiator =
                    interpreter.getRuntimeEnvironment().getTypeInstantiator();

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
