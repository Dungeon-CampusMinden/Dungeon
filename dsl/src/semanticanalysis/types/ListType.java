package semanticanalysis.types;

import semanticanalysis.IScope;
import semanticanalysis.Symbol;

public class ListType extends Symbol implements IType {
    // private final IType elementType;
    public IType getElementType() {
        return this.dataType;
    }

    public static String getListTypeName(IType elementType) {
        return elementType.getName() + "[]";
    }

    public ListType(IType elementType, IScope parentScope) {
        this.name = getListTypeName(elementType);
        this.dataType = elementType;
        this.scope = parentScope;
    }

    @Override
    public Kind getTypeKind() {
        return Kind.ListType;
    }
}
