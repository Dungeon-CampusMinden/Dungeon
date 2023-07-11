package semanticanalysis.types.CallbackAdapter;

import semanticanalysis.types.FunctionType;
import semanticanalysis.types.TypeBuilder;

import java.lang.reflect.Field;

/** Builder interface for a {@link FunctionType} for a callback method */
public interface IFunctionTypeBuilder {
    /**
     * Build a {@link FunctionType} representing the signature of a callback
     *
     * @param field The field defining the callback
     * @param typeBuilder {@link TypeBuilder} instance to lookup parameter types
     * @return {@link FunctionType} corresponding to the callback signature
     */
    FunctionType buildFunctionType(Field field, TypeBuilder typeBuilder);
}
