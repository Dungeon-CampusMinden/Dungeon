package semanticAnalysis.types;

import semanticAnalysis.IScope;

/** This is used to adapt a type, which only requires a single parameter for construction */
public class AdaptedType implements IType {
    final String name;
    final Class<?> originType;
    final BuiltInType buildParameterType;
    final IScope parentScope;

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

    public AdaptedType(
            String name, IScope parentScope, Class<?> originType, BuiltInType buildParameterType) {
        this.name = name;
        this.parentScope = parentScope;
        this.originType = originType;
        this.buildParameterType = buildParameterType;
    }
}
