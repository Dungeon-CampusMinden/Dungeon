package semanticAnalysis.types;

import semanticAnalysis.IScope;

public class AggregateTypeAdapter extends AggregateType {
    public AggregateTypeAdapter(String name, IScope parentScope, Class<?> originType) {
        super(name, parentScope, originType);
    }

    @Override
    public Kind getTypeKind() {
        return Kind.AggregateAdapted;
    }
}
