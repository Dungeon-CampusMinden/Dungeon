package semanticanalysis.types;

import runtime.ListValue;
import runtime.nativefunctions.NativeMethod;

import semanticanalysis.IScope;
import semanticanalysis.ScopedSymbol;

public class ListType extends ScopedSymbol implements IType {
    public IType getElementType() {
        return this.dataType;
    }

    public static String getListTypeName(IType elementType) {
        return elementType.getName() + "[]";
    }

    public ListType(IType elementType, IScope parentScope) {
        super(getListTypeName(elementType), parentScope, elementType);

        NativeMethod addMethod =
                new NativeMethod(
                        "add",
                        this,
                        new FunctionType(BuiltInType.noType, elementType),
                        ListValue.AddMethod.instance);
        this.bind(addMethod);

        NativeMethod sizeMethod =
            new NativeMethod(
                "size",
                this,
                new FunctionType(BuiltInType.intType, BuiltInType.noType),
                ListValue.SizeMethod.instance);
        this.bind(sizeMethod);
    }

    @Override
    public Kind getTypeKind() {
        return Kind.ListType;
    }
}
