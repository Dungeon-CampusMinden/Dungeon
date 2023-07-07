package semanticanalysis.types;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class FunctionFunctionTypeBuilder implements IFunctionTypeBuilder {
    public static FunctionFunctionTypeBuilder instance = new FunctionFunctionTypeBuilder();

    private FunctionFunctionTypeBuilder() {
    }

    @Override
    public FunctionType buildFunctionType(Field field, TypeBuilder typeBuilder) {
        var genericType = field.getGenericType();

        var typeMap = typeBuilder.getJavaTypeToDSLTypeMap();

        var parameterizedType = (ParameterizedType) genericType;
        // the parameters will be the arguments for the function
        Type[] typeArray = parameterizedType.getActualTypeArguments();

        // TODO: the code fragment below is used multiple times over in the interpreter
        //  codebase and should be refactored
        IType parameterType = TypeBuilder.getDSLTypeForClass((Class<?>) typeArray[0]);
        if (null == parameterType) {
            parameterType = typeMap.get(typeArray[0]);
        }
        if (null == parameterType) {
            throw new RuntimeException("Type of parameter of Function could not be translated");
        }

        IType returnType = TypeBuilder.getDSLTypeForClass((Class<?>) typeArray[1]);
        if (null == returnType) {
            returnType = typeMap.get(typeArray[1]);
        }
        if (null == returnType) {
            throw new RuntimeException("Returntype of Function could not be translated");
        }
        return new FunctionType(returnType, parameterType);
    }
}
