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

        NativeMethod addMehod =
                new NativeMethod(
                        "add",
                        this,
                        new FunctionType(BuiltInType.noType, elementType),
                        ListValue.AddMethod.instance);
        this.bind(addMehod);
    }

    @Override
    public Kind getTypeKind() {
        return Kind.ListType;
    }
}
