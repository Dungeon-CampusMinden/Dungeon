package semanticanalysis.types.callbackadapter;

import semanticanalysis.IScope;
import semanticanalysis.types.FunctionType;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class BiFunctionFunctionTypeBuilder implements IFunctionTypeBuilder {

    public static BiFunctionFunctionTypeBuilder instance = new BiFunctionFunctionTypeBuilder();

    private BiFunctionFunctionTypeBuilder() {
    }

    @Override
    public FunctionType buildFunctionType(
        Field field, TypeBuilder typeBuilder, IScope globalScope) {
        var genericType = field.getGenericType();

        var parameterizedType = (ParameterizedType) genericType;
        Type[] typeArray = parameterizedType.getActualTypeArguments();

        // the first type parameter of the BiFunction<T,U,R> interface will correspond to
        // the type of the first parameter of the function
        Type firstParameterType = typeArray[0];
        IType firstParameterDSLType =
            typeBuilder.createDSLTypeForJavaTypeInScope(globalScope, firstParameterType);
        if (null == firstParameterDSLType) {
            throw new RuntimeException("Type of parameter of Function could not be translated");
        }

        // the second type parameter of the BiFunction<T,U,R> interface will correspond to
        // the type of the second parameter of the function
        Type secondParameterType = typeArray[1];
        IType secondParameterDSLType =
            typeBuilder.createDSLTypeForJavaTypeInScope(globalScope, secondParameterType);
        if (null == secondParameterDSLType) {
            throw new RuntimeException("Type of parameter of Function could not be translated");
        }

        // the third type parameter of the BiFunction<T,U,R> interface will correspond to
        // the return type of the function
        Type returnType = typeArray[2];
        IType returnDSLType = typeBuilder.createDSLTypeForJavaTypeInScope(globalScope, returnType);

        if (null == returnDSLType) {
            throw new RuntimeException("Returntype of Function could not be translated");
        }
        return new FunctionType(returnDSLType, firstParameterDSLType, secondParameterDSLType);
    }
}
