package interpreter.mockecs;

import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;

public class ExternalTypeBuilderMultiParam {
    @DSLTypeAdapter
    public static ExternalType buildExternalType(
            @DSLTypeMember(name = "number") int n, @DSLTypeMember(name = "string") String str) {
        return new ExternalType(n, 12, str);
    }
}
