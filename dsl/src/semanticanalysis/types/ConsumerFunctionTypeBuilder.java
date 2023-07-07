package semanticanalysis.types;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

public class ConsumerFunctionTypeBuilder implements IFunctionTypeBuilder {
    public static ConsumerFunctionTypeBuilder instance = new ConsumerFunctionTypeBuilder();

    private ConsumerFunctionTypeBuilder(){}

    @Override
    public FunctionType buildFunctionType(Field field, TypeBuilder typeBuilder) {
        var genericType = field.getGenericType();

        var typeMap = typeBuilder.getJavaTypeToDSLTypeMap();

        var parameterizedType = (ParameterizedType)genericType;
        // the parameters will be the arguments for the function
        ArrayList<IType> parameterTypes = new ArrayList<>(parameterizedType.getActualTypeArguments().length);
        for (var parameterType : parameterizedType.getActualTypeArguments()) {
            IType dslType = TypeBuilder.getDSLTypeForClass((Class<?>)parameterType);
            if (null == dslType) {
                dslType = typeMap.get(parameterType);
            }
            if (null == dslType) {
                throw new RuntimeException("Type of parameter of Consumer could not be translated");
            } else {
                parameterTypes.add(dslType);
            }
        }
        return new FunctionType(BuiltInType.noType, parameterTypes);
    }
}

