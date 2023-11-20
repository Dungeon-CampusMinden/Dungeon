package dsl.semanticanalysis.types;

import dsl.semanticanalysis.types.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.types.typebuilding.annotation.DSLTypeMember;

@DSLType
public class ComponentWithInterfaceMember {
    private @DSLTypeMember ITestInterface interfaceMember;
}
