package runtime;

/**
 * Interface for a specific translation from Java-Object to DSL-Value, i.e. translation from
 * Java-Class into DSL-typesystem
 */
public interface IObjectToValueTranslator {
    /**
     * Translate an Object into the DSL-typesystem. This requires the Class of the passed Object to
     * be registered as a DSL-type in the given environment, which can be achieved by calling
     * `createTypeFromClass` of the {@link semanticanalysis.types.TypeBuilder} of the {@link
     * IEvironment} and calling `loadType` of the environment with the created type afterwards.
     *
     * @param object the Object to translate
     * @param parentMemorySpace the {@link IMemorySpace} in which the translated Value should be
     *     created
     * @param environment the {@link IEvironment} to use for resolving type names
     * @return the translated object as a Value
     */
    Value translate(Object object, IMemorySpace parentMemorySpace, IEvironment environment);
}
