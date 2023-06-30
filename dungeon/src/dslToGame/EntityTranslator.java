package dslToGame;

import core.Component;
import core.Entity;

import interpreter.DSLInterpreter;

import runtime.AggregateValue;
import runtime.EncapsulatedObject;
import runtime.IEvironment;
import runtime.IMemorySpace;

import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.TypeBuilder;

import java.util.List;

public class EntityTranslator implements IRuntimeObjectTranslator<Entity, AggregateValue> {
    @Override
    public AggregateValue translate(
            Entity object,
            IEvironment env,
            IMemorySpace parentMemorySpace,
            DSLInterpreter interpreter) {
        // get datatype for entity
        var entityType = env.getGlobalScope().resolve("entity");

        if (!(entityType instanceof AggregateType)) {
            throw new RuntimeException("The resolved symbol for 'entity' is not an AggregateType!");
        } else {
            // create aggregateValue
            var value = new AggregateValue((AggregateType) entityType, parentMemorySpace, object);

            // get components
            // TODO: use stream better
            List<Component> componentTypes = object.componentStream().toList();

            // TODO: translate components into DSL-objects
            //  this probably could be done by encapsulating objects with a previos check
            //  for type availability
            var globalScope = interpreter.getRuntimeEnvironment().getSymbolTable().getGlobalScope();
            for (var component : componentTypes) {
                String componentDSLName = TypeBuilder.getDSLName(component.getClass());
                var componentDSLType = globalScope.resolve(componentDSLName);

                if (componentDSLType != Symbol.NULL) {
                    // TODO: casting to AggregateType here is probably not safe
                    //  -> was passiert, wenn das hier PODAdapted ist?

                    var encapsulatedObject =
                            new EncapsulatedObject(
                                    component,
                                    (AggregateType) componentDSLType,
                                    value.getMemorySpace(),
                                    null);
                    AggregateValue aggregateMemberValue =
                            new AggregateValue(
                                    (AggregateType) componentDSLType,
                                    value.getMemorySpace(),
                                    component);
                    aggregateMemberValue.setMemorySpace(encapsulatedObject);

                    value.getMemorySpace().bindValue(componentDSLName, aggregateMemberValue);
                }
            }

            return value;
        }
    }
}
