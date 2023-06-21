package dslToGame;

import core.Component;
import core.Entity;

import interpreter.DSLInterpreter;
import runtime.AggregateValue;
import runtime.IEvironment;
import runtime.IMemorySpace;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.IType;

import java.util.ArrayList;
import java.util.HashSet;

public class EntityTranslator implements IRuntimeObjectTranslator<Entity, AggregateValue> {
    @Override
    public AggregateValue translate(Entity object, IEvironment env, IMemorySpace parentMemorySpace, DSLInterpreter interpreter) {
        // get datatype for entity
        var entityType = env.getGlobalScope().resolve("entity");

        if (!(entityType instanceof AggregateType)) {
            throw new RuntimeException("The resolved symbol for 'entity' is not an AggregateType!");
        } else {
            // create aggregateValue
            var value = new AggregateValue((AggregateType)entityType, parentMemorySpace, object);

            // check for presence of components
            var types = env.getTypes();
            ArrayList<AggregateType> componentTypes = new ArrayList<>();
            for (var type : types) {
                if (type.getTypeKind() == IType.Kind.Aggregate ||
                    type.getTypeKind() == IType.Kind.PODAdapted ||
                    type.getTypeKind() == IType.Kind.AggregateAdapted) {
                    if (type instanceof AggregateType) {
                        var aggrType = (AggregateType) type;
                        var originType = aggrType.getOriginType();
                        var superClass = originType.getSuperclass();
                        if (superClass.equals(Component.class)) {
                            componentTypes.add(aggrType);
                        }
                    }
                }
            }

            for (var componentType : componentTypes) {
                var originType = componentType.getOriginType();
                if (object.isPresent((Class<? extends Component>) originType)) {
                    // add aggregate value for component in memoryspace of entity
                    // TODO: this probably needs a translator for each component..
                    boolean b = true;
                }
            }
        }

        return null;
    }
}
