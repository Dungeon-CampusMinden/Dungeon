package semanticAnalysis.types;

import graph.Graph;
import org.junit.Test;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import static org.junit.Assert.*;

public class TestTypeBuilder {
    @Test
    public void testNameConversion() {
        String name = "helloWorldW";
        var convertedName = TypeBuilder.convertToDSLName(name);
        assertEquals("hello_world_w", convertedName);
    }

    /** Test class for testing conversion into DSL datatype */
    @DSLType
    private class TestComponent {
        @DSLTypeMember public int intMember;

        @DSLTypeMember public String stringMember;

        @DSLTypeMember public Graph<String> graphMember;
    }

    /** Test class for testing conversion into DSL datatype */
    @DSLType
    private class ChainClass {
        @DSLTypeMember public TestComponent testComponentMember;

        @DSLTypeMember public String stringMember;
    }

    @DSLType
    private record TestRecord(@DSLTypeMember int comp1, @DSLTypeMember String comp2) {}

    @Test
    public void testSimpleClass() {
        TypeBuilder typeBuilder = new TypeBuilder();
        Scope scope = new Scope();
        var dslType = typeBuilder.createTypeFromClass(scope, TestComponent.class);

        var stringMember = dslType.resolve("string_member");
        assertNotSame(stringMember, Symbol.NULL);
        assertEquals(BuiltInType.stringType, stringMember.getDataType());

        var intMember = dslType.resolve("int_member");
        assertNotSame(intMember, Symbol.NULL);
        assertEquals(BuiltInType.intType, intMember.getDataType());

        var graphMember = dslType.resolve("graph_member");
        assertNotSame(graphMember, Symbol.NULL);
        assertEquals(BuiltInType.graphType, graphMember.getDataType());
    }

    @Test
    public void testChainedClass() {
        TypeBuilder typeBuilder = new TypeBuilder();
        Scope scope = new Scope();
        var dslType = typeBuilder.createTypeFromClass(scope, ChainClass.class);

        var testComponentMember = dslType.resolve("test_component_member");
        assertNotSame(testComponentMember, Symbol.NULL);

        var testComponentMemberType = testComponentMember.getDataType();
        assertEquals("test_component", testComponentMemberType.getName());

        var intMemberInTestComponent =
                ((AggregateType) testComponentMemberType).resolve("int_member");
        assertNotSame(intMemberInTestComponent, Symbol.NULL);
    }

    @Test
    public void testRecord() {
        TypeBuilder typeBuilder = new TypeBuilder();
        Scope scope = new Scope();
        var dslType = typeBuilder.createTypeFromClass(scope, TestRecord.class);

        var comp1 = dslType.resolve("comp1");
        assertNotSame(comp1, Symbol.NULL);
        assertEquals(BuiltInType.intType, comp1.getDataType());

        var comp2 = dslType.resolve("comp2");
        assertNotSame(comp2, Symbol.NULL);
        assertEquals(BuiltInType.stringType, comp2.getDataType());
    }


    @Test
    public void testTypeAdapterRegister() {
        TypeBuilder tb = new TypeBuilder();
        tb.registerTypeAdapter(RecordBuilder.class);

        var adapter = tb.getRegisteredTypeAdapter(TestRecordComponent.class);
        assertNotNull(adapter);

        try {
            var object = adapter.invoke(null, "Hello");
            assertNotNull(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
