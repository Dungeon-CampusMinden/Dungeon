package semanticanalysis.types;

import dungeonFiles.DungeonConfig;

import helpers.Helpers;

import interpreter.DSLInterpreter;

import org.junit.Assert;
import org.junit.Test;

import runtime.AggregateValue;
import runtime.MemorySpace;

import semanticanalysis.Scope;

import taskDependencyGraph.TaskDependencyGraph;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class TestTypeInstantiator {
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

        // the fieldName does not necessary match the member name in the created DSLType, so store a
        // map from member to
        // field name
        HashMap<String, String> typeMemberNameToJavaFieldName =
                tb.typeMemberNameToJavaFieldMap(DungeonConfig.class);

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
                TaskDependencyGraph graph =
                        new TaskDependencyGraph(new ArrayList<>(), new ArrayList<>());
                setValues.put(member.getName(), graph);
                ms.resolve(member.getName()).setInternalValue(graph);
            }
            memberCounter++;
        }

        AggregateValue aggregateValue = new AggregateValue(type, null);
        aggregateValue.setMemorySpace(ms);

        TypeInstantiator ti = new TypeInstantiator(interpreter);
        var instance = ti.instantiate(aggregateValue);

        // check, that all values originally only set in the memory space match the
        // set values in the java class instance
        for (String typeMemberName : setValues.keySet()) {
            try {
                var fieldName = typeMemberNameToJavaFieldName.get(typeMemberName);
                var field = DungeonConfig.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                var value = field.get(instance);

                Assert.assertEquals(setValues.get(typeMemberName), value);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

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

        // the fieldName does not necessary match the member name in the created DSLType, so store a
        // map from member to
        // field name
        HashMap<String, String> typeMemberNameToJavaFieldName =
                tb.typeMemberNameToJavaFieldMap(TestClassOuter.class);

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
                TaskDependencyGraph graph =
                        new TaskDependencyGraph(new ArrayList<>(), new ArrayList<>());
                setValues.put(member.getName(), graph);
                ms.resolve(member.getName()).setInternalValue(graph);
            }
            memberCounter++;
        }

        AggregateValue aggregateValue = new AggregateValue(type, null);
        aggregateValue.setMemorySpace(ms);

        TypeInstantiator ti = new TypeInstantiator(interpreter);
        var instance = ti.instantiate(aggregateValue);

        // check, that all values originally only set in the memory space match the
        // set values in the java class instance
        for (String typeMemberName : setValues.keySet()) {
            try {
                var fieldName = typeMemberNameToJavaFieldName.get(typeMemberName);
                var field = TestClassOuter.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                var value = field.get(instance);

                Assert.assertEquals(setValues.get(typeMemberName), value);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
