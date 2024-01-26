package dsl.semanticanalysis.typesystem;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;

@DSLType
public class TestRecordUser {
  @DSLTypeMember int member1;
  @DSLTypeMember TestRecordComponent componentMember;

  public TestRecordUser() {
    this.member1 = 0;
    this.componentMember = null;
  }
}
