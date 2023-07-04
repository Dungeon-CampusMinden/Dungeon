package dslToGame;

import core.Entity;

import runtime.*;

import semanticanalysis.types.AggregateType;
import semanticanalysis.types.TypeBuilder;

public class EntityTranslator implements IObjectToValueTranslator {
    public static EntityTranslator instance = new EntityTranslator();

    private EntityTranslator() {}

    @Override
    public Value translate(Object object, IMemorySpace parentMemorySpace, IEvironment environment) {
        var entity = (Entity) object;
        // get datatype for entity
        var entityType = environment.getGlobalScope().resolve("entity");

        if (!(entityType instanceof AggregateType)) {
            throw new RuntimeException("The resolved symbol for 'entity' is not an AggregateType!");
        } else {
            // create aggregateValue for entity
            var value = new AggregateValue((AggregateType) entityType, parentMemorySpace, entity);

            entity.componentStream()
                    .forEach(
                            (component) -> {
                                var aggregateMemberValue =
                                        environment
                                                .getRuntimeObjectTranslator()
                                                .translateRuntimeObject(
                                                        component,
                                                        value.getMemorySpace(),
                                                        environment);

                                if (aggregateMemberValue != Value.NONE) {
                                    String componentDSLName =
                                            TypeBuilder.getDSLName(component.getClass());
                                    value.getMemorySpace()
                                            .bindValue(componentDSLName, aggregateMemberValue);
                                }
                            });
            return value;
        }
    }
}
