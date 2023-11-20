package dsl.semanticanalysis.types;

import dsl.semanticanalysis.types.annotation.DSLType;
import dsl.semanticanalysis.types.annotation.DSLTypeMember;

@DSLType
public class ComponentWithInterfaceMember {
    private @DSLTypeMember ITestInterface interfaceMember;
}
