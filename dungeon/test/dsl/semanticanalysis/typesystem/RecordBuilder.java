package dsl.semanticanalysis.typesystem;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeAdapter;

public class RecordBuilder {
  @DSLTypeAdapter
  public static TestRecordComponent buildTestRecord(String param) {
    return new TestRecordComponent(42, param);
  }
}
