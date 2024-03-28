package dsl.semanticanalysis.typesystem.typebuilding;

import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

/**
 * Builder for a {@link FunctionType} for a callback defined by the {@link
 * java.util.function.Consumer} interface.
 */
public class ConsumerFunctionTypeBuilder implements IFunctionTypeBuilder {

  /** Singleton instance. */
  public static ConsumerFunctionTypeBuilder instance = new ConsumerFunctionTypeBuilder();

  private ConsumerFunctionTypeBuilder() {}

  @Override
  public FunctionType buildFunctionType(
      ParameterizedType parameterizedFunctionType, TypeBuilder typeBuilder, IScope globalScope) {

    // the parameters will be the arguments for the function
    ArrayList<IType> parameterTypes =
        new ArrayList<>(parameterizedFunctionType.getActualTypeArguments().length);
    for (var parameterType : parameterizedFunctionType.getActualTypeArguments()) {
      IType dslType = typeBuilder.createDSLTypeForJavaTypeInScope(globalScope, parameterType);
      if (null == dslType) {
        throw new RuntimeException("Type of parameter of Consumer could not be translated");
      } else {
        parameterTypes.add(dslType);
      }
    }
    return new FunctionType(BuiltInType.noType, parameterTypes);
  }
}
