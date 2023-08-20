package semanticanalysis;

import semanticanalysis.types.DSLTypeProperty;
import semanticanalysis.types.IDSLTypeProperty;
import semanticanalysis.types.IType;

/**
 * This Symbol enables the usage {@link IDSLTypeProperty} in {@link
 * semanticanalysis.types.AggregateType}s.
 */
public class PropertySymbol extends Symbol {
    private IDSLTypeProperty<?, ?> property;
    private final boolean settable;
    private final boolean gettable;

    public boolean isSettable() {
        return settable;
    }

    public boolean isGettable() {
        return gettable;
    }

    public IDSLTypeProperty<?, ?> getProperty() {
        return property;
    }

    public PropertySymbol(
            String symbolName,
            IScope parentScope,
            IType dataType,
            IDSLTypeProperty<?, ?> property) {
        super(symbolName, parentScope, dataType);
        this.property = property;

        var annotation = property.getClass().getAnnotation(DSLTypeProperty.class);
        this.settable = annotation.isSettable();
        this.gettable = annotation.isGettable();
    }
}
