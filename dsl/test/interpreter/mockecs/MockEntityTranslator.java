package interpreter.mockecs;

import runtime.IObjectToValueTranslator;

import runtime.*;

import semanticanalysis.IScope;
import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.TypeBuilder;

public class MockEntityTranslator implements IObjectToValueTranslator {
    public static MockEntityTranslator instance = new MockEntityTranslator();

    private MockEntityTranslator() {}

    @Override
    public Value translate(Object object, IScope globalScope, IMemorySpace parentMemorySpace) {
        Entity entity = (Entity) object;
        // get datatype for entity
        var entityType = globalScope.resolve("entity");

        if (!(entityType instanceof AggregateType)) {
            throw new RuntimeException("The resolved symbol for 'entity' is not an AggregateType!");
        } else {
            // create aggregateValue for entity
            var value = new AggregateValue((AggregateType) entityType, parentMemorySpace, entity);

            for (var component : entity.components) {
                String componentDSLName = TypeBuilder.getDSLName(component.getClass());
                var componentDSLType = globalScope.resolve(componentDSLName);

                if (componentDSLType != Symbol.NULL) {
                    // TODO: casting to AggregateType here is probably not safe
                    //  -> was passiert, wenn das hier PODAdapted ist?

                    // encapsulate the component
                    var encapsulatedObject =
                            new EncapsulatedObject(
                                    component,
                                    (AggregateType) componentDSLType,
                                    value.getMemorySpace());
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
