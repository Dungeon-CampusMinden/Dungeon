package semanticanalysis.types;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Builder for a {@link FunctionType} for a callback defined by the {@link java.util.function.Function}
 * interface
 */
public class FunctionFunctionTypeBuilder implements IFunctionTypeBuilder {
    public static FunctionFunctionTypeBuilder instance = new FunctionFunctionTypeBuilder();

    private FunctionFunctionTypeBuilder() {
    }

    @Override
    public FunctionType buildFunctionType(Field field, TypeBuilder typeBuilder) {
        var genericType = field.getGenericType();

        var typeMap = typeBuilder.getJavaTypeToDSLTypeMap();

        var parameterizedType = (ParameterizedType) genericType;
        Type[] typeArray = parameterizedType.getActualTypeArguments();

        // the first type parameter of the Function<T,R> interface will correspond to
        // the type of the single parameter of the function
        IType parameterType = TypeBuilder.getBasicDSLType((Class<?>) typeArray[0]);
        if (null == parameterType) {
            parameterType = typeMap.get(typeArray[0]);
        }
        if (null == parameterType) {
            throw new RuntimeException("Type of parameter of Function could not be translated");
        }

        // the second type parameter of the Function<T,R> interface will correspond to
        // the return type of the function
        IType returnType = TypeBuilder.getBasicDSLType((Class<?>) typeArray[1]);
        if (null == returnType) {
            returnType = typeMap.get(typeArray[1]);
        }
        if (null == returnType) {
            throw new RuntimeException("Returntype of Function could not be translated");
        }
        return new FunctionType(returnType, parameterType);
    }
}
