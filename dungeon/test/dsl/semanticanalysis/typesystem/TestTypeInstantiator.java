package dsl.semanticanalysis.typesystem;

import dsl.helpers.Helpers;
import dsl.interpreter.DSLInterpreter;
import dsl.runtime.memoryspace.MemorySpace;
import dsl.runtime.value.AggregateValue;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.typesystem.instantiation.TypeInstantiator;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import entrypoint.DungeonConfig;
import graph.taskdependencygraph.TaskDependencyGraph;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;

/** WTF? . */
public class TestTypeInstantiator {
  /** WTF? . */
  @Test
  public void testInstantiatorRecord()
      throws NoSuchFieldException,
          InvocationTargetException,
          InstantiationException,
          IllegalAccessException {
    MemorySpace ms = new MemorySpace();
    HashMap<String, Object> setValues = new HashMap<>();

    TypeBuilder tb = new TypeBuilder();
    Scope scope = new Scope();
    DSLInterpreter interpreter = new DSLInterpreter();
    var type = (AggregateType) tb.createDSLTypeForJavaTypeInScope(scope, DungeonConfig.class);

    int memberCounter = 0;
    for (var member : type.getSymbols()) {
      Helpers.bindDefaultValueInMemorySpace(member, ms, interpreter);

      if (member.getDataType().equals(BuiltInType.intType)) {
        setValues.put(member.getName(), memberCounter);
        ms.resolve(member.getName()).setInternalValue(memberCounter);
      } else if (member.getDataType().equals(BuiltInType.stringType)) {
        String str = "Hello, World!" + memberCounter;
        setValues.put(member.getName(), str);
        ms.resolve(member.getName()).setInternalValue(str);
      } else if (member.getDataType().equals(BuiltInType.graphType)) {
        TaskDependencyGraph graph = new TaskDependencyGraph(new ArrayList<>(), new ArrayList<>());
        setValues.put(member.getName(), graph);
        ms.resolve(member.getName()).setInternalValue(graph);
      }
      memberCounter++;
    }

    AggregateValue aggregateValue = new AggregateValue(type, null);
    aggregateValue.setMemorySpace(ms);

    TypeInstantiator ti = new TypeInstantiator(interpreter);
    var instance = ti.instantiate(aggregateValue);

    // the fieldName does not necessary match the member name in the created DSLType, so store a
    // map from member to
    // field name
    HashMap<String, Field> typeMemberNameToField = TypeBuilder.mapTypeMembersToField(type);

    // check, that all values originally only set in the memory space match the
    // set values in the java class instance
    for (String typeMemberName : setValues.keySet()) {
      try {
        var field = typeMemberNameToField.get(typeMemberName);

        field.setAccessible(true);
        var value = field.get(instance);

        Assert.assertEquals(setValues.get(typeMemberName), value);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /** WTF? . */
  @Test
  public void testInstantiatorClass()
      throws NoSuchFieldException,
          InvocationTargetException,
          InstantiationException,
          IllegalAccessException {
    MemorySpace ms = new MemorySpace();
    HashMap<String, Object> setValues = new HashMap<>();

    DSLInterpreter interpreter = new DSLInterpreter();
    TypeBuilder tb = new TypeBuilder();
    Scope scope = new Scope();
    var type = (AggregateType) tb.createDSLTypeForJavaTypeInScope(scope, TestClassOuter.class);

    int memberCounter = 0;
    for (var member : type.getSymbols()) {
      Helpers.bindDefaultValueInMemorySpace(member, ms, interpreter);
      if (member.getDataType().equals(BuiltInType.intType)) {
        setValues.put(member.getName(), memberCounter);
        ms.resolve(member.getName()).setInternalValue(memberCounter);
      } else if (member.getDataType().equals(BuiltInType.stringType)) {
        String str = "Hello, World!" + memberCounter;
        setValues.put(member.getName(), str);
        ms.resolve(member.getName()).setInternalValue(str);
      } else if (member.getDataType().equals(BuiltInType.graphType)) {
        TaskDependencyGraph graph = new TaskDependencyGraph(new ArrayList<>(), new ArrayList<>());
        setValues.put(member.getName(), graph);
        ms.resolve(member.getName()).setInternalValue(graph);
      }
      memberCounter++;
    }

    AggregateValue aggregateValue = new AggregateValue(type, null);
    aggregateValue.setMemorySpace(ms);

    TypeInstantiator ti = new TypeInstantiator(interpreter);
    var instance = ti.instantiate(aggregateValue);

    // the fieldName does not necessary match the member name in the created DSLType, so store a
    // map from member to
    // field name
    HashMap<String, Field> typeMemberNameToField = TypeBuilder.mapTypeMembersToField(type);

    // check, that all values originally only set in the memory space match the
    // set values in the java class instance
    for (String typeMemberName : setValues.keySet()) {
      try {
        var field = typeMemberNameToField.get(typeMemberName);
        field.setAccessible(true);
        var value = field.get(instance);

        Assert.assertEquals(setValues.get(typeMemberName), value);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
