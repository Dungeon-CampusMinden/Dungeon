package dsl.semanticanalysis.typesystem.typebuilding;

import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.lang.reflect.ParameterizedType;

/** Builder interface for a {@link FunctionType} for a callback method. */
public interface IFunctionTypeBuilder {
  /**
   * Build a {@link FunctionType} representing the signature of a callback.
   *
   * @param parameterizedFunctionType the parameterized type (with generic type information) of the
   *     function type to build
   * @param typeBuilder {@link TypeBuilder} instance to lookup parameter types
   * @param globalScope the global {@link IScope} of the {@link IEnvironment} in which to build the
   *     function type.
   * @return {@link FunctionType} corresponding to the callback signature
   */
  FunctionType buildFunctionType(
      ParameterizedType parameterizedFunctionType, TypeBuilder typeBuilder, IScope globalScope);
}
