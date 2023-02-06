package interpreter.mockECS;

import semanticAnalysis.types.DSLTypeAdapter;

public class ExternalTypeBuilderMultiParam {
    @DSLTypeAdapter(t = ExternalType.class)
    public static ExternalType buildExternalType(int n, String str) {
        return new ExternalType(n, 12, str);
    }
}
