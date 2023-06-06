package interpreter.mockecs;

import semanticanalysis.types.DSLTypeAdapter;

public class ExternalTypeBuilder {
    @DSLTypeAdapter
    public static ExternalType buildExternalType(String str) {
        return new ExternalType(42, 12, str);
    }
}
