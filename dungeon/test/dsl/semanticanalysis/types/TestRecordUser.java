package dsl.semanticanalysis.types;

import dsl.semanticanalysis.types.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.types.typebuilding.annotation.DSLTypeMember;

@DSLType
public class TestRecordUser {
    @DSLTypeMember
    int member1;
    @DSLTypeMember TestRecordComponent componentMember;

    public TestRecordUser() {
        this.member1 = 0;
        this.componentMember = null;
    }
}
