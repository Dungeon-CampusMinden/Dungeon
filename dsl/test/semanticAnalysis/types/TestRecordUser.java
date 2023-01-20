package semanticAnalysis.types;

@DSLType
public class TestRecordUser {
    @DSLTypeMember int member1;
    @DSLTypeMember TestRecordComponent componentMember;

    public TestRecordUser() {
        this.member1 = 0;
        this.componentMember = null;
    }
}
