package semanticanalysis.types;

import java.lang.reflect.Field;

public interface IFunctionTypeBuilder {
    FunctionType buildFunctionType(Field field, TypeBuilder typeBuilder);

}
