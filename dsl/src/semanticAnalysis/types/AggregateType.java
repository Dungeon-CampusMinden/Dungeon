package semanticAnalysis.types;

import semanticAnalysis.IScope;
import semanticAnalysis.ScopedSymbol;

public class AggregateType extends ScopedSymbol implements IType {

    protected Class<?> originalJavaClass;

    /**
     * Constructor
     *
     * @param name Name of this type
     * @param parentScope parent scope of this type
     */
    public AggregateType(String name, IScope parentScope) {
        super(name, parentScope, null);
        originalJavaClass = null;
    }

    public AggregateType(String name, IScope parentScope, Class<?> originalJavaClass) {
        super(name, parentScope, null);
        this.originalJavaClass = originalJavaClass;
    }

    @Override
    public Kind getTypeKind() {
        return Kind.Aggregate;
    }

    public Class<?> getOriginalJavaClass() {
        return this.originalJavaClass;
    }
}
