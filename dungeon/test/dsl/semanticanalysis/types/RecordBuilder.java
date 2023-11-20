package dsl.semanticanalysis.types;

import dsl.semanticanalysis.types.annotation.DSLTypeAdapter;

public class RecordBuilder {
    @DSLTypeAdapter
    public static TestRecordComponent buildTestRecord(String param) {
        return new TestRecordComponent(42, param);
    }
}
