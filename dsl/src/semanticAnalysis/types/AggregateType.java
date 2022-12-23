package semanticAnalysis.types;

import semanticAnalysis.IScope;
import semanticAnalysis.ScopedSymbol;

public class AggregateType extends ScopedSymbol implements IType {
    /**
     * Constructor
     *
     * @param name Name of this type
     * @param parentScope parent scope of this type
     */
    public AggregateType(String name, IScope parentScope) {
        super(name, parentScope, null);
    }
}
