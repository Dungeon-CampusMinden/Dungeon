package semanticanalysis.types;

import semanticanalysis.IScope;
import semanticanalysis.ScopedSymbol;

public class MapType extends ScopedSymbol implements IType {
    private final IType keyType;
    private final IType elementType;

    public IType getKeyType() {
        return this.keyType;
    }

    public IType getElementType() {
        return this.elementType;
    }

    public static String getMapTypeName(IType keyType, IType elementType) {
        return "[" + keyType.getName() + "->" + elementType.getName() + "]";
    }

    public MapType(IType keyType, IType elementType, IScope parentScope) {
        super(getMapTypeName(keyType, elementType), parentScope, elementType);
        this.keyType = keyType;
        this.elementType = elementType;

        /*
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
         */
    }

    @Override
    public Kind getTypeKind() {
        return Kind.MapType;
    }
}
