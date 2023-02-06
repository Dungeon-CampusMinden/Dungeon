package semanticAnalysis.types;

import java.lang.reflect.Method;
import semanticAnalysis.IScope;

public class AggregateTypeAdapter extends AggregateType {

    final Method builderMethod;

    public AggregateTypeAdapter(
            String name, IScope parentScope, Class<?> originType, Method builderMethod) {
        super(name, parentScope, originType);
        this.builderMethod = builderMethod;
    }

    @Override
    public Kind getTypeKind() {
        return Kind.AggregateAdapted;
    }
}
