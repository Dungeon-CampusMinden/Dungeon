package interpreter.mockECS;

import semanticAnalysis.types.DSLTypeAdapter;

public class ExternalTypeBuilder {
    @DSLTypeAdapter(t = ExternalType.class)
    public static ExternalType buildExternalType(String str) {
        return new ExternalType(42, 12, str);
    }
}
