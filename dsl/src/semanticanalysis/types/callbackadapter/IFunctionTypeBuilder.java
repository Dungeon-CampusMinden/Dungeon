package semanticanalysis.types.callbackadapter;

import semanticanalysis.IScope;
import semanticanalysis.types.FunctionType;
import semanticanalysis.types.TypeBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/** Builder interface for a {@link FunctionType} for a callback method */
public interface IFunctionTypeBuilder {
    /**
     * Build a {@link FunctionType} representing the signature of a callback
     *
     * @param genericType the generic type of the function type to build
     * @param typeBuilder {@link TypeBuilder} instance to lookup parameter types
     * @return {@link FunctionType} corresponding to the callback signature
     */
    //FunctionType buildFunctionType(Field field, TypeBuilder typeBuilder, IScope globalScope);
    FunctionType buildFunctionType(Type genericType, TypeBuilder typeBuilder, IScope globalScope);
}
