package dsl.semanticanalysis.symbol;

import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeProperty;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

/** This Symbol enables the usage {@link IDSLExtensionProperty} in {@link AggregateType}s. */
public class PropertySymbol extends Symbol {
    private IDSLExtensionProperty<?, ?> property;
    private final boolean settable;
    private final boolean gettable;

    public boolean isSettable() {
        return settable;
    }

    public boolean isGettable() {
        return gettable;
    }

    public IDSLExtensionProperty<?, ?> getProperty() {
        return property;
    }

    public PropertySymbol(
            String symbolName,
            IScope parentScope,
            IType dataType,
            IDSLExtensionProperty<?, ?> property) {
        super(symbolName, parentScope, dataType);
        this.property = property;

        var annotation = property.getClass().getAnnotation(DSLTypeProperty.class);
        this.settable = annotation.isSettable();
        this.gettable = annotation.isGettable();
    }
}
