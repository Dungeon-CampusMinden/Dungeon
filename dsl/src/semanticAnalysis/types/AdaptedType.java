package semanticAnalysis.types;

import java.lang.reflect.Method;
import semanticAnalysis.IScope;

/** This is used to adapt a type, which only requires a single parameter for construction */
public class AdaptedType implements IType {
    final String name;
    final Class<?> originType;
    final BuiltInType buildParameterType;
    final IScope parentScope;

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
        return parentScope;
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
        this.name = name;
        this.parentScope = parentScope;
        this.originType = originType;
        this.buildParameterType = buildParameterType;
        this.builderMethod = builderMethod;
    }
}
