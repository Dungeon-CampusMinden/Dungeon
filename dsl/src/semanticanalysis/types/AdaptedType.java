package semanticanalysis.types;

import semanticanalysis.IScope;
import semanticanalysis.Symbol;

import java.lang.reflect.Method;

/** This is used to adapt a type, which only requires a single parameter for construction */
public class AdaptedType extends Symbol implements IType {
    final Class<?> originType;
    final BuiltInType buildParameterType;

    // TODO: this is only a temporary solution
    final Method builderMethod;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Kind getTypeKind() {
        return Kind.PODAdapted;
    }

    public BuiltInType getBuildParameterType() {
        return buildParameterType;
    }

    public IScope getParentScope() {
        return this.scope;
    }

    public Class<?> getOriginType() {
        return originType;
    }

    public Method getBuilderMethod() {
        return builderMethod;
    }

    public AdaptedType(
            String name,
            IScope parentScope,
            Class<?> originType,
            BuiltInType buildParameterType,
            Method builderMethod) {
        super(name, parentScope, null);
        this.originType = originType;
        this.buildParameterType = buildParameterType;
        this.builderMethod = builderMethod;
    }
}
