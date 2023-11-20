package dsl.semanticanalysis.types;

import dsl.runtime.nativefunctions.NativeMethod;
import dsl.runtime.value.SetValue;
import dsl.semanticanalysis.IScope;
import dsl.semanticanalysis.ScopedSymbol;

public class SetType extends ScopedSymbol implements IType {
    public IType getElementType() {
        return this.dataType;
    }

    public static String getSetTypeName(IType elementType) {
        return elementType.getName() + "<>";
    }

    public SetType(IType elementType, IScope parentScope) {
        super(getSetTypeName(elementType), parentScope, elementType);

        NativeMethod addMethod =
                new NativeMethod(
                        "add",
                        this,
                        new FunctionType(BuiltInType.boolType, elementType),
                        SetValue.AddMethod.instance);
        this.bind(addMethod);

        NativeMethod sizeMethod =
                new NativeMethod(
                        "size",
                        this,
                        new FunctionType(BuiltInType.intType, BuiltInType.noType),
                        SetValue.SizeMethod.instance);
        this.bind(sizeMethod);

        NativeMethod getMethod =
                new NativeMethod(
                        "contains",
                        this,
                        new FunctionType(BuiltInType.boolType, elementType),
                        SetValue.ContainsMethod.instance);
        this.bind(getMethod);
    }

    @Override
    public Kind getTypeKind() {
        return Kind.SetType;
    }
}
