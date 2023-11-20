package dsl.interpreter.mockecs;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeAdapter;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeMember;

public class ExternalTypeBuilderMultiParam {
    @DSLTypeAdapter
    public static ExternalType buildExternalType(
            @DSLTypeMember(name = "number") int n, @DSLTypeMember(name = "string") String str) {
        return new ExternalType(n, 12, str);
    }
}
