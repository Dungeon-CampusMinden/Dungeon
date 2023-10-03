package dsltypeproperties;

import core.Entity;
import runtime.*;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;
import task.Task;

public class TaskTranslator implements IObjectToValueTranslator {
    public static TaskTranslator instance = new TaskTranslator();

    private TaskTranslator() {}

    /**
     * Iterates over all components of the passed Entity, resolves the name of the component in
     * passed IEnvironment, translates each component (for which the type can be resolved) into a
     * DSL-Value and binds the Component-Value in the MemorySpace of the Entity-Value.
     *
     * @param object The {@link Task} instance to translate
     * @param parentMemorySpace The {@link IMemorySpace} in which the translated value should be
     *     created
     * @param environment The {@link IEvironment}, which will be used to resolve types
     * @return The translated value
     */
    @Override
    public Value translate(Object object, IMemorySpace parentMemorySpace, IEvironment environment) {
        Class<?> clazz = object.getClass();
        if (!Task.class.isAssignableFrom(clazz)) {
            return Value.NONE;
        }

        var task = (Task) object;

        // get type of task
        IType type = environment.getTypeBuilder().createDSLTypeForJavaTypeInScope(environment.getGlobalScope(), clazz);
        return Value.NONE;
    }
}
