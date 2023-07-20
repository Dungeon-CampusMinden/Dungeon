package semanticanalysis.types;

import semanticanalysis.IScope;
import semanticanalysis.Symbol;

public class SetType extends Symbol implements IType {
    //private final IType elementType;
    public IType getElementType() {
        return this.dataType;
    }

    public static String getSetTypeName(IType elementType) {
        return elementType.getName() + "<>";
    }

    public SetType(IType elementType, IScope parentScope) {
        this.name = getSetTypeName(elementType);
        this.dataType = elementType;
        this.scope = parentScope;
    }

    @Override
    public Kind getTypeKind() {
        return Kind.SetType;
    }
}
