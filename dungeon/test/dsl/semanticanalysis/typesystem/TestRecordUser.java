package dsl.semanticanalysis.typesystem;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;

/** WTF? . */
@DSLType
public class TestRecordUser {
  @DSLTypeMember int member1;
  @DSLTypeMember TestRecordComponent componentMember;

  /** WTF? . */
  public TestRecordUser() {
    this.member1 = 0;
    this.componentMember = null;
  }
}
