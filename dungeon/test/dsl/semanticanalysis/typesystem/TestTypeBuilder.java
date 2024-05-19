package dsl.semanticanalysis.typesystem;

import static org.junit.Assert.*;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import dsl.interpreter.TestEnvironment;
import dsl.interpreter.mockecs.*;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.*;
import graph.taskdependencygraph.TaskDependencyGraph;
import java.lang.reflect.InvocationTargetException;
import org.junit.Assert;
import org.junit.Test;

/** WTF? . */
public class TestTypeBuilder {
  /** WTF? . */
  @Test
  public void testNameConversion() {
    String name = "helloWorldW";
    var convertedName = TypeBuilder.convertToDSLName(name);
    assertEquals("hello_world_w", convertedName);
  }

  /** Test class for testing conversion into DSL datatype. */
  @DSLType
  private class TestComponent {
    @DSLTypeMember public int intMember;

    @DSLTypeMember public String stringMember;

    @DSLTypeMember public TaskDependencyGraph graphMember;
  }

  /** Test class for testing conversion into DSL datatype. */
  @DSLType
  private class ChainClass {
    @DSLTypeMember public TestComponent testComponentMember;

    @DSLTypeMember public String stringMember;
  }

  @DSLType
  private record TestRecord(
      @DSLTypeMember int comp1, @DSLTypeMember String comp2, @DSLTypeMember float comp3) {}

