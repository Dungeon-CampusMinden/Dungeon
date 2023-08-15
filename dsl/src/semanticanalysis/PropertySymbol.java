package semanticanalysis;

import semanticanalysis.types.IDSLTypeProperty;
import semanticanalysis.types.IType;

public class PropertySymbol extends Symbol {
    private IDSLTypeProperty<?, ?> property;

    public IDSLTypeProperty<?,?> getProperty() {
        return property;
    }

    public PropertySymbol(
            String symbolName,
            IScope parentScope,
            IType dataType,
            IDSLTypeProperty<?, ?> property) {
        super(symbolName, parentScope, dataType);
        this.property = property;
    }
}
