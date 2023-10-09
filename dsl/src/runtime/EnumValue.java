package runtime;

import semanticanalysis.Symbol;
import semanticanalysis.types.EnumType;

public class EnumValue extends Value {
    public EnumValue(EnumType enumType, Symbol enumVariantSymbol) {
        super(enumType, enumVariantSymbol);
    }

    public Symbol getEnumVariantSymbol() {
        return (Symbol) this.getInternalValue();
    }
}
