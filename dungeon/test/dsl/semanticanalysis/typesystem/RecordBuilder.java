package dsl.semanticanalysis.typesystem;

import dsl.annotation.DSLTypeAdapter;

/** WTF? . */
public class RecordBuilder {
  /**
   * WTF? .
   *
   * @param param foo
   * @return foo
   */
  @DSLTypeAdapter
  public static TestRecordComponent buildTestRecord(String param) {
    return new TestRecordComponent(42, param);
  }
}
