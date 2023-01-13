package semanticAnalysis.types;

import semanticAnalysis.IScope;
import semanticAnalysis.ScopedSymbol;

public class AggregateType extends ScopedSymbol implements IType {

    protected Class<?> originType;

    /**
     * Constructor
     *
     * @param name Name of this type
     * @param parentScope parent scope of this type
     */
    public AggregateType(String name, IScope parentScope) {
        super(name, parentScope, null);
        originType = null;
    }

    public AggregateType(String name, IScope parentScope, Class<?> originType) {
        super(name, parentScope, null);
        this.originType = originType;
    }

    @Override
    public Kind getTypeKind() {
        return Kind.Aggregate;
    }

    public Class<?> getOriginType() {
        return this.originType;
    }
}
