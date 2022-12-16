package semanticAnalysis.typebuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import graph.Graph;
import org.junit.Test;
import semanticAnalysis.AggregateType;
import semanticAnalysis.BuiltInType;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;
import semanticAnalysis.typebulder.DSLType;
import semanticAnalysis.typebulder.DSLTypeMember;
import semanticAnalysis.typebulder.TypeBuilder;

public class TestTypeBuilder {
    @Test
    public void testNameConversion() {
        String name = "helloWorldW";
        var convertedName = TypeBuilder.convertToDSLName(name);
        assertEquals("hello_world_w", convertedName);
    }

    /**
     * Test class for testing conversion into DSL datatype
     */
    @DSLType
    private class TestComponent {
        @DSLTypeMember public int intMember;

        @DSLTypeMember public String stringMember;

        @DSLTypeMember public Graph<String> graphMember;
    }

    /**
     * Test class for testing conversion into DSL datatype
     */
    @DSLType
    private class ChainClass {
        @DSLTypeMember public TestComponent testComponentMember;

        @DSLTypeMember public String stringMember;
    }

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

        var intMemberInTestComponent = ((AggregateType)testComponentMemberType).resolve("int_member");
        assertNotSame(intMemberInTestComponent, Symbol.NULL);
    }
}
