package interpreter.mockecs;

import runtime.*;
import runtime.IObjectToValueTranslator;

import semanticanalysis.IScope;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.TypeBuilder;

public class MockEntityTranslator implements IObjectToValueTranslator {
    public static MockEntityTranslator instance = new MockEntityTranslator();

    private MockEntityTranslator() {}

    @Override
    public Value translate(
            Object object,
            IScope globalScope,
            IMemorySpace parentMemorySpace,
            IEvironment environment) {
        Entity entity = (Entity) object;
        // get datatype for entity
        var entityType = globalScope.resolve("entity");

        if (!(entityType instanceof AggregateType)) {
            throw new RuntimeException("The resolved symbol for 'entity' is not an AggregateType!");
        } else {
            // create aggregateValue for entity
            var value = new AggregateValue((AggregateType) entityType, parentMemorySpace, entity);

            for (var component : entity.components) {
                var aggregateMemberValue =
                        environment
                                .getRuntimeObjectTranslator()
                                .translateRuntimeObject(
                                        component,
                                        globalScope,
                                        value.getMemorySpace(),
                                        environment);
                if (aggregateMemberValue != Value.NONE) {
                    String componentDSLName = TypeBuilder.getDSLName(component.getClass());
                    value.getMemorySpace().bindValue(componentDSLName, aggregateMemberValue);
                }
            }
            return value;
        }
    }
}
