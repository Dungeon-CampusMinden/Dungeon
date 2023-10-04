package semanticanalysis.types;

import semanticanalysis.IScope;
import semanticanalysis.ScopedSymbol;
import semanticanalysis.Symbol;

public class EnumType extends ScopedSymbol implements IType {
    protected Class<? extends Enum<?>> originType;

    public EnumType(String name, IScope parentScope, Class<? extends Enum<?>> originType) {
        super(name, parentScope, null);
        this.originType = originType;
    }

    public Class<? extends Enum<?>> getOriginType() {
        return originType;
    }

    @Override
    public Kind getTypeKind() {
        return Kind.EnumType;
    }
}