  /** WTF? . */
  @Test
  public void testSimpleClass() {
    TypeBuilder typeBuilder = new TypeBuilder();
    Scope scope = new Scope();
    var dslType =
        (AggregateType) typeBuilder.createDSLTypeForJavaTypeInScope(scope, TestComponent.class);

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

  /** WTF? . */
  @Test
  public void testChainedClass() {
    TypeBuilder typeBuilder = new TypeBuilder();
    Scope scope = new Scope();
    var dslType =
        (AggregateType) typeBuilder.createDSLTypeForJavaTypeInScope(scope, ChainClass.class);

    var testComponentMember = dslType.resolve("test_component_member");
    assertNotSame(testComponentMember, Symbol.NULL);

    var testComponentMemberType = testComponentMember.getDataType();
    assertEquals("test_component", testComponentMemberType.getName());

    var intMemberInTestComponent = ((AggregateType) testComponentMemberType).resolve("int_member");
    assertNotSame(intMemberInTestComponent, Symbol.NULL);
  }

  /** WTF? . */
  @Test
  public void testRecord() {
    TypeBuilder typeBuilder = new TypeBuilder();
    Scope scope = new Scope();
    var dslType =
        (AggregateType) typeBuilder.createDSLTypeForJavaTypeInScope(scope, TestRecord.class);

    var comp1 = dslType.resolve("comp1");
    assertNotSame(comp1, Symbol.NULL);
    assertEquals(BuiltInType.intType, comp1.getDataType());

    var comp2 = dslType.resolve("comp2");
    assertNotSame(comp2, Symbol.NULL);
    assertEquals(BuiltInType.stringType, comp2.getDataType());

    var comp3 = dslType.resolve("comp3");
    assertNotSame(comp3, Symbol.NULL);
    assertEquals(BuiltInType.floatType, comp3.getDataType());
  }

  /** WTF? . */
  @Test
  public void testTypeAdapterRegister() {
    TypeBuilder tb = new TypeBuilder();
    Scope scope = new Scope();
    tb.registerTypeAdapter(RecordBuilder.class, scope);

    var adapter = tb.getRegisteredTypeAdaptersForType(TestRecordComponent.class).get(0);
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

  /** WTF? . */
  @Test
  public void testAggregateTypeAdapterRegister() {
    TypeBuilder tb = new TypeBuilder();
    Scope scope = new Scope();
    tb.registerTypeAdapter(ExternalTypeBuilderMultiParam.class, scope);

    var adapter = tb.getRegisteredTypeAdaptersForType(ExternalType.class).get(0);
    assertNotNull(adapter);

    try {
      var object = adapter.invoke(null, 42, "Hello");
      assertNotNull(object);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  /** WTF? . */
  @Test
  public void testAggregateTypeAdapterCreation() {
    TypeBuilder tb = new TypeBuilder();
    Scope scope = new Scope();
    tb.registerTypeAdapter(ExternalTypeBuilderMultiParam.class, scope);
    var adapterType = tb.createDSLTypeForJavaTypeInScope(Scope.NULL, ExternalType.class);

    assertNotNull(adapterType);
    var symbols = ((AggregateTypeAdapter) adapterType).getSymbols();
    assertEquals("number", symbols.get(0).getName());
    assertEquals(BuiltInType.intType, symbols.get(0).getDataType());
    assertEquals("string", symbols.get(1).getName());
    assertEquals(BuiltInType.stringType, symbols.get(1).getDataType());

    try {
      var builderMethod = ((AggregateTypeAdapter) adapterType).builderMethod();
      var expected =
          ExternalTypeBuilderMultiParam.class.getDeclaredMethod(
              "buildExternalType", int.class, String.class);
      assertEquals(expected, builderMethod);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  /** WTF? . */
  @Test
  public void testAdapterUsage() {
    TypeBuilder tb = new TypeBuilder();
    Scope scope = new Scope();
    tb.registerTypeAdapter(RecordBuilder.class, scope);
    var type = tb.createDSLTypeForJavaTypeInScope(scope, TestRecordUser.class);
    var memberSymbol = ((AggregateType) type).resolve("component_member");
    assertNotEquals(Symbol.NULL, memberSymbol);
    var membersDatatype = memberSymbol.getDataType();
    assertEquals(IType.Kind.AggregateAdapted, membersDatatype.getTypeKind());
  }

  /** WTF? . */
  @Test
  public void testExternalTypeMember() {
    TypeBuilder typeBuilder = new TypeBuilder();
    Scope scope = new Scope();
    var dslType =
        (AggregateType)
            typeBuilder.createDSLTypeForJavaTypeInScope(
                scope, ComponentWithExternalTypeMember.class);

    assertNotSame(dslType, null);
    assertNotSame(dslType, Symbol.NULL);
  }

  /** WTF? . */
  @Test
  public void testInterfaceMember() {
    TypeBuilder typeBuilder = new TypeBuilder();
    Scope scope = new Scope();
    var dslType =
        (AggregateType)
            typeBuilder.createDSLTypeForJavaTypeInScope(scope, ComponentWithInterfaceMember.class);

    assertNotSame(dslType, null);
    assertNotSame(dslType, Symbol.NULL);
  }

  /** WTF? . */
  @Test
  public void testCallbackConsumer() {
    TypeBuilder tb = new TypeBuilder();
    Scope scope = new Scope();
    // register Entity type (setup)
    var entityType = (AggregateType) tb.createDSLTypeForJavaTypeInScope(scope, Entity.class);

    var dslType =
        (AggregateType) tb.createDSLTypeForJavaTypeInScope(scope, TestComponentWithCallback.class);
    var callbackSymbol = dslType.resolve("on_interaction");
    assertNotEquals(Symbol.NULL, callbackSymbol);
    var symbolType = callbackSymbol.getDataType();
    assertNotEquals(BuiltInType.noType, symbolType);
    var functionType = (FunctionType) symbolType;
    assertEquals(entityType, functionType.getParameterTypes().get(0));
  }

  /** WTF? . */
  @Test
  public void testCallbackTriConsumer() {
    TestEnvironment env = new TestEnvironment();
    // TypeBuilder tb = new TypeBuilder();
    // register Entity type (setup)
    var entityType =
        (AggregateType)
            env.getTypeBuilder()
                .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);

    var dslType =
        (AggregateType)
            env.getTypeBuilder()
                .createDSLTypeForJavaTypeInScope(
                    env.getGlobalScope(), TestComponentWithTriConsumerCallback.class);
    var callbackSymbol = dslType.resolve("on_interaction");

    assertNotEquals(Symbol.NULL, callbackSymbol);
    var symbolType = callbackSymbol.getDataType();
    assertNotEquals(BuiltInType.noType, symbolType);
    var functionType = (FunctionType) symbolType;

    assertEquals(entityType, functionType.getParameterTypes().get(0));
    assertEquals(entityType, functionType.getParameterTypes().get(1));
    assertEquals(BuiltInType.boolType, functionType.getParameterTypes().get(2));
  }

  /** WTF? . */
  @Test
  public void testCallbackFunction() {
    TestEnvironment env = new TestEnvironment();
    // TypeBuilder tb = new TypeBuilder();
    // register Entity type (setup)
    var entityType =
        (AggregateType)
            env.getTypeBuilder()
                .createDSLTypeForJavaTypeInScope(env.getGlobalScope(), Entity.class);

    var dslType =
        (AggregateType)
            env.getTypeBuilder()
                .createDSLTypeForJavaTypeInScope(
                    env.getGlobalScope(), TestComponentWithFunctionCallback.class);
    var callbackSymbol = dslType.resolve("on_interaction");

    assertNotEquals(Symbol.NULL, callbackSymbol);
    var symbolType = callbackSymbol.getDataType();
    assertNotEquals(BuiltInType.noType, symbolType);
    var functionType = (FunctionType) symbolType;

    assertEquals(entityType, functionType.getParameterTypes().get(0));
    assertEquals(BuiltInType.boolType, functionType.getReturnType());
  }

  /** WTF? . */
  public class TestClass {
    boolean b = true;

    /**
     * WTF? .
     *
     * @param object foo
     * @return foo
     */
    public Object accept(Object object) {
      Entity entity = (Entity) object;
      return b;
    }
  }

  /** WTF? . */
  @Test
  public void testListMember() {
    TestEnvironment env = new TestEnvironment();
    var questConfigType =
        (AggregateType)
            env.getTypeBuilder()
                .createDSLTypeForJavaTypeInScope(
                    env.getGlobalScope(), TestComponentWithListMember.class);

    Symbol intListSymbol = questConfigType.resolve("int_list");
    assertEquals("int[]", intListSymbol.getDataType().getName());
    ListType listType = (ListType) intListSymbol.getDataType();
    assertEquals(BuiltInType.intType, listType.getElementType());

    Symbol floatListSymbol = questConfigType.resolve("float_list");
    assertEquals("float[]", floatListSymbol.getDataType().getName());
    listType = (ListType) floatListSymbol.getDataType();
    assertEquals(BuiltInType.floatType, listType.getElementType());
  }

  /** WTF? . */
  @Test
  public void testSetMember() {
    TestEnvironment env = new TestEnvironment();
    var questConfigType =
        (AggregateType)
            env.getTypeBuilder()
                .createDSLTypeForJavaTypeInScope(
                    env.getGlobalScope(), TestComponentWithSetMember.class);
    Symbol intSetSymbol = questConfigType.resolve("int_set");
    assertEquals("int<>", intSetSymbol.getDataType().getName());
    SetType setType = (SetType) intSetSymbol.getDataType();
    assertEquals(BuiltInType.intType, setType.getElementType());

    Symbol floatSetSymbol = questConfigType.resolve("float_set");
    assertEquals("float<>", floatSetSymbol.getDataType().getName());
    setType = (SetType) floatSetSymbol.getDataType();
    assertEquals(BuiltInType.floatType, setType.getElementType());
  }

  /** WTF? . */
  @Test
  public void testTypeForNull() {
    TestEnvironment env = new TestEnvironment();
    var type = env.getTypeBuilder().createDSLTypeForJavaTypeInScope(env.getGlobalScope(), null);
    Assert.assertEquals(BuiltInType.noType, type);
  }
}
