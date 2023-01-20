package semanticAnalysis.types;

public class RecordBuilder {
    @DSLTypeAdapter(t = TestRecordComponent.class)
    public static TestRecordComponent buildTestRecord(String param) {
        return new TestRecordComponent(42, param);
    }
}
