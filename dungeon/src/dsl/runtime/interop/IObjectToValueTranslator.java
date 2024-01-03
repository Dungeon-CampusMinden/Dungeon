package dsl.runtime.interop;

import dsl.runtime.memoryspace.IMemorySpace;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;

/**
 * Interface for a specific translation from Java-Object to DSL-Value, i.e. translation from
 * Java-Class into DSL-typesystem
 */
public interface IObjectToValueTranslator {
  /**
   * Translate an Object into the DSL-typesystem. This requires the Class of the passed Object to be
   * registered as a DSL-type in the given environment, which can be achieved by calling
   * `createTypeFromClass` of the {@link TypeBuilder} of the {@link IEnvironment} and calling
   * `loadType` of the environment with the created type afterwards.
   *
   * @param object the Object to translate
   * @param parentMemorySpace the {@link IMemorySpace} in which the translated Value should be
   *     created
   * @param environment the {@link IEnvironment} to use for resolving type names
   * @return the translated object as a Value
   */
  Value translate(Object object, IMemorySpace parentMemorySpace, IEnvironment environment);
}
