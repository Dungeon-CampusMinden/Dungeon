package interpreter.mockECS;

import semanticAnalysis.types.DSLTypeAdapter;
import semanticAnalysis.types.DSLTypeMember;

public class ExternalTypeBuilderMultiParam {
    @DSLTypeAdapter(t = ExternalType.class)
    public static ExternalType buildExternalType(
            @DSLTypeMember(name = "number") int n, @DSLTypeMember(name = "string") String str) {
        return new ExternalType(n, 12, str);
    }
}
