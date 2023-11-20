package dsl.semanticanalysis.typesystem;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeMember;

@DSLType
public class ComponentWithInterfaceMember {
    private @DSLTypeMember ITestInterface interfaceMember;
}
