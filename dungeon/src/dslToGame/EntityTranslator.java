package dslToGame;

import core.Entity;

import interpreter.DSLInterpreter;

import runtime.*;

import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.TypeBuilder;

public class EntityTranslator implements IRuntimeObjectTranslator {
    @Override
    public Value translate(
            Object object,
            IEvironment env,
            IMemorySpace parentMemorySpace,
            DSLInterpreter interpreter) {
        var entity = (Entity) object;
        // get datatype for entity
        var entityType = env.getGlobalScope().resolve("entity");

        if (!(entityType instanceof AggregateType)) {
            throw new RuntimeException("The resolved symbol for 'entity' is not an AggregateType!");
        } else {
            // create aggregateValue for entity
            var value = new AggregateValue((AggregateType) entityType, parentMemorySpace, entity);
            var globalScope = interpreter.getRuntimeEnvironment().getSymbolTable().getGlobalScope();

            entity.componentStream()
                    .forEach(
                            (component) -> {
                                String componentDSLName =
                                        TypeBuilder.getDSLName(component.getClass());
                                var componentDSLType = globalScope.resolve(componentDSLName);

                                if (componentDSLType != Symbol.NULL) {
                                    // TODO: casting to AggregateType here is probably not safe
                                    //  -> was passiert, wenn das hier PODAdapted ist?

                                    // encapsulate the component
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

                                    value.getMemorySpace()
                                            .bindValue(componentDSLName, aggregateMemberValue);
                                }
                            });
            return value;
        }
    }
}
