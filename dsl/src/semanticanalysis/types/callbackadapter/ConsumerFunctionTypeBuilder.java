package semanticanalysis.types.callbackadapter;

import semanticanalysis.IScope;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.FunctionType;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Builder for a {@link FunctionType} for a callback defined by the {@link
 * java.util.function.Consumer} interface
 */
public class ConsumerFunctionTypeBuilder implements IFunctionTypeBuilder {

    public static ConsumerFunctionTypeBuilder instance = new ConsumerFunctionTypeBuilder();

    private ConsumerFunctionTypeBuilder() {}

    @Override
    public FunctionType buildFunctionType(
        Type genericType, TypeBuilder typeBuilder, IScope globalScope) {

        var parameterizedType = (ParameterizedType) genericType;
        // the parameters will be the arguments for the function
        ArrayList<IType> parameterTypes =
                new ArrayList<>(parameterizedType.getActualTypeArguments().length);
        for (var parameterType : parameterizedType.getActualTypeArguments()) {
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
